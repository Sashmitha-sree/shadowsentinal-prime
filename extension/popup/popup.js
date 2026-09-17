document.addEventListener('DOMContentLoaded', () => {
  const checkBtn = document.getElementById('check-btn');
  const statusText = document.getElementById('status-text');

  checkBtn.addEventListener('click', () => {
    chrome.runtime.sendMessage({ type: 'PING' }, (response) => {
      if (chrome.runtime.lastError) {
        statusText.textContent = 'Service worker disconnected';
        return;
      }
      if (response && response.status === 'PONG') {
        statusText.textContent = 'Active (Ping OK)';
      }
    });
  });
});
