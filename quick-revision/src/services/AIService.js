/**
 * AIService - Service for interacting with LLM providers
 * 
 * This service handles communication with AI language model providers,
 * supporting both standard and streaming responses, citation extraction,
 * and intermediate steps visualization.
 * 
 * Implements requirements:
 * - 2.1: Connect to default LLM provider when conversation is initiated
 * - 2.4: Properly attribute sources in responses
 */

// Default configuration - should be loaded from environment variables in production
const DEFAULT_CONFIG = {
  apiUrl: 'https://api.openai.com/v1/chat/completions',
  apiKey: '', // Should be loaded from environment variables
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 1000,
  systemPrompt: 'You are a helpful assistant for the Gen AI Chat Assistant.',
  enableCitations: true,
  enableIntermediateSteps: true,
  fallbackProviders: ['anthropic', 'cohere'], // Fallback LLM providers if primary is unavailable
  retryAttempts: 3, // Number of retry attempts for failed requests
  retryDelay: 1000 // Delay between retry attempts in milliseconds
};

/**
 * AIService class for LLM integration
 * Provides a unified interface for interacting with various LLM providers
 */
class AIService {
  /**
   * Create a new AIService instance
   * @param {Object} config - Configuration for the AI service
   */
  constructor(config = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.currentProvider = this.config.provider || 'openai';
    this.retryCount = 0;
    this.isConnected = false;
    
    // Initialize connection status
    this.checkConnection();
  }
  
  /**
   * Check connection to the LLM provider
   * @returns {Promise<boolean>} - Promise resolving to connection status
   */
  async checkConnection() {
    try {
      // Simple ping request to check if the API is accessible
      const response = await fetch(`${this.config.apiUrl}/models`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.config.apiKey}`
        }
      });
      
      this.isConnected = response.ok;
      return this.isConnected;
    } catch (error) {
      console.warn('Failed to connect to LLM provider:', error);
      this.isConnected = false;
      return false;
    }
  }
  
  /**
   * Switch to a different LLM provider
   * @param {string} providerName - Name of the provider to switch to
   * @returns {boolean} - Success status of the switch
   */
  switchProvider(providerName) {
    if (!this.config.fallbackProviders.includes(providerName) && providerName !== 'openai') {
      console.error(`Provider ${providerName} is not configured`);
      return false;
    }
    
    // In a real implementation, this would update the API URL and authentication
    // For now, we'll just update the provider name
    this.currentProvider = providerName;
    console.log(`Switched to ${providerName} provider`);
    return true;
  }

  /**
   * Format messages for the API
   * @param {Array} messages - Array of message objects
   * @returns {Array} - Formatted messages for the API
   */
  formatMessages(messages) {
    const formattedMessages = messages.map(msg => ({
      role: msg.isUser ? 'user' : 'assistant',
      content: msg.text
    }));

    // Add system message
    formattedMessages.unshift({
      role: 'system',
      content: this.config.systemPrompt
    });

    return formattedMessages;
  }

  /**
   * Generate a response from the LLM
   * @param {Array} messages - Array of message objects
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async generateResponse(messages, options = {}) {
    // Reset retry count for new requests
    this.retryCount = 0;
    
    // If not connected, try to reconnect
    if (!this.isConnected) {
      await this.checkConnection();
      if (!this.isConnected) {
        return this.handleProviderFailover(messages, options);
      }
    }
    
    try {
      const formattedMessages = this.formatMessages(messages);
      
      const requestBody = {
        model: options.model || this.config.model,
        messages: formattedMessages,
        temperature: options.temperature || this.config.temperature,
        max_tokens: options.maxTokens || this.config.maxTokens
      };
      
      // Add optional parameters if provided
      if (options.topP) requestBody.top_p = options.topP;
      if (options.presencePenalty) requestBody.presence_penalty = options.presencePenalty;
      if (options.frequencyPenalty) requestBody.frequency_penalty = options.frequencyPenalty;
      if (options.stop) requestBody.stop = options.stop;
      
      const response = await fetch(this.config.apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.config.apiKey}`
        },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const errorMessage = errorData.error?.message || 'Unknown error';
        
        // Handle rate limiting with retry
        if (response.status === 429) {
          return this.handleRateLimiting(messages, options);
        }
        
        // Handle server errors with retry
        if (response.status >= 500) {
          return this.handleServerError(messages, options);
        }
        
        // Handle authentication errors with provider failover
        if (response.status === 401 || response.status === 403) {
          return this.handleProviderFailover(messages, options);
        }
        
        throw new Error(`API request failed with status ${response.status}: ${errorMessage}`);
      }

