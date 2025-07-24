/**
 * ActionService - Service for handling actions and workflows
 * 
 * This service handles action detection, presentation, and execution,
 * as well as workflow support.
 */

// Default configuration
const DEFAULT_CONFIG = {
  actionDetectionEndpoint: '/api/detect-actions',
  actionExecutionEndpoint: '/api/execute-action',
  workflowEndpoint: '/api/workflows',
};

/**
 * ActionService class for actions and workflows
 */
class ActionService {
  /**
   * Create a new ActionService instance
   * @param {Object} config - Configuration for the action service
   * @param {Object} aiService - Optional AIService instance for action detection
   */
  constructor(config = {}, aiService = null) {
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.aiService = aiService; // Optional AIService for action detection
    this.registeredActions = new Map(); // Map of action IDs to handler functions
    this.registeredWorkflows = new Map(); // Map of workflow IDs to workflow definitions
  }

  /**
   * Register an action handler
   * @param {string} actionId - Action ID
   * @param {Function} handler - Action handler function
   * @param {Object} metadata - Action metadata
   */
  registerAction(actionId, handler, metadata = {}) {
    this.registeredActions.set(actionId, {
      id: actionId,
      handler,
      name: metadata.name || actionId,
      description: metadata.description || '',
      parameters: metadata.parameters || {},
      icon: metadata.icon || null,
    });
  }

  /**
   * Register a workflow
   * @param {string} workflowId - Workflow ID
   * @param {Object} workflow - Workflow definition
   */
  registerWorkflow(workflowId, workflow) {
    this.registeredWorkflows.set(workflowId, {
      id: workflowId,
      ...workflow,
    });
  }

