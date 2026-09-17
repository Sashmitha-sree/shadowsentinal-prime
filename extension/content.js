/**
 * CRITICAL: never read the value of any input, textarea, or contenteditable.
 * Never send document text. Only booleans and counts.
 *
 * Shadow Sentinel Content Script
 */

(function () {
  let evidenceSent = false;
  let collectionInitialized = false;

  function bundleEvidencePayload() {
    const metadata = window.SentinelMetadataCollector ? window.SentinelMetadataCollector.collect() : {};
    const ui = window.SentinelUiCollector ? window.SentinelUiCollector.collect() : {};
    const interaction = window.SentinelInteractionCollector ? window.SentinelInteractionCollector.collect() : {};

    return {
      schemaVersion: 'v1',
      capturedAt: new Date().toISOString(),
      // 6 metadata
      domainLength: metadata.domainLength || 0,
      visitCount: metadata.visitCount || 0,
      durationSeconds: metadata.durationSeconds || 0,
      isKnownAiDomain: !!metadata.isKnownAiDomain,
      hourOfDay: metadata.hourOfDay !== undefined ? metadata.hourOfDay : new Date().getHours(),
      pathDepth: metadata.pathDepth || 0,
      // 7 UI
      chatInterfacePresent: !!ui.chatInterfacePresent,
      promptInputPresent: !!ui.promptInputPresent,
      generateControlPresent: !!ui.generateControlPresent,
      regenerateControlPresent: !!ui.regenerateControlPresent,
      aiTermCount: ui.aiTermCount || 0,
      streamingOutputPresent: !!ui.streamingOutputPresent,
      fileUploadPresent: !!ui.fileUploadPresent,
      // 5 interaction
      promptSubmitCount: interaction.promptSubmitCount || 0,
      generateClickCount: interaction.generateClickCount || 0,
      pasteEventCount: interaction.pasteEventCount || 0,
      copyFromResponseCount: interaction.copyFromResponseCount || 0,
      typedCharCountBucket: interaction.typedCharCountBucket || 0
    };
  }

  function sendEvidence() {
    if (evidenceSent) return;
    evidenceSent = true;

    try {
      const payload = bundleEvidencePayload();
      chrome.runtime.sendMessage({
        type: 'EVIDENCE_COLLECTED',
        payload: payload,
        url: window.location.href,
        domain: window.location.hostname
      }, () => {
        if (chrome.runtime.lastError) {
          // Extension context may be unloaded; ignore gracefully
        }
      });
    } catch (e) {
      // Non-blocking catch
    }
  }

  // Content script runs at document_idle, wait 3 seconds for DOM to hydrate
  setTimeout(() => {
    collectionInitialized = true;

    // Dispatch evidence after 60 seconds of page activity
    setTimeout(() => {
      sendEvidence();
    }, 60000);

  }, 3000);

  // Send evidence on page unload / navigation
  window.addEventListener('pagehide', () => {
    if (collectionInitialized) {
      sendEvidence();
    }
  });

  window.addEventListener('beforeunload', () => {
    if (collectionInitialized) {
      sendEvidence();
    }
  });
})();