      const data = await response.json();
      
      // Process the response to extract citations and intermediate steps
      const processedResponse = this.processResponseText(data.choices[0].message.content);
      
      return {
        text: processedResponse.text,
        model: data.model,
        usage: data.usage,
        citations: processedResponse.citations,
        intermediateSteps: processedResponse.intermediateSteps,
        source: {
          type: 'ai',
          title: data.model,
          provider: this.currentProvider
        }
      };
    } catch (error) {
      console.error('Error generating AI response:', error);
      
      // If we haven't exceeded retry attempts, try again
      if (this.retryCount < this.config.retryAttempts) {
        return this.retryGenerateResponse(messages, options);
      }
      
      // If we've exhausted retries, try failover
      return this.handleProviderFailover(messages, options);
    }
  }
  
  /**
   * Retry generating a response after a delay
   * @param {Array} messages - Array of message objects
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async retryGenerateResponse(messages, options = {}) {
    this.retryCount++;
    console.log(`Retrying request (${this.retryCount}/${this.config.retryAttempts})...`);
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, this.config.retryDelay));
    
    // Try again
    return this.generateResponse(messages, options);
  }
  
  /**
   * Handle rate limiting by waiting and retrying
   * @param {Array} messages - Array of message objects
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleRateLimiting(messages, options = {}) {
    if (this.retryCount >= this.config.retryAttempts) {
      return this.handleProviderFailover(messages, options);
    }
    
    // Exponential backoff for rate limiting
    const delay = this.config.retryDelay * Math.pow(2, this.retryCount);
    console.log(`Rate limited. Waiting ${delay}ms before retry...`);
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, delay));
    
    return this.retryGenerateResponse(messages, options);
  }
  
  /**
   * Handle server errors by retrying
   * @param {Array} messages - Array of message objects
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleServerError(messages, options = {}) {
    if (this.retryCount >= this.config.retryAttempts) {
      return this.handleProviderFailover(messages, options);
    }
    
    console.log(`Server error. Retrying in ${this.config.retryDelay}ms...`);
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, this.config.retryDelay));
    
    return this.retryGenerateResponse(messages, options);
  }
  
  /**
   * Handle provider failover by switching to a fallback provider
   * @param {Array} messages - Array of message objects
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleProviderFailover(messages, options = {}) {
    // If we've tried all providers, fall back to mock response
    if (this.config.fallbackProviders.length === 0) {
      console.warn('All providers failed. Using mock response.');
      return this.mockGenerateResponse(messages);
    }
    
    // Get the next provider
    const nextProvider = this.config.fallbackProviders.shift();
    console.log(`Switching to fallback provider: ${nextProvider}`);
    
    // Switch to the next provider
    this.switchProvider(nextProvider);
    
    // Reset retry count for the new provider
    this.retryCount = 0;
    
    // Try with the new provider
    return this.generateResponse(messages, options);
  }

  /**
   * Stream a response from the LLM
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving when streaming is complete
   */
  async streamResponse(messages, onChunk, options = {}) {
    // Reset retry count for new requests
    this.retryCount = 0;
    
    // If not connected, try to reconnect
    if (!this.isConnected) {
      await this.checkConnection();
      if (!this.isConnected) {
        return this.handleStreamingProviderFailover(messages, onChunk, options);
      }
    }
    
    try {
      const formattedMessages = this.formatMessages(messages);
      
      const requestBody = {
        model: options.model || this.config.model,
        messages: formattedMessages,
        temperature: options.temperature || this.config.temperature,
        max_tokens: options.maxTokens || this.config.maxTokens,
        stream: true
      };
      
      // Add optional parameters if provided
      if (options.topP) requestBody.top_p = options.topP;
      if (options.presencePenalty) requestBody.presence_penalty = options.presencePenalty;
      if (options.frequencyPenalty) requestBody.frequency_penalty = options.frequencyPenalty;
      if (options.stop) requestBody.stop = options.stop;
      
      const response = await fetch(this.config.apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.config.apiKey}`
        },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const errorMessage = errorData.error?.message || 'Unknown error';
        
        // Handle rate limiting with retry
        if (response.status === 429) {
          return this.handleStreamingRateLimiting(messages, onChunk, options);
        }
        
        // Handle server errors with retry
        if (response.status >= 500) {
          return this.handleStreamingServerError(messages, onChunk, options);
        }
        
        // Handle authentication errors with provider failover
        if (response.status === 401 || response.status === 403) {
          return this.handleStreamingProviderFailover(messages, onChunk, options);
        }
        
        throw new Error(`API request failed with status ${response.status}: ${errorMessage}`);
      }

      // Ensure the response is readable as a stream
      if (!response.body) {
        throw new Error('ReadableStream not supported in this browser.');
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let fullText = '';
      let model = '';
      let intermediateSteps = [];
      let currentStep = null;
      
      // Process the stream
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        
        // Decode the chunk
        const chunk = decoder.decode(value, { stream: true });
        
        // Process the chunk (OpenAI specific format)
        const lines = chunk
          .split('\n')
          .filter(line => line.trim() !== '' && line.trim() !== 'data: [DONE]');
        
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const jsonData = JSON.parse(line.slice(6));
              
              // Extract the model if not already set
              if (!model && jsonData.model) {
                model = jsonData.model;
              }
              
              // Extract the content delta
              const contentDelta = jsonData.choices?.[0]?.delta?.content;
              if (contentDelta) {
                fullText += contentDelta;
                
                // Check if this chunk contains step markers
                if (contentDelta.includes('Step ') && !currentStep) {
                  // Start a new step
                  const stepMatch = /Step (\d+):/.exec(contentDelta);
                  if (stepMatch) {
                    const stepNumber = parseInt(stepMatch[1], 10);
                    currentStep = {
                      id: stepNumber,
                      description: `Step ${stepNumber}`,
                      content: contentDelta
                    };
                  }
                } else if (currentStep) {
                  // Continue adding to the current step
                  currentStep.content += contentDelta;
                  
                  // Check if the step is complete
                  if (contentDelta.includes('Step ') || contentDelta.includes('\n\n')) {
                    intermediateSteps.push(currentStep);
                    currentStep = null;
                  }
                }
                
                // Pass the chunk to the callback with additional metadata
                onChunk({
                  text: contentDelta,
                  fullText,
                  intermediateSteps,
                  currentStep,
                  isComplete: false
                });
              }
            } catch (e) {
              console.warn('Error parsing SSE chunk:', e);
            }
          }
        }
      }
      
      // If there's a current step that hasn't been added yet, add it
      if (currentStep) {
        intermediateSteps.push(currentStep);
      }
      
      // Process the full text to extract citations
      const processedResponse = this.processResponseText(fullText);
      
      // Send final complete response
      onChunk({
        text: '',
        fullText,
        intermediateSteps: intermediateSteps.length > 0 ? intermediateSteps : processedResponse.intermediateSteps,
        citations: processedResponse.citations,
        isComplete: true
      });
      
      // Return the complete response data
      return {
        text: fullText,
        model: model || this.config.model,
        citations: processedResponse.citations,
        intermediateSteps: intermediateSteps.length > 0 ? intermediateSteps : processedResponse.intermediateSteps,
        source: {
          type: 'ai',
          title: model || this.config.model,
          provider: this.currentProvider
        }
      };
    } catch (error) {
      console.error('Error streaming AI response:', error);
      
      // If we haven't exceeded retry attempts, try again
      if (this.retryCount < this.config.retryAttempts) {
        return this.retryStreamResponse(messages, onChunk, options);
      }
      
      // If we've exhausted retries, try failover
      return this.handleStreamingProviderFailover(messages, onChunk, options);
    }
  }
  
  /**
   * Retry streaming a response after a delay
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async retryStreamResponse(messages, onChunk, options = {}) {
    this.retryCount++;
    console.log(`Retrying streaming request (${this.retryCount}/${this.config.retryAttempts})...`);
    
    // Notify the user about the retry
    onChunk(`\n[Retrying request ${this.retryCount}/${this.config.retryAttempts}...]\n`, null, { isSystemMessage: true });
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, this.config.retryDelay));
    
    // Try again
    return this.streamResponse(messages, onChunk, options);
  }
  
  /**
   * Handle rate limiting for streaming by waiting and retrying
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleStreamingRateLimiting(messages, onChunk, options = {}) {
    if (this.retryCount >= this.config.retryAttempts) {
      return this.handleStreamingProviderFailover(messages, onChunk, options);
    }
    
    // Exponential backoff for rate limiting
    const delay = this.config.retryDelay * Math.pow(2, this.retryCount);
    console.log(`Rate limited. Waiting ${delay}ms before retry...`);
    
    // Notify the user about the rate limiting
    onChunk(`\n[Rate limited. Waiting before retry...]\n`, null, { isSystemMessage: true });
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, delay));
    
    return this.retryStreamResponse(messages, onChunk, options);
  }
  
  /**
   * Handle server errors for streaming by retrying
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleStreamingServerError(messages, onChunk, options = {}) {
    if (this.retryCount >= this.config.retryAttempts) {
      return this.handleStreamingProviderFailover(messages, onChunk, options);
    }
    
    console.log(`Server error. Retrying in ${this.config.retryDelay}ms...`);
    
    // Notify the user about the server error
    onChunk(`\n[Server error. Retrying...]\n`, null, { isSystemMessage: true });
    
    // Wait before retrying
    await new Promise(resolve => setTimeout(resolve, this.config.retryDelay));
    
    return this.retryStreamResponse(messages, onChunk, options);
  }
  
  /**
   * Handle provider failover for streaming by switching to a fallback provider
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving to the AI response
   */
  async handleStreamingProviderFailover(messages, onChunk, options = {}) {
    // If we've tried all providers, fall back to mock response
    if (this.config.fallbackProviders.length === 0) {
      console.warn('All providers failed. Using mock streaming response.');
      
      // Notify the user about the failover to mock
      onChunk(`\n[All providers unavailable. Using backup response.]\n`, null, { isSystemMessage: true });
      
      return this.mockStreamResponse(messages, onChunk);
    }
    
    // Get the next provider
    const nextProvider = this.config.fallbackProviders.shift();
    console.log(`Switching to fallback provider: ${nextProvider}`);
    
    // Notify the user about the provider switch
    onChunk(`\n[Switching to alternative provider...]\n`, null, { isSystemMessage: true });
    
    // Switch to the next provider
    this.switchProvider(nextProvider);
    
    // Reset retry count for the new provider
    this.retryCount = 0;
    
    // Try with the new provider
    return this.streamResponse(messages, onChunk, options);
  }

  /**
   * Mock response generation for development without API key
   * @param {Array} messages - Array of message objects
   * @returns {Promise<Object>} - Promise resolving to the mock AI response
   */
  async mockGenerateResponse(messages) {
    return new Promise((resolve) => {
      // Simulate API delay
      setTimeout(() => {
        const lastMessage = messages[messages.length - 1];
        let responseText = '';
        
        if (lastMessage.isUser) {
          const userMessage = lastMessage.text.toLowerCase();
          
          // Simple response logic
          if (userMessage.includes('hello') || userMessage.includes('hi')) {
            responseText = 'Hello! How can I help you today?';
          } else if (userMessage.includes('help')) {
            responseText = 'I can help you with information, answer questions, or assist with various tasks. What would you like to know?';
          } else if (userMessage.includes('thank')) {
            responseText = 'You\'re welcome! Let me know if you need anything else.';
          } else {
            responseText = `I understand you're asking about "${lastMessage.text}". Here's what I know about this topic...`;
          }
        } else {
          responseText = 'How else can I assist you?';
        }

        resolve({
          text: responseText,
          model: 'mock-model',
          citations: [],
          source: {
            type: 'ai',
            title: 'Mock AI Model',
          }
        });
      }, 1000);
    });
  }

  /**
   * Extract citations from text
   * @param {string} text - Text to extract citations from
   * @returns {Array} - Array of extracted citations
   */
  extractCitations(text) {
    // This is a simplified implementation
    // In a real system, this would use more sophisticated NLP techniques
    const citations = [];
    
    // Look for citation patterns like [1], [2], etc.
    const citationRegex = /\[(\d+)\]/g;
    let match;
    
    // Look for citation details at the end of the text
    // Format: [1] Source Title - URL or description
    const citationDetailsRegex = /\[(\d+)\]\s+(.+?)(?=\[\d+\]|$)/g;
    const citationDetails = new Map();
    
    // First, extract any citation details from the end of the text
    let detailsMatch;
    while ((detailsMatch = citationDetailsRegex.exec(text)) !== null) {
      const citationNumber = parseInt(detailsMatch[1], 10);
      const citationText = detailsMatch[2].trim();
      
      // Extract URL if present
      let url = null;
      let title = citationText;
      const urlMatch = citationText.match(/(https?:\/\/[^\s]+)/);
      if (urlMatch) {
        url = urlMatch[1];
        title = citationText.replace(url, '').trim();
        // Remove trailing dash or hyphen
        title = title.replace(/[-–—]\s*$/, '').trim();
      }
      
      citationDetails.set(citationNumber, {
        text: citationText,
        title: title || 'Source',
        url
      });
    }
    
    // Now extract all citation references
    while ((match = citationRegex.exec(text)) !== null) {
      const citationNumber = parseInt(match[1], 10);
      
      // Add citation if it doesn't already exist
      if (!citations.some(c => c.id === citationNumber)) {
        const details = citationDetails.get(citationNumber);
        
        citations.push({
          id: citationNumber,
          text: details ? details.text : `Citation ${citationNumber}`,
          source: {
            type: details?.url ? 'web' : 'ai',
            title: details?.title || 'AI Generated Citation',
            url: details?.url,
            provider: this.currentProvider
          }
        });
      }
    }
    
    return citations;
  }

  /**
   * Extract intermediate steps from text
   * @param {string} text - Text to extract steps from
   * @returns {Array} - Array of extracted steps
   */
  extractIntermediateSteps(text) {
    // This is a simplified implementation
    // In a real system, this would use more sophisticated techniques
    const steps = [];
    
    // Look for step patterns like "Step 1:", "Step 2:", etc.
    const stepRegex = /Step (\d+):(.*?)(?=Step \d+:|$)/gs;
    let match;
    
    // Also look for reasoning patterns like "Reasoning:", "Analysis:", etc.
    const reasoningPatterns = [
      { pattern: /Reasoning:(.*?)(?=\n\n|$)/s, description: "Reasoning" },
      { pattern: /Analysis:(.*?)(?=\n\n|$)/s, description: "Analysis" },
      { pattern: /Thought process:(.*?)(?=\n\n|$)/s, description: "Thought Process" },
      { pattern: /Let me think:(.*?)(?=\n\n|$)/s, description: "Thinking" }
    ];
    
    // Extract numbered steps
    while ((match = stepRegex.exec(text)) !== null) {
      const stepNumber = parseInt(match[1], 10);
      const stepContent = match[2].trim();
      
      steps.push({
        id: stepNumber,
        description: `Step ${stepNumber}`,
        content: stepContent
      });
    }
    
    // Extract reasoning patterns if no numbered steps were found
    if (steps.length === 0) {
      for (const { pattern, description } of reasoningPatterns) {
        const match = text.match(pattern);
        if (match) {
          steps.push({
            id: steps.length + 1,
            description,
            content: match[1].trim()
          });
        }
      }
    }
    
    return steps;
  }

  /**
   * Process response text to extract citations and steps
   * @param {string} text - Response text to process
   * @param {Object} options - Processing options
   * @returns {Object} - Processed response with citations and steps
   */
  processResponseText(text, options = {}) {
    const processedResponse = {
      text,
      citations: [],
      intermediateSteps: []
    };
    
    // Extract citations if enabled
    if (this.config.enableCitations && options.extractCitations !== false) {
      processedResponse.citations = this.extractCitations(text);
      
      // If citations were found at the end of the text, we might want to remove them
      // from the displayed text to avoid duplication
      if (processedResponse.citations.length > 0 && options.removeCitationText !== false) {
        // Look for citation list at the end of the text
        const citationListMatch = text.match(/\n\nReferences:\n\[.+$/s);
        if (citationListMatch) {
          processedResponse.text = text.replace(citationListMatch[0], '');
        }
      }
    }
    
    // Extract intermediate steps if enabled
    if (this.config.enableIntermediateSteps && options.extractSteps !== false) {
      processedResponse.intermediateSteps = this.extractIntermediateSteps(text);
      
      // If steps were found and the option is set, remove them from the displayed text
      if (processedResponse.intermediateSteps.length > 0 && options.removeStepsFromText) {
        // This is a simplified approach - in a real system, we'd need more sophisticated text processing
        let cleanedText = processedResponse.text;
        
        // Remove step patterns
        for (const step of processedResponse.intermediateSteps) {
          const stepPattern = new RegExp(`Step ${step.id}:${step.content.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`, 'g');
          cleanedText = cleanedText.replace(stepPattern, '');
        }
        
        // Clean up any double newlines or spaces
        cleanedText = cleanedText.replace(/\n{3,}/g, '\n\n').trim();
        
        processedResponse.text = cleanedText;
      }
    }
    
    return processedResponse;
  }

  /**
   * Mock streaming response for development without API key
   * @param {Array} messages - Array of message objects
   * @param {Function} onChunk - Callback function for each chunk of the response
   * @param {Object} options - Additional options for the request
   * @returns {Promise<Object>} - Promise resolving when streaming is complete
   */
  async mockStreamResponse(messages, onChunk, options = {}) {
    return new Promise(async (resolve) => {
      const lastMessage = messages[messages.length - 1];
      let responseText = '';
      let fullText = '';
      let intermediateSteps = [];
      
      if (lastMessage.isUser) {
        const userMessage = lastMessage.text.toLowerCase();
        
        // Add first intermediate step
        intermediateSteps.push({
          id: 1,
          description: "Analyzing user query",
          content: `Received user message: "${lastMessage.text}"\nAnalyzing intent and context...`
        });
        
        // Send first intermediate step
        onChunk({
          text: '',
          fullText: '',
          intermediateSteps,
          isComplete: false
        });
        
        // Wait a bit before sending the second step
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // Add second intermediate step
        intermediateSteps.push({
          id: 2,
          description: "Formulating response",
          content: "Based on the user's query, I'll provide a helpful response that addresses their needs."
        });
        
        // Send updated intermediate steps
        onChunk({
          text: '',
          fullText: '',
          intermediateSteps,
          isComplete: false
        });
        
        // Wait a bit before starting to stream the response
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // Determine response based on user message
        let responseChunks = [];
        let citations = [];
        
        if (userMessage.includes('hello') || userMessage.includes('hi')) {
          responseChunks = 'Hello! How can I help you today?'.split(' ');
        } else if (userMessage.includes('help')) {
          responseChunks = 'I can help you with information, answer questions, or assist with various tasks. What would you like to know?'.split(' ');
        } else if (userMessage.includes('thank')) {
          responseChunks = 'You\'re welcome! Let me know if you need anything else.'.split(' ');
        } else if (userMessage.includes('citation') || userMessage.includes('source')) {
          responseChunks = 'According to recent research [1], AI assistants can significantly improve productivity. Another study [2] suggests that natural language interfaces are becoming increasingly important.'.split(' ');
          citations = [
            {
              id: 1,
              text: "The Impact of AI Assistants on Productivity",
              source: {
                type: 'web',
                title: 'AI Productivity Research',
                url: 'https://example.com/ai-productivity',
                snippet: "AI assistants can significantly improve productivity across various tasks."
              }
            },
            {
              id: 2,
              text: "Natural Language Interfaces: A Survey",
              source: {
                type: 'company',
                title: 'Journal of AI Research',
                location: 'Research Database',
                snippet: "Natural language interfaces are becoming increasingly important in human-computer interaction."
              }
            }
          ];
        } else {
          responseChunks = `I understand you're asking about "${lastMessage.text}". Here are some thoughts on this topic based on my knowledge.`.split(' ');
          citations = [
            {
              id: 1,
              text: "AI Knowledge Base",
              source: {
                type: 'ai',
                title: 'Large Language Model',
                snippet: "Information derived from the model's training data up to its knowledge cutoff date."
              }
            }
          ];
        }
        
        // Stream each word with a delay
        for (let i = 0; i < responseChunks.length; i++) {
          const chunk = responseChunks[i] + (i < responseChunks.length - 1 ? ' ' : '');
          fullText += chunk;
          
          // Send the chunk
          onChunk({
            text: chunk,
            fullText,
            intermediateSteps,
            isComplete: false
          });
          
          // Random delay between 50-150ms to simulate typing
          await new Promise(resolve => setTimeout(resolve, Math.floor(Math.random() * 100) + 50));
        }
        
        // Add final step
        intermediateSteps.push({
          id: 3,
          description: "Final response",
          content: fullText
        });
        
        // Send final complete response
        onChunk({
          text: '',
          fullText,
          intermediateSteps,
          citations,
          isComplete: true
        });
        
        // Resolve with the complete response
        resolve({
          text: fullText,
          model: 'mock-model',
          citations: citations,
          intermediateSteps: intermediateSteps,
          source: {
            type: 'ai',
            title: 'Mock AI Model',
          }
        });
      } else {
        // If not a user message, just return a simple response
        fullText = 'How else can I assist you?';
        
        // Stream the simple response
        for (let i = 0; i < fullText.length; i++) {
          const chunk = fullText[i];
          
          onChunk({
            text: chunk,
            fullText: fullText.substring(0, i + 1),
            intermediateSteps: [],
            isComplete: false
          });
          
          await new Promise(resolve => setTimeout(resolve, 50));
        }
        
        // Send final complete response
        onChunk({
          text: '',
          fullText,
          intermediateSteps: [],
          citations: [],
          isComplete: true
        });
        
        resolve({
          text: fullText,
          model: 'mock-model',
          citations: [],
          intermediateSteps: [],
          source: {
            type: 'ai',
            title: 'Mock AI Model',
          }
        });
      }
    });
  }'s what I know about this topic...`;
        }
      } else {
        responseText = 'How else can I assist you?';
      }

      // Split the response into chunks to simulate streaming
      const words = responseText.split(' ');
      let fullText = '';
      let index = 0;

      // Stream each word with a delay
      const interval = setInterval(() => {
        if (index >= words.length) {
          clearInterval(interval);
          
          // Process the full response to extract citations and steps
          const processedResponse = this.processResponseText(fullText, options);
          
          resolve({
            text: fullText,
            model: 'mock-model',
            citations: processedResponse.citations,
            intermediateSteps: processedResponse.intermediateSteps,
            source: {
              type: 'ai',
              title: 'Mock AI Model',
              provider: 'mock'
            }
          });
          
          return;
        }

        const chunk = words[index] + (index < words.length - 1 ? ' ' : '');
        fullText += chunk;
        onChunk(chunk, fullText);
        index++;
      }, 100);
    });
  }
  
  /**
   * Get available LLM models
   * @returns {Promise<Array>} - Promise resolving to array of available models
   */
  async getAvailableModels() {
    try {
      const response = await fetch(`${this.config.apiUrl}/models`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.config.apiKey}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch models: ${response.status}`);
      }
      
      const data = await response.json();
      return data.data || [];
    } catch (error) {
      console.error('Error fetching available models:', error);
      
      // Return mock models if API call fails
      return [
        { id: 'gpt-3.5-turbo', name: 'GPT-3.5 Turbo' },
        { id: 'gpt-4', name: 'GPT-4' },
        { id: 'gpt-4-turbo', name: 'GPT-4 Turbo' }
      ];
    }
  }
  
  /**
   * Get the current connection status
   * @returns {Object} - Connection status object
   */
  getConnectionStatus() {
    return {
      connected: this.isConnected,
      provider: this.currentProvider,
      availableFallbacks: [...this.config.fallbackProviders]
    };
  }
  
  /**
   * Set system prompt
   * @param {string} prompt - System prompt to set
   */
  setSystemPrompt(prompt) {
    if (typeof prompt === 'string' && prompt.trim()) {
      this.config.systemPrompt = prompt.trim();
      return true;
    }
    return false;
  }
  
  /**
   * Toggle citation extraction
   * @param {boolean} enabled - Whether to enable citation extraction
   */
  toggleCitations(enabled) {
    this.config.enableCitations = !!enabled;
  }
  
  /**
   * Toggle intermediate steps extraction
   * @param {boolean} enabled - Whether to enable intermediate steps extraction
   */
  toggleIntermediateSteps(enabled) {
    this.config.enableIntermediateSteps = !!enabled;
  }
}

export default AIService;