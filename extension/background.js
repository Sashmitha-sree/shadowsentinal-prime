// Shadow Sentinel - MV3 Background Service Worker

chrome.runtime.onInstalled.addListener((details) => {
  console.log('[Shadow Sentinel] Extension installed or updated:', details.reason);
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'PING') {
    sendResponse({ status: 'PONG', timestamp: Date.now() });
  }
  return true;
});
