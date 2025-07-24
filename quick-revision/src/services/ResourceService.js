/**
 * ResourceService - Service for interacting with company resources
 * 
 * This service handles communication with company resources including
 * dashboards, research reports, and spaces.
 */

// Default configuration - should be loaded from environment variables in production
const DEFAULT_CONFIG = {
  apiBaseUrl: 'https://api.company.com/resources',
  apiKey: '', // Should be loaded from environment variables
};

/**
 * ResourceService class for company resource integration
 */
class ResourceService {
  /**
   * Create a new ResourceService instance
   * @param {Object} config - Configuration for the resource service
   */
  constructor(config = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.authToken = config.authToken || '';
  }

  /**
   * Set the authentication token
   * @param {string} token - Authentication token
   */
  setAuthToken(token) {
    this.authToken = token;
  }

  /**
   * Make an authenticated API request
   * @param {string} endpoint - API endpoint
   * @param {Object} options - Request options
   * @returns {Promise<Object>} - Promise resolving to the API response
   */
  async makeRequest(endpoint, options = {}) {
    try {
      const url = `${this.config.apiBaseUrl}/${endpoint}`;
      
      const response = await fetch(url, {
        method: options.method || 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.authToken}`,
          ...options.headers
        },
        body: options.body ? JSON.stringify(options.body) : undefined
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          `Resource API request failed with status ${response.status}: ${
            errorData.error?.message || 'Unknown error'
          }`
        );
      }

      return await response.json();
    } catch (error) {
      console.error(`Error in ResourceService (${endpoint}):`, error);
      throw error;
    }
  }

  /**
   * Fetch a dashboard by ID
   * @param {string} dashboardId - Dashboard ID
   * @returns {Promise<Object>} - Promise resolving to the dashboard data
   */
  async fetchDashboard(dashboardId) {
    const data = await this.makeRequest(`dashboards/${dashboardId}`);
    
    return {
      type: 'dashboard',
      id: dashboardId,
      title: data.title,
      description: data.description,
      content: data.content,
      lastUpdated: new Date(data.lastUpdated),
      creator: data.creator,
      url: data.url,
      source: {
        type: 'company',
        title: data.title,
        url: data.url,
      }
    };
  }

  /**
   * Search dashboards
   * @param {string} query - Search query
   * @param {Object} filters - Search filters
   * @returns {Promise<Array>} - Promise resolving to an array of dashboard summaries
   */
  async searchDashboards(query, filters = {}) {
    return await this.makeRequest('dashboards/search', {
      method: 'POST',
      body: { query, filters }
    });
  }

  /**
   * Fetch a research report by ID
   * @param {string} reportId - Report ID
   * @returns {Promise<Object>} - Promise resolving to the report data
   */
  async fetchReport(reportId) {
    const data = await this.makeRequest(`reports/${reportId}`);
    
    return {
      type: 'report',
      id: reportId,
      title: data.title,
      abstract: data.abstract,
      content: data.content,
      authors: data.authors,
      publishDate: new Date(data.publishDate),
      tags: data.tags,
      url: data.url,
      source: {
        type: 'company',
        title: data.title,
        url: data.url,
      }
    };
  }

  /**
   * Search research reports
   * @param {string} query - Search query
   * @param {Object} filters - Search filters
   * @returns {Promise<Array>} - Promise resolving to an array of report summaries
   */
  async searchReports(query, filters = {}) {
    return await this.makeRequest('reports/search', {
      method: 'POST',
      body: { query, filters }
    });
  }

  /**
   * Fetch a space by ID
   * @param {string} spaceId - Space ID
   * @returns {Promise<Object>} - Promise resolving to the space data
   */
  async fetchSpace(spaceId) {
    const data = await this.makeRequest(`spaces/${spaceId}`);
    
    return {
      type: 'space',
      id: spaceId,
      title: data.title,
      description: data.description,
      content: data.content,
      owner: data.owner,
      members: data.members,
      resources: data.resources,
      lastUpdated: new Date(data.lastUpdated),
      url: data.url,
      source: {
        type: 'company',
        title: data.title,
        url: data.url,
      }
    };
  }

  /**
   * Search spaces
   * @param {string} query - Search query
   * @param {Object} filters - Search filters
   * @returns {Promise<Array>} - Promise resolving to an array of space summaries
   */
  async searchSpaces(query, filters = {}) {
    return await this.makeRequest('spaces/search', {
      method: 'POST',
      body: { query, filters }
    });
  }

  /**
   * Mock fetch dashboard for development without API
   * @param {string} dashboardId - Dashboard ID
   * @returns {Promise<Object>} - Promise resolving to mock dashboard data
   */
  async mockFetchDashboard(dashboardId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          type: 'dashboard',
          id: dashboardId,
          title: `Dashboard ${dashboardId}`,
          description: 'This is a mock dashboard for development purposes.',
          content: 'Mock dashboard content with charts and metrics data.',
          lastUpdated: new Date(),
          creator: 'Mock User',
          url: `https://company.com/dashboards/${dashboardId}`,
          source: {
            type: 'company',
            title: `Dashboard ${dashboardId}`,
            url: `https://company.com/dashboards/${dashboardId}`,
          }
        });
      }, 500);
    });
  }

  /**
   * Mock fetch report for development without API
   * @param {string} reportId - Report ID
   * @returns {Promise<Object>} - Promise resolving to mock report data
   */
  async mockFetchReport(reportId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          type: 'report',
          id: reportId,
          title: `Research Report ${reportId}`,
          abstract: 'This is a mock research report abstract for development purposes.',
          content: 'Mock report content with detailed analysis and findings.',
          authors: ['Mock Author 1', 'Mock Author 2'],
          publishDate: new Date(),
          tags: ['mock', 'development', 'research'],
          url: `https://company.com/reports/${reportId}`,
          source: {
            type: 'company',
            title: `Research Report ${reportId}`,
            url: `https://company.com/reports/${reportId}`,
          }
        });
      }, 500);
    });
  }

  /**
   * Mock fetch space for development without API
   * @param {string} spaceId - Space ID
   * @returns {Promise<Object>} - Promise resolving to mock space data
   */
  async mockFetchSpace(spaceId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          type: 'space',
          id: spaceId,
          title: `Space ${spaceId}`,
          description: 'This is a mock space for development purposes.',
          content: 'Mock space content with collaborative documents and resources.',
          owner: 'Mock Owner',
          members: ['Mock Member 1', 'Mock Member 2'],
          resources: [
            { id: '1', type: 'document', title: 'Mock Document 1' },
            { id: '2', type: 'dashboard', title: 'Mock Dashboard 1' }
          ],
          lastUpdated: new Date(),
          url: `https://company.com/spaces/${spaceId}`,
          source: {
            type: 'company',
            title: `Space ${spaceId}`,
            url: `https://company.com/spaces/${spaceId}`,
          }
        });
      }, 500);
    });
  }
}

export default ResourceService;