  /**
   * Detect actions from a message
   * @param {string} message - Message to detect actions from
   * @param {Object} context - Additional context for action detection
   * @returns {Promise<Array>} - Promise resolving to an array of detected actions
   */
  async detectActions(message, context = {}) {
    try {
      // If AIService is provided, use it for action detection
      if (this.aiService) {
        const messages = [
          {
            isUser: true,
            text: `Detect possible actions from the following message: ${message}`
          }
        ];
        
        const response = await this.aiService.generateResponse(messages);
        
        // Parse the response to extract actions
        // This is a simplified implementation
        // In a real system, the AI would return structured data
        const actions = [];
        
        // For now, return empty array as this requires specific AI model support
        return actions;
      }
      
      // Otherwise use the action detection endpoint
      const response = await fetch(this.config.actionDetectionEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message,
          context,
        })
      });

      if (!response.ok) {
        throw new Error(`Action detection failed with status ${response.status}`);
      }

      const data = await response.json();
      return data.actions;
    } catch (error) {
      console.error('Error detecting actions:', error);
      return [];
    }
  }

  /**
   * Execute an action
   * @param {string} actionId - Action ID
   * @param {Object} parameters - Action parameters
   * @returns {Promise<Object>} - Promise resolving to the action result
   */
  async executeAction(actionId, parameters = {}) {
    try {
      // Check if the action is registered locally
      if (this.registeredActions.has(actionId)) {
        const action = this.registeredActions.get(actionId);
        return await action.handler(parameters);
      }
      
      // Otherwise use the action execution endpoint
      const response = await fetch(this.config.actionExecutionEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          actionId,
          parameters,
        })
      });

      if (!response.ok) {
        throw new Error(`Action execution failed with status ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error(`Error executing action ${actionId}:`, error);
      throw error;
    }
  }

  /**
   * Get available workflows
   * @param {Object} context - Context for workflow availability
   * @returns {Promise<Array>} - Promise resolving to an array of available workflows
   */
  async getAvailableWorkflows(context = {}) {
    try {
      // Return locally registered workflows
      if (this.registeredWorkflows.size > 0) {
        return Array.from(this.registeredWorkflows.values());
      }
      
      // Otherwise fetch from the workflow endpoint
      const response = await fetch(`${this.config.workflowEndpoint}/available`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(context)
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch workflows with status ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error getting available workflows:', error);
      return [];
    }
  }

  /**
   * Execute a workflow
   * @param {string} workflowId - Workflow ID
   * @param {Object} parameters - Workflow parameters
   * @param {Function} onStepComplete - Callback for step completion
   * @returns {Promise<Object>} - Promise resolving to the workflow result
   */
  async executeWorkflow(workflowId, parameters = {}, onStepComplete = null) {
    try {
      // Check if the workflow is registered locally
      if (this.registeredWorkflows.has(workflowId)) {
        const workflow = this.registeredWorkflows.get(workflowId);
        
        // Execute each step in sequence
        const results = [];
        let currentContext = { ...parameters };
        
        for (const step of workflow.steps) {
          // Execute the step
          const stepResult = await this.executeAction(step.actionId, {
            ...step.parameters,
            ...currentContext
          });
          
          // Update the context with the step result
          currentContext = {
            ...currentContext,
            [`${step.id}Result`]: stepResult
          };
          
          // Store the result
          results.push({
            stepId: step.id,
            result: stepResult
          });
          
          // Call the step completion callback if provided
          if (onStepComplete) {
            onStepComplete(step.id, stepResult, results.length, workflow.steps.length);
          }
        }
        
        return {
          workflowId,
          results,
          context: currentContext
        };
      }
      
      // Otherwise use the workflow execution endpoint
      const response = await fetch(`${this.config.workflowEndpoint}/execute/${workflowId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(parameters)
      });

      if (!response.ok) {
        throw new Error(`Workflow execution failed with status ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error(`Error executing workflow ${workflowId}:`, error);
      throw error;
    }
  }

  /**
   * Mock action detection for development without API
   * @param {string} message - Message to detect actions from
   * @returns {Promise<Array>} - Promise resolving to mock detected actions
   */
  async mockDetectActions(message) {
    return new Promise((resolve) => {
      setTimeout(() => {
        // Simple keyword-based action detection
        const actions = [];
        
        if (message.toLowerCase().includes('email') || message.toLowerCase().includes('send')) {
          actions.push({
            id: 'send-email',
            name: 'Send Email',
            description: 'Send an email to the specified recipient',
            parameters: {
              to: { type: 'string', description: 'Email recipient' },
              subject: { type: 'string', description: 'Email subject' },
              body: { type: 'string', description: 'Email body' }
            }
          });
        }
        
        if (message.toLowerCase().includes('schedule') || message.toLowerCase().includes('meeting')) {
          actions.push({
            id: 'schedule-meeting',
            name: 'Schedule Meeting',
            description: 'Schedule a meeting with the specified participants',
            parameters: {
              participants: { type: 'array', description: 'Meeting participants' },
              title: { type: 'string', description: 'Meeting title' },
              time: { type: 'string', description: 'Meeting time' },
              duration: { type: 'number', description: 'Meeting duration in minutes' }
            }
          });
        }
        
        if (message.toLowerCase().includes('search') || message.toLowerCase().includes('find')) {
          actions.push({
            id: 'search-documents',
            name: 'Search Documents',
            description: 'Search for documents matching the query',
            parameters: {
              query: { type: 'string', description: 'Search query' },
              filters: { type: 'object', description: 'Search filters' }
            }
          });
        }
        
        resolve(actions);
      }, 300);
    });
  }

  /**
   * Mock action execution for development without API
   * @param {string} actionId - Action ID
   * @param {Object} parameters - Action parameters
   * @returns {Promise<Object>} - Promise resolving to mock action result
   */
  async mockExecuteAction(actionId, parameters = {}) {
    return new Promise((resolve) => {
      setTimeout(() => {
        switch (actionId) {
          case 'send-email':
            resolve({
              success: true,
              actionId,
              result: {
                emailId: `email_${Date.now()}`,
                to: parameters.to,
                subject: parameters.subject,
                sentAt: new Date().toISOString()
              }
            });
            break;
            
          case 'schedule-meeting':
            resolve({
              success: true,
              actionId,
              result: {
                meetingId: `meeting_${Date.now()}`,
                title: parameters.title,
                participants: parameters.participants,
                scheduledTime: parameters.time,
                duration: parameters.duration
              }
            });
            break;
            
          case 'search-documents':
            resolve({
              success: true,
              actionId,
              result: {
                query: parameters.query,
                results: [
                  { id: 'doc1', title: 'Mock Document 1', snippet: 'This is a mock document snippet...' },
                  { id: 'doc2', title: 'Mock Document 2', snippet: 'Another mock document snippet...' },
                  { id: 'doc3', title: 'Mock Document 3', snippet: 'Yet another mock document snippet...' }
                ]
              }
            });
            break;
            
          default:
            resolve({
              success: false,
              actionId,
              error: `Unknown action: ${actionId}`
            });
        }
      }, 500);
    });
  }

  /**
   * Mock get available workflows for development without API
   * @returns {Promise<Array>} - Promise resolving to mock available workflows
   */
  async mockGetAvailableWorkflows() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          {
            id: 'onboarding',
            name: 'New Employee Onboarding',
            description: 'Workflow for onboarding new employees',
            steps: [
              { id: 'step1', actionId: 'create-account', name: 'Create Account' },
              { id: 'step2', actionId: 'assign-equipment', name: 'Assign Equipment' },
              { id: 'step3', actionId: 'schedule-orientation', name: 'Schedule Orientation' }
            ]
          },
          {
            id: 'document-approval',
            name: 'Document Approval',
            description: 'Workflow for document approval process',
            steps: [
              { id: 'step1', actionId: 'submit-document', name: 'Submit Document' },
              { id: 'step2', actionId: 'review-document', name: 'Review Document' },
              { id: 'step3', actionId: 'approve-document', name: 'Approve Document' }
            ]
          }
        ]);
      }, 300);
    });
  }
}

export default ActionService;