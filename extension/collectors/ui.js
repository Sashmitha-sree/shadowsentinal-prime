/**
 * CRITICAL: never read the value of any input, textarea, or contenteditable.
 * Never send document text. Only booleans and counts.
 *
 * Collector: UI Signals (7 fields)
 */

(function () {
  function hasChatInterface() {
    const chatSelectors = [
      '[role="log"]',
      '[data-testid*="conversation" i]',
      '[data-testid*="chat" i]',
      '[class*="conversation" i]',
      '[class*="chat-window" i]',
      '[class*="chat-message" i]',
      '[class*="message-list" i]',
      '#chat-container',
      '.chat-history'
    ];
    return chatSelectors.some((sel) => !!document.querySelector(sel));
  }

  function hasPromptInput() {
    const promptSelectors = [
      'textarea',
      '[contenteditable="true"]',
      'input[type="text"][placeholder*="message" i]',
      'input[type="text"][placeholder*="prompt" i]',
      'input[type="text"][placeholder*="ask" i]',
      '[data-placeholder*="message" i]',
      '[data-placeholder*="ask" i]',
      '#prompt-textarea'
    ];
    return promptSelectors.some((sel) => !!document.querySelector(sel));
  }

  function hasGenerateControl() {
    const buttons = document.querySelectorAll('button, [role="button"]');
    for (const btn of buttons) {
      const aria = (btn.getAttribute('aria-label') || '').toLowerCase();
      const text = (btn.textContent || '').trim().toLowerCase();
      const testid = (btn.getAttribute('data-testid') || '').toLowerCase();
      if (
        aria.includes('send') ||
        aria.includes('generate') ||
        aria.includes('submit') ||
        aria.includes('ask') ||
        testid.includes('send') ||
        text === 'send' ||
        text === 'generate' ||
        text === 'ask' ||
        text === 'submit'
      ) {
        return true;
      }
    }
    return false;
  }

  function hasRegenerateControl() {
    const buttons = document.querySelectorAll('button, [role="button"]');
    for (const btn of buttons) {
      const aria = (btn.getAttribute('aria-label') || '').toLowerCase();
      const text = (btn.textContent || '').trim().toLowerCase();
      const testid = (btn.getAttribute('data-testid') || '').toLowerCase();
      if (
        aria.includes('regenerate') ||
        aria.includes('retry') ||
        testid.includes('regenerate') ||
        text.includes('regenerate') ||
        text.includes('try again') ||
        text === 'retry'
      ) {
        return true;
      }
    }
    return false;
  }

  function hasStreamingOutput() {
    const streamingSelectors = [
      '.result-streaming',
      '[class*="streaming" i]',
      '[class*="result-stream" i]',
      '[data-state="streaming" i]',
      '[class*="cursor-blink" i]',
      '.animate-pulse'
    ];
    return streamingSelectors.some((sel) => !!document.querySelector(sel));
  }

  function hasFileUpload() {
    if (document.querySelector('input[type="file"]')) {
      return true;
    }
    const buttons = document.querySelectorAll('button, [role="button"]');
    for (const btn of buttons) {
      const aria = (btn.getAttribute('aria-label') || '').toLowerCase();
      const testid = (btn.getAttribute('data-testid') || '').toLowerCase();
      if (
        aria.includes('attach') ||
        aria.includes('upload') ||
        aria.includes('file') ||
        testid.includes('upload') ||
        testid.includes('attach')
      ) {
        return true;
      }
    }
    return false;
  }

  function countAiTerms() {
    const keywords = (window.CONFIG && window.CONFIG.AI_KEYWORDS) || [
      'model', 'prompt', 'token', 'temperature', 'chatgpt', 'claude', 'gemini', 'llm'
    ];
    let count = 0;

    // Sample structural metadata attributes and headings only (NO body/document text extraction)
    const candidates = document.querySelectorAll('h1, h2, h3, button, [role="button"], [aria-label], placeholder');
    candidates.forEach((el) => {
      const label = (el.getAttribute('aria-label') || el.getAttribute('placeholder') || el.textContent || '').toLowerCase();
      keywords.forEach((kw) => {
        if (label.includes(kw)) {
          count++;
        }
      });
    });

    return count;
  }

  function collectUiSignals() {
    return {
      chatInterfacePresent: hasChatInterface(),
      promptInputPresent: hasPromptInput(),
      generateControlPresent: hasGenerateControl(),
      regenerateControlPresent: hasRegenerateControl(),
      aiTermCount: countAiTerms(),
      streamingOutputPresent: hasStreamingOutput(),
      fileUploadPresent: hasFileUpload()
    };
  }

  window.SentinelUiCollector = {
    collect: collectUiSignals
  };
})();
