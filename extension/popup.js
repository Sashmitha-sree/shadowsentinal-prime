// Shadow Sentinel - Popup Controller

document.addEventListener('DOMContentLoaded', async () => {
  const loginSection = document.getElementById('login-section');
  const dashboardSection = document.getElementById('dashboard-section');
  const loginForm = document.getElementById('login-form');
  const emailInput = document.getElementById('email-input');
  const passwordInput = document.getElementById('password-input');
  const loginBtn = document.getElementById('login-btn');
  const loginError = document.getElementById('login-error');

  const sessionStatusBadge = document.getElementById('session-status-badge');
  const sessionStatusText = document.getElementById('session-status-text');
  const activityCountEl = document.getElementById('activity-count');
  const sessionIdDisplay = document.getElementById('session-id-display');
  const pauseToggle = document.getElementById('pause-toggle');
  const logoutBtn = document.getElementById('logout-btn');

  const apiBase = (window.CONFIG && window.CONFIG.API_BASE_URL) || 'http://localhost:8080';

  async function updateUI() {
    const data = await chrome.storage.local.get([
      'token',
      'sessionId',
      'sessionStatus',
      'activityCount',
      'isPaused'
    ]);

    if (!data.token) {
      loginSection.classList.remove('hidden');
      dashboardSection.classList.add('hidden');
      return;
    }

    loginSection.classList.add('hidden');
    dashboardSection.classList.remove('hidden');

    activityCountEl.textContent = data.activityCount !== undefined ? data.activityCount : 0;
    sessionIdDisplay.textContent = data.sessionId ? `#${data.sessionId}` : '--';
    pauseToggle.checked = !!data.isPaused;

    if (data.isPaused) {
      sessionStatusText.textContent = 'Paused';
      sessionStatusBadge.className = 'status-badge paused';
    } else {
      sessionStatusText.textContent = data.sessionStatus || 'Active';
      sessionStatusBadge.className = 'status-badge';
    }
  }

  // Handle Login
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginError.classList.add('hidden');
    loginBtn.disabled = true;
    loginBtn.textContent = 'Authenticating...';

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    try {
      const response = await fetch(`${apiBase}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (response.ok) {
        const result = await response.json();
        await chrome.storage.local.set({
          token: result.token,
          userEmail: email,
          isPaused: false
        });

        // Prompt background to start a new session
        chrome.runtime.sendMessage({ type: 'INIT_SESSION' }, () => {
          loginBtn.disabled = false;
          loginBtn.textContent = 'Login';
          emailInput.value = '';
          passwordInput.value = '';
          updateUI();
        });
      } else {
        const errorData = await response.json().catch(() => ({}));
        loginError.textContent = errorData.message || 'Login failed. Please check your credentials.';
        loginError.classList.remove('hidden');
        loginBtn.disabled = false;
        loginBtn.textContent = 'Login';
      }
    } catch (err) {
      loginError.textContent = 'Cannot connect to Sentinel API. Ensure backend is running.';
      loginError.classList.remove('hidden');
      loginBtn.disabled = false;
      loginBtn.textContent = 'Login';
    }
  });

  // Handle Pause Toggle
  pauseToggle.addEventListener('change', async () => {
    const isPaused = pauseToggle.checked;
    await chrome.storage.local.set({ isPaused });
    updateUI();
  });

  // Handle Logout
  logoutBtn.addEventListener('click', async () => {
    await chrome.storage.local.remove([
      'token',
      'sessionId',
      'sessionStatus',
      'activityCount',
      'userEmail'
    ]);
    updateUI();
  });

  // Listen for storage changes from background worker
  chrome.storage.onChanged.addListener((changes, area) => {
    if (area === 'local') {
      updateUI();
    }
  });

  // Initial render
  updateUI();
});
