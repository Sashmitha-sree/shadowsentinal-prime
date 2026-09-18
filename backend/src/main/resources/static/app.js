// Shadow Sentinel Dashboard - Vanilla JS
(function () {
  'use strict';

  // State
  let currentUser = null;
  let activeTab = 'overview';
  const API_BASE = '';

  // DOM Elements
  const loginModal = document.getElementById('login-modal');
  const authTitle = document.getElementById('auth-title');
  const authSubtitle = document.getElementById('auth-subtitle');
  const loginForm = document.getElementById('login-form');
  const loginFeedback = document.getElementById('login-feedback');
  const loginEmail = document.getElementById('login-email');
  const loginPassword = document.getElementById('login-password');
  const loginError = document.getElementById('login-error');
  const showSignupBtn = document.getElementById('show-signup-btn');

  // Signup DOM Elements
  const signupForm = document.getElementById('signup-form');
  const signupEmail = document.getElementById('signup-email');
  const signupPassword = document.getElementById('signup-password');
  const signupConfirmPassword = document.getElementById('signup-confirm-password');
  const signupEmailError = document.getElementById('signup-email-error');
  const signupPasswordError = document.getElementById('signup-password-error');
  const signupConfirmError = document.getElementById('signup-confirm-error');
  const signupError = document.getElementById('signup-error');
  const signupSuccess = document.getElementById('signup-success');
  const signupBtn = document.getElementById('signup-btn');
  const showLoginBtn = document.getElementById('show-login-btn');

  const appShell = document.getElementById('app-shell');
  const tabBar = document.getElementById('tab-bar');
  const mainContent = document.getElementById('main-content');
  const userNoticeView = document.getElementById('user-notice-view');
  const logoutBtn = document.getElementById('logout-btn');
  const userRoleBadge = document.getElementById('user-role-badge');
  const userEmailDisplay = document.getElementById('user-email-display');
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabPanes = document.querySelectorAll('.tab-pane');
  const tabPoliciesBtn = document.getElementById('tab-policies-btn');
  const tabTeamBtn = document.getElementById('tab-team-btn');
  const navAlertCount = document.getElementById('nav-alert-count');

  // Drawer Elements
  const drawer = document.getElementById('activity-drawer');
  const drawerCloseBtn = document.getElementById('drawer-close-btn');

  // Token helper
  function getToken() {
    return sessionStorage.getItem('jwt');
  }

  function setToken(token) {
    sessionStorage.setItem('jwt', token);
  }

  function clearToken() {
    sessionStorage.removeItem('jwt');
  }

  // API Client with Authorization header
  async function apiFetch(endpoint, options = {}) {
    const token = getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...(options.headers || {})
    };

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    if (response.status === 401) {
      clearToken();
      showLogin();
      throw new Error('Unauthorized');
    }

    return response;
  }

  // Init
  async function init() {
    setupEventListeners();
    const token = getToken();
    if (token) {
      try {
        await fetchCurrentUser();
        showApp();
      } catch (err) {
        showLogin();
      }
    } else {
      showLogin();
    }
  }

  function setupEventListeners() {
    // Login form
    loginForm.addEventListener('submit', handleLogin);

    // Signup form
    signupForm.addEventListener('submit', handleSignup);

    // Auth mode toggle buttons
    showSignupBtn.addEventListener('click', switchToSignup);
    showLoginBtn.addEventListener('click', () => switchToLogin());

    // Live validation cleanup on input
    signupEmail.addEventListener('input', () => {
      signupEmailError.classList.add('hidden');
      signupEmailError.textContent = '';
      signupEmail.classList.remove('input-error');
      signupError.classList.add('hidden');
    });

    signupPassword.addEventListener('input', () => {
      signupPasswordError.classList.add('hidden');
      signupPasswordError.textContent = '';
      signupPassword.classList.remove('input-error');
      signupError.classList.add('hidden');
    });

    signupConfirmPassword.addEventListener('input', () => {
      signupConfirmError.classList.add('hidden');
      signupConfirmError.textContent = '';
      signupConfirmPassword.classList.remove('input-error');
      signupError.classList.add('hidden');
    });

    // Logout
    logoutBtn.addEventListener('click', () => {
      clearToken();
      currentUser = null;
      showLogin();
    });

    // Tab switching
    tabButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        const tab = btn.dataset.tab;
        switchTab(tab);
      });
    });

    // Drawer close
    drawerCloseBtn.addEventListener('click', () => {
      drawer.classList.add('hidden');
    });

    drawer.addEventListener('click', (e) => {
      if (e.target === drawer) {
        drawer.classList.add('hidden');
      }
    });

    // Refresh buttons
    document.getElementById('refresh-activities-btn').addEventListener('click', fetchActivities);
    document.getElementById('refresh-alerts-btn').addEventListener('click', fetchAlerts);
    document.getElementById('refresh-policies-btn').addEventListener('click', fetchPolicies);
    const refreshTeamBtn = document.getElementById('refresh-team-btn');
    if (refreshTeamBtn) {
      refreshTeamBtn.addEventListener('click', fetchTeam);
    }

    // Add policy rule form
    const addRuleForm = document.getElementById('add-rule-form');
    if (addRuleForm) {
      addRuleForm.addEventListener('submit', handleAddRule);
    }
  }

  // Auth flow - Login
  async function handleLogin(e) {
    e.preventDefault();
    loginError.classList.add('hidden');
    loginError.textContent = '';
    loginFeedback.classList.add('hidden');
    loginFeedback.textContent = '';

    const email = loginEmail.value.trim();
    const password = loginPassword.value;

    try {
      const resp = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      if (!resp.ok) {
        const errData = await resp.json().catch(() => ({}));
        throw new Error(errData.message || 'Invalid email or password');
      }

      const data = await resp.json();
      setToken(data.token);
      await fetchCurrentUser();
      showApp();
    } catch (err) {
      loginError.textContent = err.message || 'Login failed';
      loginError.classList.remove('hidden');
    }
  }

  // Client-side validation for signup
  function validateSignupForm() {
    let isValid = true;

    // Reset previous inline errors
    signupEmailError.classList.add('hidden');
    signupEmailError.textContent = '';
    signupEmail.classList.remove('input-error');

    signupPasswordError.classList.add('hidden');
    signupPasswordError.textContent = '';
    signupPassword.classList.remove('input-error');

    signupConfirmError.classList.add('hidden');
    signupConfirmError.textContent = '';
    signupConfirmPassword.classList.remove('input-error');

    signupError.classList.add('hidden');
    signupError.textContent = '';

    const email = signupEmail.value.trim();
    const password = signupPassword.value;
    const confirmPassword = signupConfirmPassword.value;

    // 1. Email format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      signupEmailError.textContent = 'Email address is required';
      signupEmailError.classList.remove('hidden');
      signupEmail.classList.add('input-error');
      isValid = false;
    } else if (!emailRegex.test(email)) {
      signupEmailError.textContent = 'Please enter a valid email address';
      signupEmailError.classList.remove('hidden');
      signupEmail.classList.add('input-error');
      isValid = false;
    }

    // 2. Password validation (minimum 8 characters)
    if (!password) {
      signupPasswordError.textContent = 'Password is required';
      signupPasswordError.classList.remove('hidden');
      signupPassword.classList.add('input-error');
      isValid = false;
    } else if (password.length < 8) {
      signupPasswordError.textContent = 'Password must be at least 8 characters long';
      signupPasswordError.classList.remove('hidden');
      signupPassword.classList.add('input-error');
      isValid = false;
    }

    // 3. Confirm password validation (must match password)
    if (!confirmPassword) {
      signupConfirmError.textContent = 'Please confirm your password';
      signupConfirmError.classList.remove('hidden');
      signupConfirmPassword.classList.add('input-error');
      isValid = false;
    } else if (confirmPassword !== password) {
      signupConfirmError.textContent = 'Passwords do not match';
      signupConfirmError.classList.remove('hidden');
      signupConfirmPassword.classList.add('input-error');
      isValid = false;
    }

    return isValid;
  }

  // Auth flow - Signup
  async function handleSignup(e) {
    e.preventDefault();

    if (!validateSignupForm()) {
      return;
    }

    const email = signupEmail.value.trim();
    const password = signupPassword.value;

    signupBtn.disabled = true;
    signupBtn.textContent = 'Creating Account...';
    signupError.classList.add('hidden');
    signupError.textContent = '';
    signupSuccess.classList.add('hidden');
    signupSuccess.textContent = '';

    try {
      const resp = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      if (!resp.ok) {
        const errData = await resp.json().catch(() => ({}));
        const msg = errData.message || (resp.status === 409 ? `User with email '${email}' already exists` : 'Registration failed');
        throw new Error(msg);
      }

      // On success: show a brief success message, then automatically switch back to login form
      signupSuccess.textContent = 'Account created successfully! Switching to sign in...';
      signupSuccess.classList.remove('hidden');

      setTimeout(() => {
        switchToLogin(email, 'Account created successfully! Please enter your password to sign in.');
      }, 1200);

    } catch (err) {
      signupError.textContent = err.message || 'Registration failed';
      signupError.classList.remove('hidden');
    } finally {
      signupBtn.disabled = false;
      signupBtn.textContent = 'Sign Up';
    }
  }

  // Form swapping helpers
  function switchToSignup() {
    loginForm.classList.add('hidden');
    signupForm.classList.remove('hidden');

    authTitle.textContent = 'Create an Account';
    authSubtitle.textContent = 'Sign up for Shadow Sentinel security observability.';

    if (loginEmail.value.trim() && !signupEmail.value.trim()) {
      signupEmail.value = loginEmail.value.trim();
    }

    loginError.classList.add('hidden');
    loginError.textContent = '';
    loginFeedback.classList.add('hidden');
    loginFeedback.textContent = '';

    resetSignupForm(false);
    if (signupEmail.value) {
      signupPassword.focus();
    } else {
      signupEmail.focus();
    }
  }

  function switchToLogin(prefillEmail = '', feedbackMessage = '') {
    signupForm.classList.add('hidden');
    loginForm.classList.remove('hidden');

    authTitle.textContent = 'Enterprise Security Console';
    authSubtitle.textContent = 'Sign in to monitor AI activity, risk assessments, and policies.';

    resetSignupForm(true);

    loginError.classList.add('hidden');
    loginError.textContent = '';

    if (feedbackMessage) {
      loginFeedback.textContent = feedbackMessage;
      loginFeedback.classList.remove('hidden');
    } else {
      loginFeedback.classList.add('hidden');
      loginFeedback.textContent = '';
    }

    if (prefillEmail) {
      loginEmail.value = prefillEmail;
      loginPassword.value = '';
      loginPassword.focus();
    } else {
      loginEmail.focus();
    }
  }

  function resetSignupForm(clearInputs = true) {
    if (clearInputs) {
      signupEmail.value = '';
      signupPassword.value = '';
      signupConfirmPassword.value = '';
    }
    signupEmailError.classList.add('hidden');
    signupEmailError.textContent = '';
    signupEmail.classList.remove('input-error');

    signupPasswordError.classList.add('hidden');
    signupPasswordError.textContent = '';
    signupPassword.classList.remove('input-error');

    signupConfirmError.classList.add('hidden');
    signupConfirmError.textContent = '';
    signupConfirmPassword.classList.remove('input-error');

    signupError.classList.add('hidden');
    signupError.textContent = '';
    signupSuccess.classList.add('hidden');
    signupSuccess.textContent = '';
  }

  async function fetchCurrentUser() {
    const resp = await apiFetch('/api/auth/me');
    if (!resp.ok) throw new Error('Failed to load user profile');
    currentUser = await resp.json();

    userEmailDisplay.textContent = currentUser.email;
    userRoleBadge.textContent = currentUser.role;
    userRoleBadge.className = 'badge ' + (currentUser.role === 'ADMIN' ? 'badge-critical' : 'badge-capable');

    // Admin-only tab visibility
    if (currentUser.role === 'ADMIN') {
      tabPoliciesBtn.classList.remove('hidden');
      if (tabTeamBtn) tabTeamBtn.classList.remove('hidden');
    } else {
      tabPoliciesBtn.classList.add('hidden');
      if (tabTeamBtn) tabTeamBtn.classList.add('hidden');
    }
  }

  function showLogin() {
    switchToLogin();
    loginModal.classList.remove('hidden');
    appShell.classList.add('hidden');
    drawer.classList.add('hidden');
  }

  function showApp() {
    loginModal.classList.add('hidden');
    appShell.classList.remove('hidden');

    if (currentUser && currentUser.role === 'ADMIN') {
      if (userNoticeView) userNoticeView.classList.add('hidden');
      if (tabBar) tabBar.classList.remove('hidden');
      if (mainContent) mainContent.classList.remove('hidden');
      switchTab(activeTab);
    } else {
      // Non-admin user: hide dashboard tabs and main content, show extension notice only
      if (tabBar) tabBar.classList.add('hidden');
      if (mainContent) mainContent.classList.add('hidden');
      if (userNoticeView) userNoticeView.classList.remove('hidden');
    }
  }

  function switchTab(tabId) {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return;
    }

    activeTab = tabId;

    tabButtons.forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tab === tabId);
    });

    tabPanes.forEach(pane => {
      pane.classList.toggle('active', pane.id === `tab-${tabId}`);
    });

    if (tabId === 'overview') {
      fetchOverviewData();
    } else if (tabId === 'activities') {
      fetchActivities();
    } else if (tabId === 'alerts') {
      fetchAlerts();
    } else if (tabId === 'policies') {
      fetchPolicies();
    } else if (tabId === 'team') {
      fetchTeam();
    }
  }

  // TAB 1: Overview
  async function fetchOverviewData() {
    try {
      const [actResp, classResp, riskResp, alertResp] = await Promise.all([
        apiFetch('/api/activities?size=100'),
        apiFetch('/api/classification/results?size=100'),
        apiFetch('/api/risk?size=100'),
        apiFetch('/api/alerts?size=100')
      ]);

      const activities = actResp.ok ? (await actResp.json()).content || [] : [];
      const classifications = classResp.ok ? (await classResp.json()).content || [] : [];
      const risks = riskResp.ok ? (await riskResp.json()).content || [] : [];
      const alerts = alertResp.ok ? (await alertResp.json()).content || [] : [];

      // Stats
      document.getElementById('stat-total-activities').textContent = activities.length;

      const aiCount = classifications.filter(c => c.classLabel !== 'NON_AI').length;
      document.getElementById('stat-ai-activities').textContent = aiCount;

      const highCriticalCount = risks.filter(r => r.riskLevel === 'HIGH' || r.riskLevel === 'CRITICAL').length;
      document.getElementById('stat-high-risks').textContent = highCriticalCount;

      const unresolvedAlerts = alerts.filter(a => a.status !== 'RESOLVED').length;
      document.getElementById('stat-active-alerts').textContent = unresolvedAlerts;

      // Update Nav Badge
      if (unresolvedAlerts > 0) {
        navAlertCount.textContent = unresolvedAlerts;
        navAlertCount.classList.remove('hidden');
      } else {
        navAlertCount.classList.add('hidden');
      }

      // Render Classification Chart
      renderClassificationChart(classifications);

      // Render Risk Distribution Chart
      renderRiskChart(risks);

    } catch (err) {
      console.error('Failed to load overview data:', err);
    }
  }

  function renderClassificationChart(classifications) {
    const container = document.getElementById('chart-class-distribution');
    const total = classifications.length;

    if (total === 0) {
      container.innerHTML = '<div class="chart-empty">No classification telemetry recorded yet.</div>';
      return;
    }

    const counts = {
      NON_AI: 0,
      AI_CAPABLE_PAGE: 0,
      AI_INTERACTION: 0,
      AI_GENERATION: 0
    };

    classifications.forEach(c => {
      if (counts[c.classLabel] !== undefined) counts[c.classLabel]++;
    });

    const labels = [
      { key: 'NON_AI', name: 'Non-AI Browsing', color: 'var(--label-non-ai)' },
      { key: 'AI_CAPABLE_PAGE', name: 'AI Capable Page', color: 'var(--label-capable)' },
      { key: 'AI_INTERACTION', name: 'AI Interaction', color: 'var(--label-interaction)' },
      { key: 'AI_GENERATION', name: 'AI Generation & Consumption', color: 'var(--label-generation)' }
    ];

    container.innerHTML = labels.map(item => {
      const count = counts[item.key];
      const pct = Math.round((count / total) * 100);
      return `
        <div class="chart-bar-row">
          <div class="chart-bar-meta">
            <span class="chart-bar-label">${item.name}</span>
            <span class="chart-bar-val">${count} (${pct}%)</span>
          </div>
          <div class="chart-bar-track">
            <div class="chart-bar-fill" style="width: ${pct}%; background-color: ${item.color};"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  function renderRiskChart(risks) {
    const container = document.getElementById('chart-risk-distribution');
    const total = risks.length;

    if (total === 0) {
      container.innerHTML = '<div class="chart-empty">No risk assessments evaluated yet.</div>';
      return;
    }

    const counts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
    risks.forEach(r => {
      if (counts[r.riskLevel] !== undefined) counts[r.riskLevel]++;
    });

    const levels = [
      { key: 'LOW', name: 'Low Risk', color: 'var(--severity-low)' },
      { key: 'MEDIUM', name: 'Medium Risk', color: 'var(--severity-medium)' },
      { key: 'HIGH', name: 'High Risk', color: 'var(--severity-high)' },
      { key: 'CRITICAL', name: 'Critical Risk', color: 'var(--severity-critical)' }
    ];

    container.innerHTML = levels.map(item => {
      const count = counts[item.key];
      const pct = Math.round((count / total) * 100);
      return `
        <div class="chart-bar-row">
          <div class="chart-bar-meta">
            <span class="chart-bar-label">${item.name}</span>
            <span class="chart-bar-val">${count} (${pct}%)</span>
          </div>
          <div class="chart-bar-track">
            <div class="chart-bar-fill" style="width: ${pct}%; background-color: ${item.color};"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // TAB 2: Activities
  async function fetchActivities() {
    const tbody = document.getElementById('activities-table-body');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center">Loading browser activities...</td></tr>';

    try {
      const [actResp, classResp, riskResp] = await Promise.all([
        apiFetch('/api/activities?size=50'),
        apiFetch('/api/classification/results?size=50'),
        apiFetch('/api/risk?size=50')
      ]);

      const activities = actResp.ok ? (await actResp.json()).content || [] : [];
      const classifications = classResp.ok ? (await classResp.json()).content || [] : [];
      const risks = riskResp.ok ? (await riskResp.json()).content || [] : [];

      const classMap = new Map(classifications.map(c => [c.activityId, c]));
      const riskMap = new Map(risks.map(r => [r.activityId, r]));

      if (activities.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No activities captured yet.</td></tr>';
        return;
      }

      tbody.innerHTML = activities.map(act => {
        const cls = classMap.get(act.id);
        const rsk = riskMap.get(act.id);

        const classBadge = cls ? `<span class="badge ${getClassBadgeClass(cls.classLabel)}">${cls.classLabel}</span>` : '<span class="text-muted">-</span>';
        const confidenceText = cls ? `${Math.round(cls.confidence * 100)}%` : '-';
        const riskBadge = rsk ? `<span class="badge ${getRiskBadgeClass(rsk.riskLevel)}">${rsk.riskLevel} (${rsk.riskScore})</span>` : '<span class="text-muted">-</span>';
        const timeFormatted = act.startedAt ? new Date(act.startedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '-';

        return `
          <tr class="clickable" data-activity-id="${act.id}" data-domain="${escapeHtml(act.domain)}" data-title="${escapeHtml(act.pageTitle || '')}">
            <td><strong>${escapeHtml(act.domain)}</strong></td>
            <td class="text-muted text-sm">${escapeHtml(act.pageTitle || 'Untitled Page')}</td>
            <td class="text-muted text-sm">${timeFormatted}</td>
            <td>${classBadge}</td>
            <td>${confidenceText}</td>
            <td>${riskBadge}</td>
            <td><button class="btn btn-secondary btn-sm inspect-btn" data-activity-id="${act.id}">Inspect</button></td>
          </tr>
        `;
      }).join('');

      // Add click handlers
      tbody.querySelectorAll('tr.clickable').forEach(row => {
        row.addEventListener('click', (e) => {
          const actId = row.dataset.activityId;
          const domain = row.dataset.domain;
          const title = row.dataset.title;
          openActivityDrawer(actId, domain, title);
        });
      });

    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Error loading activities: ${err.message}</td></tr>`;
    }
  }

  // Side-Panel Drawer: 18 Evidence Fields & Explainability
  async function openActivityDrawer(activityId, domain, title) {
    document.getElementById('drawer-domain').textContent = domain || 'Activity Details';
    document.getElementById('drawer-activity-id').textContent = `Activity #${activityId} • ${title || ''}`;

    // Reset fields
    document.getElementById('drawer-risk-score').textContent = '-';
    document.getElementById('drawer-risk-level').textContent = '-';
    document.getElementById('drawer-risk-level').className = 'badge';
    document.getElementById('drawer-matched-rules').textContent = 'Loading...';
    document.getElementById('drawer-reasoning').textContent = 'Loading assessment...';

    const evFields = [
      'domainLength', 'visitCount', 'durationSeconds', 'isKnownAiDomain', 'hourOfDay', 'pathDepth',
      'chatInterfacePresent', 'promptInputPresent', 'generateControlPresent', 'regenerateControlPresent',
      'aiTermCount', 'streamingOutputPresent', 'fileUploadPresent',
      'promptSubmitCount', 'generateClickCount', 'pasteEventCount', 'copyFromResponseCount', 'typedCharCountBucket'
    ];
    evFields.forEach(f => {
      const el = document.getElementById(`ev-${f}`);
      if (el) el.textContent = '...';
    });

    drawer.classList.remove('hidden');

    try {
      const [evResp, rskResp] = await Promise.all([
        apiFetch(`/api/classification/evidence/${activityId}`),
        apiFetch(`/api/risk/${activityId}`)
      ]);

      if (evResp.ok) {
        const ev = await evResp.json();
        evFields.forEach(f => {
          const el = document.getElementById(`ev-${f}`);
          if (el) {
            let val = ev[f];
            if (typeof val === 'boolean') {
              el.textContent = val ? 'Yes' : 'No';
              el.style.color = val ? 'var(--accent)' : 'var(--text-muted)';
            } else {
              el.textContent = val !== undefined ? val : '-';
              el.style.color = 'var(--text-primary)';
            }
          }
        });
      }

      if (rskResp.ok) {
        const rsk = await rskResp.json();
        document.getElementById('drawer-risk-score').textContent = rsk.riskScore;
        const levelBadge = document.getElementById('drawer-risk-level');
        levelBadge.textContent = rsk.riskLevel;
        levelBadge.className = `badge ${getRiskBadgeClass(rsk.riskLevel)}`;
        document.getElementById('drawer-matched-rules').textContent = rsk.matchedRuleIds || 'None';
        document.getElementById('drawer-reasoning').textContent = rsk.reasoning || 'No reasoning available.';
      } else {
        document.getElementById('drawer-reasoning').textContent = 'No risk assessment triggered for this activity.';
      }

    } catch (err) {
      console.error('Failed to load activity details:', err);
    }
  }

  // TAB 3: Alerts
  async function fetchAlerts() {
    const container = document.getElementById('alerts-list-container');
    container.innerHTML = '<div class="chart-empty">Loading alerts...</div>';

    const statusFilter = document.getElementById('filter-alert-status').value;
    const severityFilter = document.getElementById('filter-alert-severity').value;

    let query = '/api/alerts?size=50';
    if (statusFilter) query += `&status=${encodeURIComponent(statusFilter)}`;
    if (severityFilter) query += `&severity=${encodeURIComponent(severityFilter)}`;

    try {
      const resp = await apiFetch(query);
      if (!resp.ok) throw new Error('Failed to fetch alerts');

      const data = await resp.json();
      const alerts = data.content || [];

      if (alerts.length === 0) {
        container.innerHTML = '<div class="chart-empty">No alerts match the selected criteria.</div>';
        return;
      }

      container.innerHTML = alerts.map(alert => {
        const sevClass = (alert.severity || '').toLowerCase();
        const timeStr = alert.createdAt ? new Date(alert.createdAt).toLocaleString() : '-';

        return `
          <div class="alert-card ${sevClass}" data-id="${alert.id}">
            <div class="alert-header">
              <div class="alert-badges">
                <span class="badge ${getRiskBadgeClass(alert.severity)}">${alert.severity}</span>
                <span class="badge">${alert.status}</span>
                ${alert.occurrenceCount > 1 ? `<span class="badge-count">${alert.occurrenceCount}x occurred</span>` : ''}
              </div>
              <span class="alert-time">${timeStr}</span>
            </div>
            <div class="alert-title">${escapeHtml(alert.title)}</div>
            <div class="alert-message">${escapeHtml(alert.message)}</div>
            <div class="alert-footer">
              <span class="alert-meta">Domain: <strong>${escapeHtml(alert.domain || '-')}</strong></span>
              <div class="alert-actions">
                ${alert.status === 'NEW' ? `
                  <button class="btn btn-secondary btn-sm ack-btn" data-id="${alert.id}">Acknowledge</button>
                  <button class="btn btn-primary btn-sm resolve-btn" data-id="${alert.id}">Resolve</button>
                ` : alert.status === 'ACKNOWLEDGED' ? `
                  <button class="btn btn-primary btn-sm resolve-btn" data-id="${alert.id}">Resolve</button>
                ` : `
                  <span class="badge badge-low">Resolved</span>
                `}
              </div>
            </div>
          </div>
        `;
      }).join('');

      // Acknowledge click handlers
      container.querySelectorAll('.ack-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
          e.stopPropagation();
          const id = btn.dataset.id;
          await acknowledgeAlert(id);
        });
      });

      // Resolve click handlers
      container.querySelectorAll('.resolve-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
          e.stopPropagation();
          const id = btn.dataset.id;
          await resolveAlert(id);
        });
      });

    } catch (err) {
      container.innerHTML = `<div class="chart-empty text-danger">Error loading alerts: ${err.message}</div>`;
    }
  }

  async function acknowledgeAlert(alertId) {
    try {
      const resp = await apiFetch(`/api/alerts/${alertId}/acknowledge`, { method: 'PATCH' });
      if (resp.ok) {
        fetchAlerts();
        fetchOverviewData();
      }
    } catch (err) {
      alert('Failed to acknowledge alert: ' + err.message);
    }
  }

  async function resolveAlert(alertId) {
    try {
      const resp = await apiFetch(`/api/alerts/${alertId}/resolve`, { method: 'PATCH' });
      if (resp.ok) {
        fetchAlerts();
        fetchOverviewData();
      }
    } catch (err) {
      alert('Failed to resolve alert: ' + err.message);
    }
  }

  // TAB 4: Policies (ADMIN Only)
  async function fetchPolicies() {
    const nonAdminMsg = document.getElementById('policy-non-admin-msg');
    const adminContent = document.getElementById('policy-admin-content');
    const container = document.getElementById('policies-container');

    if (currentUser.role !== 'ADMIN') {
      nonAdminMsg.classList.remove('hidden');
      adminContent.classList.add('hidden');
      return;
    }

    nonAdminMsg.classList.add('hidden');
    adminContent.classList.remove('hidden');
    container.innerHTML = '<div class="chart-empty">Loading active corporate policies...</div>';

    try {
      const resp = await apiFetch('/api/policies');
      if (!resp.ok) throw new Error('Failed to load policies');

      const policies = await resp.json();
      if (policies.length === 0) {
        container.innerHTML = '<div class="chart-empty">No active corporate policy found.</div>';
        return;
      }

      const activePolicy = policies[0];
      document.getElementById('policy-name-header').textContent = `${activePolicy.name} (v${activePolicy.version})`;

      const rules = activePolicy.rules || [];
      if (rules.length === 0) {
        container.innerHTML = '<div class="chart-empty">Policy contains no rules.</div>';
        return;
      }

      container.innerHTML = rules.map(r => {
        const conditions = [];
        if (r.appliesToLabel) conditions.push(`Label: <strong>${r.appliesToLabel}</strong>`);
        if (r.domainPattern) conditions.push(`Domain: <strong>${escapeHtml(r.domainPattern)}</strong>`);
        if (r.minGenerateClicks) conditions.push(`Min Clicks: <strong>${r.minGenerateClicks}</strong>`);
        if (r.requiresFileUpload !== null && r.requiresFileUpload !== undefined) {
          conditions.push(`File Upload: <strong>${r.requiresFileUpload ? 'Required' : 'Forbidden'}</strong>`);
        }
        if (r.requiresPasteEvent !== null && r.requiresPasteEvent !== undefined) {
          conditions.push(`Paste: <strong>${r.requiresPasteEvent ? 'Required' : 'Forbidden'}</strong>`);
        }
        const condText = conditions.length > 0 ? conditions.join(' • ') : 'Applies unconditionally';

        return `
          <div class="rule-item">
            <div class="rule-item-header">
              <span class="rule-key">${escapeHtml(r.ruleKey)}</span>
              <div style="display:flex; gap:6px; align-items:center;">
                <span class="badge ${getRiskBadgeClass(r.severity)}">${r.severity}</span>
                <span class="badge">+${r.scoreWeight} pts</span>
              </div>
            </div>
            <div class="rule-desc">${escapeHtml(r.description)}</div>
            <div class="rule-conditions">${condText}</div>
          </div>
        `;
      }).join('');

    } catch (err) {
      container.innerHTML = `<div class="chart-empty text-danger">Error loading policies: ${err.message}</div>`;
    }
  }

  async function handleAddRule(e) {
    e.preventDefault();
    const feedback = document.getElementById('rule-form-feedback');
    feedback.className = 'form-feedback hidden';

    const ruleKey = document.getElementById('rule-key').value.trim();
    const description = document.getElementById('rule-description').value.trim();
    const severity = document.getElementById('rule-severity').value;
    const scoreWeight = parseInt(document.getElementById('rule-weight').value, 10);
    const appliesToLabel = document.getElementById('rule-label').value || null;
    const domainPattern = document.getElementById('rule-domain').value.trim() || null;

    const clicksVal = document.getElementById('rule-clicks').value.trim();
    const minGenerateClicks = clicksVal ? parseInt(clicksVal, 10) : null;

    const fileVal = document.getElementById('rule-file-upload').value;
    const requiresFileUpload = fileVal === 'true' ? true : fileVal === 'false' ? false : null;

    const pasteVal = document.getElementById('rule-paste').value;
    const requiresPasteEvent = pasteVal === 'true' ? true : pasteVal === 'false' ? false : null;

    const payload = {
      ruleKey,
      description,
      severity,
      scoreWeight,
      appliesToLabel,
      domainPattern,
      minGenerateClicks,
      requiresFileUpload,
      requiresPasteEvent
    };

    try {
      const resp = await apiFetch('/api/policies/rules', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      if (!resp.ok) {
        const errData = await resp.json().catch(() => ({}));
        throw new Error(errData.message || 'Failed to create rule');
      }

      feedback.textContent = `Rule "${ruleKey}" successfully added! Policy version incremented.`;
      feedback.className = 'form-feedback success';
      document.getElementById('add-rule-form').reset();
      fetchPolicies();
    } catch (err) {
      feedback.textContent = err.message;
      feedback.className = 'form-feedback error';
    }
  }

  // Helpers
  function getClassBadgeClass(label) {
    switch (label) {
      case 'NON_AI': return 'badge-non-ai';
      case 'AI_CAPABLE_PAGE': return 'badge-ai-capable';
      case 'AI_INTERACTION': return 'badge-ai-interaction';
      case 'AI_GENERATION': return 'badge-ai-generation';
      default: return '';
    }
  }

  function getRiskBadgeClass(level) {
    switch (level) {
      case 'LOW': return 'badge-low';
      case 'MEDIUM': return 'badge-medium';
      case 'HIGH': return 'badge-high';
      case 'CRITICAL': return 'badge-critical';
      default: return '';
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // TAB 5: Team (ADMIN)
  async function fetchTeam() {
    if (!currentUser || currentUser.role !== 'ADMIN') return;
    const tbody = document.getElementById('team-table-body');
    tbody.innerHTML = '<tr><td colspan="3" class="text-center">Loading team members...</td></tr>';

    try {
      const resp = await apiFetch('/api/users');
      if (!resp.ok) {
        throw new Error('Failed to load team members');
      }
      const users = await resp.json();

      if (!Array.isArray(users) || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No employee accounts found.</td></tr>';
        return;
      }

      // Sort online users to the top, then by most recent lastSeenAt, then by email
      users.sort((a, b) => {
        const onlineA = !!a.online;
        const onlineB = !!b.online;
        if (onlineA !== onlineB) {
          return onlineA ? -1 : 1;
        }
        const timeA = a.lastSeenAt ? new Date(a.lastSeenAt).getTime() : 0;
        const timeB = b.lastSeenAt ? new Date(b.lastSeenAt).getTime() : 0;
        if (timeA !== timeB) {
          return timeB - timeA;
        }
        return (a.email || '').localeCompare(b.email || '');
      });

      tbody.innerHTML = users.map(user => {
        const statusBadge = user.online
          ? '<span class="badge badge-online">Online</span>'
          : '<span class="badge badge-offline">Offline</span>';
        const relativeTime = formatRelativeTime(user.lastSeenAt);

        return `
          <tr>
            <td><strong>${escapeHtml(user.email)}</strong></td>
            <td>${statusBadge}</td>
            <td class="text-muted text-sm">${escapeHtml(relativeTime)}</td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Failed to load team members.</td></tr>';
    }
  }

  function formatRelativeTime(isoString) {
    if (!isoString) return 'Never';
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return 'Never';
    const now = new Date();
    const diffSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffSeconds < 0) return 'Just now';
    if (diffSeconds < 60) return `${diffSeconds <= 10 ? 'Just now' : diffSeconds + ' seconds ago'}`;

    const diffMinutes = Math.floor(diffSeconds / 60);
    if (diffMinutes === 1) return '1 minute ago';
    if (diffMinutes < 60) return `${diffMinutes} minutes ago`;

    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours === 1) return '1 hour ago';
    if (diffHours < 24) return `${diffHours} hours ago`;

    const diffDays = Math.floor(diffHours / 24);
    if (diffDays === 1) return '1 day ago';
    if (diffDays < 30) return `${diffDays} days ago`;

    return date.toLocaleDateString();
  }

  // Start app
  document.addEventListener('DOMContentLoaded', init);
})();
