/**
 * CRITICAL: never read the value of any input, textarea, or contenteditable.
 * Never send document text. Only booleans and counts.
 *
 * Collector: Metadata Signals (6 fields)
 */

(function () {
  const pageStartTime = Date.now();

  function getVisitCount(hostname) {
    try {
      const key = 'ss_visit_' + hostname;
      const count = (parseInt(sessionStorage.getItem(key), 10) || 0) + 1;
      sessionStorage.setItem(key, count.toString());
      return count;
    } catch (e) {
      return 1;
    }
  }

  function checkKnownAiDomain(hostname, knownDomains) {
    if (!knownDomains || !Array.isArray(knownDomains)) return false;
    const lowerHost = hostname.toLowerCase();
    return knownDomains.some((domain) => lowerHost === domain || lowerHost.endsWith('.' + domain));
  }

  function collectMetadata() {
    const hostname = window.location.hostname || '';
    const pathname = window.location.pathname || '';
    const pathSegments = pathname.split('/').filter(Boolean);
    const knownDomains = (window.CONFIG && window.CONFIG.KNOWN_AI_DOMAINS) || [];

    const elapsedSeconds = Math.max(0, Math.floor((Date.now() - pageStartTime) / 1000));

    return {
      domainLength: hostname.length,
      visitCount: getVisitCount(hostname),
      durationSeconds: elapsedSeconds,
      isKnownAiDomain: checkKnownAiDomain(hostname, knownDomains),
      hourOfDay: new Date().getHours(),
      pathDepth: pathSegments.length
    };
  }

  window.SentinelMetadataCollector = {
    collect: collectMetadata,
    getStartTime: () => pageStartTime
  };
})();
