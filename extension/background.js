// Shadow Sentinel - Background Service Worker (MV3)

importScripts('config.js');

let currentActivity = null;
let lastNavigationTime = 0;

// Helper to make API calls with 1 retry, non-blocking
async function apiRequest(endpoint, method, body, token) {
  const url = `${CONFIG.API_BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json'
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const options = {
    method: method,
    headers: headers,
    body: body ? JSON.stringify(body) : undefined
  };

  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      const response = await fetch(url, options);
      if (response.ok) {
        return await response.json();
      }
      if (response.status === 401) {
        console.warn(`[Sentinel] 401 Unauthorized for ${endpoint}`);
        chrome.storage.local.set({ sessionStatus: 'NOT_LOGGED_IN' });
        return null;
      }
      if (response.status === 409) {
        console.warn(`[Sentinel] 409 Conflict for ${endpoint}`);
        return null;
      }
      if (response.status === 403) {
        console.warn(`[Sentinel] 403 Forbidden for ${endpoint}`);
        return null;
      }
    } catch (err) {
      if (attempt === 1) {
        console.warn(`[Sentinel] Network request to ${endpoint} failed after retry:`, err);
        return null;
      }
    }
  }
  return null;
}

// Initialize session on startup
async function initSession() {
  const data = await chrome.storage.local.get(['token', 'isPaused']);
  if (!data.token) {
    chrome.storage.local.set({ sessionStatus: 'NOT_LOGGED_IN' });
    return null;
  }

  if (data.isPaused) {
    chrome.storage.local.set({ sessionStatus: 'PAUSED' });
    return null;
  }

  const session = await apiRequest('/api/sessions', 'POST', null, data.token);
  if (session && session.id) {
    await chrome.storage.local.set({
      sessionId: session.id,
      sessionStatus: 'ACTIVE',
      activityCount: 0
    });
    return session.id;
  } else {
    chrome.storage.local.set({ sessionStatus: 'ERROR' });
    return null;
  }
}

// Handle browser navigation & tab change
async function handleNavigation(tabId, url, title) {
  if (!url || !url.startsWith('http')) return;

  const now = Date.now();
  if (now - lastNavigationTime < 2000) {
    // Debounce navigations under 2 seconds
    return;
  }
  lastNavigationTime = now;

  const data = await chrome.storage.local.get(['token', 'sessionId', 'isPaused', 'activityCount']);
  if (!data.token || data.isPaused || !data.sessionId) return;

  let domain = '';
  try {
    domain = new URL(url).hostname;
  } catch (e) {
    return;
  }

  // Close previous activity if one exists
  if (currentActivity && currentActivity.activityId) {
    currentActivity.endedAt = new Date().toISOString();
  }

  // Create new activity
  const newActivity = await apiRequest('/api/activities', 'POST', {
    sessionId: data.sessionId,
    domain: domain,
    url: url.substring(0, 500),
    pageTitle: (title || '').substring(0, 200),
    startedAt: new Date().toISOString()
  }, data.token);

  if (newActivity && newActivity.id) {
    currentActivity = {
      activityId: newActivity.id,
      tabId: tabId,
      domain: domain,
      url: url,
      startedAt: new Date().toISOString()
    };

    const newCount = (data.activityCount || 0) + 1;
    await chrome.storage.local.set({
      activityCount: newCount,
      lastActivityDomain: domain
    });
  }
}

// Tab activation listener
chrome.tabs.onActivated.addListener(async (activeInfo) => {
  try {
    const tab = await chrome.tabs.get(activeInfo.tabId);
    if (tab && tab.url) {
      await handleNavigation(tab.id, tab.url, tab.title);
    }
  } catch (e) {
    // Tab might have been closed
  }
});

// Tab URL update listener
chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === 'complete' && tab.url) {
    handleNavigation(tabId, tab.url, tab.title);
  }
});

// Listen for evidence from content script or messages from popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'EVIDENCE_COLLECTED') {
    (async () => {
      const data = await chrome.storage.local.get(['token', 'isPaused']);
      if (!data.token || data.isPaused) {
        sendResponse({ status: 'SKIPPED' });
        return;
      }

      const activityId = currentActivity ? currentActivity.activityId : null;
      if (!activityId) {
        sendResponse({ status: 'NO_ACTIVE_ACTIVITY' });
        return;
      }

      const evidencePayload = {
        ...message.payload,
        activityId: activityId
      };

      const result = await apiRequest('/api/classification/evidence', 'POST', evidencePayload, data.token);
      sendResponse({ status: result ? 'SUCCESS' : 'FAILED' });
    })();
    return true; // Keep channel open for async response
  }

  if (message.type === 'INIT_SESSION') {
    initSession().then((sessionId) => {
      sendResponse({ sessionId });
    });
    return true;
  }

  return false;
});

// Extension install / startup lifecycle
chrome.runtime.onStartup.addListener(() => {
  initSession();
});

chrome.runtime.onInstalled.addListener(() => {
  initSession();
});
