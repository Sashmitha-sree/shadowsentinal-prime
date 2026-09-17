// Shadow Sentinel - Extension Configuration

const CONFIG = {
  API_BASE_URL: 'http://localhost:8080',
  KNOWN_AI_DOMAINS: [
    'chatgpt.com',
    'openai.com',
    'claude.ai',
    'anthropic.com',
    'gemini.google.com',
    'copilot.microsoft.com',
    'perplexity.ai',
    'poe.com',
    'mistral.ai',
    'chat.mistral.ai',
    'huggingface.co',
    'deepseek.com',
    'cohere.com',
    'replicate.com',
    'groq.com',
    'character.ai'
  ],
  AI_KEYWORDS: [
    'model',
    'prompt',
    'token',
    'temperature',
    'chatgpt',
    'claude',
    'gemini',
    'copilot',
    'llm',
    'gpt-4',
    'gpt-3',
    'ai assistant',
    'regenerate',
    'generate response',
    'streaming'
  ]
};

// Expose globally for both service worker and content scripts
if (typeof self !== 'undefined') {
  self.CONFIG = CONFIG;
}
if (typeof window !== 'undefined') {
  window.CONFIG = CONFIG;
}
