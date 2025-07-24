/**
 * WebContentService - Service for fetching and processing web content
 * 
 * This service handles fetching web content, extracting relevant information,
 * and summarizing content for use in the chat interface.
 */

// Default configuration
const DEFAULT_CONFIG = {
  proxyUrl: '/api/web-proxy', // Proxy endpoint to avoid CORS issues
  summarizationEndpoint: '/api/summarize',
  maxContentLength: 100000, // Maximum content length to process
};

/**
 * WebContentService class for web content integration
 */
class WebContentService {
  /**
   * Create a new WebContentService instance
   * @param {Object} config - Configuration for the web content service
   * @param {Object} aiService - Optional AIService instance for summarization
   */
  constructor(config = {}, aiService = null) {
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.aiService = aiService; // Optional AIService for summarization
  }

  /**
   * Fetch content from a URL
   * @param {string} url - URL to fetch content from
   * @returns {Promise<Object>} - Promise resolving to the web content data
   */
  async fetchContent(url) {
    try {
      // Use proxy to avoid CORS issues
      const response = await fetch(`${this.config.proxyUrl}?url=${encodeURIComponent(url)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch content from ${url}: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      
      return {
        url,
        title: data.title || url,
        content: data.content,
        html: data.html,
        metadata: data.metadata || {},
        timestamp: new Date(),
        source: {
          type: 'web',
          title: data.title || url,
          url: url,
        }
      };
    } catch (error) {
      console.error(`Error fetching web content from ${url}:`, error);
      throw error;
    }
  }

  /**
   * Extract main content from HTML
   * @param {string} html - HTML content
   * @returns {Object} - Extracted content and metadata
   */
  extractContent(html) {
    // In a real implementation, this would use a library like Readability
    // For this implementation, we'll use a simple regex-based approach
    
    // Create a temporary DOM element to parse the HTML
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    
    // Extract title
    const title = doc.querySelector('title')?.textContent || '';
    
    // Extract meta description
    const metaDescription = doc.querySelector('meta[name="description"]')?.getAttribute('content') || '';
    
    // Extract main content (simplified approach)
    // In a real implementation, this would be more sophisticated
    let content = '';
    
    // Try to find main content containers
    const mainElement = doc.querySelector('main') || 
                        doc.querySelector('article') || 
                        doc.querySelector('#content') || 
                        doc.querySelector('.content');
    
    if (mainElement) {
      content = mainElement.textContent;
    } else {
      // Fallback to body content
      content = doc.body.textContent;
    }
    
    // Clean up the content
    content = content
      .replace(/\s+/g, ' ')
      .trim()
      .substring(0, this.config.maxContentLength);
    
    return {
      title,
      content,
      metadata: {
        description: metaDescription,
      }
    };
  }

  /**
   * Summarize content
   * @param {string} content - Content to summarize
   * @param {Object} options - Summarization options
   * @returns {Promise<string>} - Promise resolving to the summarized content
   */
  async summarizeContent(content, options = {}) {
    try {
      // If AIService is provided, use it for summarization
      if (this.aiService) {
        const messages = [
          {
            isUser: true,
            text: `Please summarize the following content in ${options.maxLength || 200} words or less:\n\n${content}`
          }
        ];
        
        const response = await this.aiService.generateResponse(messages);
        return response.text;
      }
      
      // Otherwise use the summarization endpoint
      const response = await fetch(this.config.summarizationEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          content,
          maxLength: options.maxLength || 200,
        })
      });

      if (!response.ok) {
        throw new Error(`Failed to summarize content: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      return data.summary;
    } catch (error) {
      console.error('Error summarizing content:', error);
      
      // Fallback to a simple summary
      return content.substring(0, options.maxLength || 200) + '...';
    }
  }

  /**
   * Process a URL to extract and summarize content
   * @param {string} url - URL to process
   * @param {Object} options - Processing options
   * @returns {Promise<Object>} - Promise resolving to the processed content
   */
  async processUrl(url, options = {}) {
    try {
      // Fetch the content
      const webContent = await this.fetchContent(url);
      
      // Extract main content if HTML is available
      if (webContent.html) {
        const extracted = this.extractContent(webContent.html);
        webContent.title = extracted.title || webContent.title;
        webContent.content = extracted.content;
        webContent.metadata = { ...webContent.metadata, ...extracted.metadata };
      }
      
      // Summarize if requested
      if (options.summarize) {
        webContent.summary = await this.summarizeContent(webContent.content, {
          maxLength: options.summaryLength
        });
      }
      
      return webContent;
    } catch (error) {
      console.error(`Error processing URL ${url}:`, error);
      throw error;
    }
  }

  /**
   * Mock fetch content for development without API
   * @param {string} url - URL to fetch content from
   * @returns {Promise<Object>} - Promise resolving to mock web content data
   */
  async mockFetchContent(url) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          url,
          title: `Mock Web Page - ${url}`,
          content: `This is mock content for ${url}. It simulates web content that would be fetched from the actual URL in a production environment. The content includes various information that might be found on a typical web page, including paragraphs of text, headings, and possibly lists or other structured data.`,
          html: `<html><head><title>Mock Web Page - ${url}</title></head><body><h1>Mock Web Page</h1><p>This is mock content for ${url}.</p></body></html>`,
          metadata: {
            description: `Mock description for ${url}`,
            keywords: 'mock, web, content',
          },
          timestamp: new Date(),
          source: {
            type: 'web',
            title: `Mock Web Page - ${url}`,
            url: url,
          }
        });
      }, 800);
    });
  }

  /**
   * Mock summarize content for development without API
   * @param {string} content - Content to summarize
   * @param {Object} options - Summarization options
   * @returns {Promise<string>} - Promise resolving to mock summarized content
   */
  async mockSummarizeContent(content, options = {}) {
    return new Promise((resolve) => {
      setTimeout(() => {
        // Create a simple mock summary by taking the first few sentences
        const sentences = content.split(/[.!?]+/).filter(s => s.trim().length > 0);
        const summary = sentences.slice(0, 2).join('. ') + '.';
        
        resolve(summary);
      }, 500);
    });
  }
}

export default WebContentService;