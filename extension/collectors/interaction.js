/**
 * CRITICAL: never read the value of any input, textarea, or contenteditable.
 * Never send document text. Only booleans and counts.
 *
 * Collector: Interaction Signals (5 fields)
 */

(function () {
  let promptSubmitCount = 0;
  let generateClickCount = 0;
  let pasteEventCount = 0;
  let copyFromResponseCount = 0;
  let rawTypedCharCount = 0;

  function isInputElement(el) {
    if (!el) return false;
    const tag = (el.tagName || '').toLowerCase();
    return tag === 'textarea' || tag === 'input' || el.isContentEditable;
  }

  function isGenerateButton(el) {
    if (!el) return false;
    const btn = el.closest('button, [role="button"]');
    if (!btn) return false;
    const aria = (btn.getAttribute('aria-label') || '').toLowerCase();
    const testid = (btn.getAttribute('data-testid') || '').toLowerCase();
    const text = (btn.textContent || '').trim().toLowerCase();
    return (
      aria.includes('send') ||
      aria.includes('generate') ||
      aria.includes('submit') ||
      aria.includes('ask') ||
      testid.includes('send') ||
      text === 'send' ||
      text === 'generate' ||
      text === 'ask'
    );
  }

  // Keydown listener for typed chars and submit intent
  document.addEventListener('keydown', (e) => {
    if (isInputElement(e.target)) {
      // Track length increment only — never capture key or character value
      if (e.key && e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
        rawTypedCharCount++;
      }

      // Enter without Shift indicates prompt submit in chat interfaces
      if (e.key === 'Enter' && !e.shiftKey) {
        promptSubmitCount++;
      }
    }
  }, { capture: true, passive: true });

  // Click listener for generate / send buttons
  document.addEventListener('click', (e) => {
    if (isGenerateButton(e.target)) {
      generateClickCount++;
      promptSubmitCount++;
    }
  }, { capture: true, passive: true });

  // Paste listener (counts paste frequency, never reads clipboard content)
  document.addEventListener('paste', (e) => {
    if (isInputElement(e.target)) {
      pasteEventCount++;
    }
  }, { capture: true, passive: true });

  // Copy listener (counts response copy events when not copying from input fields)
  document.addEventListener('copy', (e) => {
    if (!isInputElement(e.target)) {
      copyFromResponseCount++;
    }
  }, { capture: true, passive: true });

  function getTypedCharBucket(count) {
    if (count <= 0) return 0;
    if (count <= 50) return 1;
    if (count <= 200) return 2;
    if (count <= 1000) return 3;
    return 4;
  }

  function collectInteractionSignals() {
    return {
      promptSubmitCount,
      generateClickCount,
      pasteEventCount,
      copyFromResponseCount,
      typedCharCountBucket: getTypedCharBucket(rawTypedCharCount)
    };
  }

  window.SentinelInteractionCollector = {
    collect: collectInteractionSignals,
    getRawTypedCount: () => rawTypedCharCount
  };
})();
