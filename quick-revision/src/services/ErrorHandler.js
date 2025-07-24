/**
 * ErrorHandler service for managing application errors
 * Provides user-friendly error messages and retry mechanisms
 */
class ErrorHandler {
  /**
   * Handle an error and return user-friendly information
   * @param {Error} error - The error object
   * @param {string} context - The context where the error occurred
   * @returns {Object} Error information with user message and recovery options
   */
  static handleError(error, context) {
    // Log error for debugging
    console.error(`Error in ${context}:`, error);

    // Determine error type and return appropriate response
    if (this.isNetworkError(error)) {
      return {
        userMessage: "Network connection issue. Please check your internet connection.",
        recoverable: true,
        retryAction: () => {
          // Return a function that can be called to retry the operation
          return Promise.resolve({ retried: true, context });
        }
      };
    }

    if (this.isServiceError(error)) {
      return {
        userMessage: "Service temporarily unavailable. Please try again later.",
        recoverable: true,
        retryAction: () => {
          // Return a function that can be called to retry the operation
          return Promise.resolve({ retried: true, context });
        }
      };
    }

    if (this.isFileError(error)) {
      return this.handleFileError(error);
    }

    if (this.isActionError(error)) {
      return this.handleActionError(error);
    }

    // Default error handling
    return {
      userMessage: "Something went wrong. Please try again.",
      recoverable: false
    };
  }

  /**
   * Check if the error is a network error
   * @param {Error} error - The error object
   * @returns {boolean} True if it's a network error
   */
  static isNetworkError(error) {
    return (
      error.name === 'NetworkError' || 
      error.message.includes('network') ||
      error.message.includes('Network') ||
      error.message.includes('connection') ||
      error.message.includes('Connection') ||
      error.message.includes('timeout') ||
      error.message.includes('Timeout') ||
      error.code === 'NETWORK_ERROR' ||
      error.status === 0 ||
      error instanceof TypeError && error.message.includes('fetch')
    );
  }

  /**
   * Check if the error is a service error
   * @param {Error} error - The error object
   * @returns {boolean} True if it's a service error
   */
  static isServiceError(error) {
    return (
      error.name === 'ServiceError' ||
      error.message.includes('service') ||
      error.message.includes('Service') ||
      error.code === 'SERVICE_ERROR' ||
      (error.status && error.status >= 500 && error.status < 600)
    );
  }

  /**
   * Check if the error is related to file operations
   * @param {Error} error - The error object
   * @returns {boolean} True if it's a file error
   */
  static isFileError(error) {
    return (
      error.name === 'FileError' ||
      error.message.includes('file') ||
      error.message.includes('File') ||
      error.code === 'FILE_ERROR' ||
      error.fileError === true
    );
  }

  /**
   * Handle file-related errors
   * @param {Error} error - The file error object
   * @returns {Object} Error information with user message and recovery options
   */
  static handleFileError(error) {
    // Check for specific file error types
    if (error.code === 'FILE_TOO_LARGE' || error.message.includes('too large')) {
      return {
        userMessage: "The file is too large. Please upload a smaller file (maximum size: 10MB).",
        recoverable: false
      };
    }

    if (error.code === 'UNSUPPORTED_FILE_TYPE' || error.message.includes('unsupported')) {
      return {
        userMessage: "Unsupported file type. Please upload a supported file type (PDF, DOCX, TXT, CSV, JSON).",
        recoverable: false
      };
    }

    if (error.code === 'FILE_UPLOAD_FAILED' || error.message.includes('upload failed')) {
      return {
        userMessage: "File upload failed. Please try again.",
        recoverable: true,
        retryAction: error.retryAction || (() => Promise.resolve({ retried: true }))
      };
    }

    if (error.code === 'FILE_PROCESSING_FAILED' || error.message.includes('processing failed')) {
      return {
        userMessage: "File processing failed. Please try again or upload a different file.",
        recoverable: true,
        retryAction: error.retryAction || (() => Promise.resolve({ retried: true }))
      };
    }

    // Default file error
    return {
      userMessage: "There was an issue with your file. Please try again with a different file.",
      recoverable: false
    };
  }

  /**
   * Check if the error is related to actions or workflows
   * @param {Error} error - The error object
   * @returns {boolean} True if it's an action error
   */
  static isActionError(error) {
    return (
      error.name === 'ActionError' ||
      error.message.includes('action') ||
      error.message.includes('Action') ||
      error.message.includes('workflow') ||
      error.message.includes('Workflow') ||
      error.code === 'ACTION_ERROR' ||
      error.actionError === true
    );
  }

  /**
   * Handle action-related errors
   * @param {Error} error - The action error object
   * @returns {Object} Error information with user message and recovery options
   */
  static handleActionError(error) {
    // Check for specific action error types
    if (error.code === 'ACTION_NOT_AVAILABLE' || error.message.includes('not available')) {
      return {
        userMessage: "This action is not available at the moment. Please try again later.",
        recoverable: false
      };
    }

    if (error.code === 'ACTION_FAILED' || error.message.includes('failed')) {
      return {
        userMessage: "The action failed to complete. Please try again.",
        recoverable: true,
        retryAction: error.retryAction || (() => Promise.resolve({ retried: true })),
        recoverySteps: error.recoverySteps || ["Try the action again", "Check your inputs", "Contact support if the issue persists"]
      };
    }

    if (error.code === 'WORKFLOW_INTERRUPTED' || error.message.includes('interrupted')) {
      return {
        userMessage: "The workflow was interrupted. You can continue where you left off or start over.",
        recoverable: true,
        retryAction: error.retryAction || (() => Promise.resolve({ retried: true })),
        continueAction: error.continueAction || (() => Promise.resolve({ continued: true }))
      };
    }

    // Default action error
    return {
      userMessage: "There was an issue with this action. Please try again later.",
      recoverable: false
    };
  }

  /**
   * Create a custom error with additional metadata
   * @param {string} message - Error message
   * @param {string} code - Error code
   * @param {Object} metadata - Additional error metadata
   * @returns {Error} Custom error object
   */
  static createError(message, code, metadata = {}) {
    const error = new Error(message);
    error.code = code;
    return Object.assign(error, metadata);
  }

  /**
   * Create a network error
   * @param {string} message - Error message
   * @param {Object} metadata - Additional error metadata
   * @returns {Error} Network error object
   */
  static createNetworkError(message, metadata = {}) {
    return this.createError(message, 'NETWORK_ERROR', { 
      name: 'NetworkError',
      ...metadata 
    });
  }

  /**
   * Create a service error
   * @param {string} message - Error message
   * @param {Object} metadata - Additional error metadata
   * @returns {Error} Service error object
   */
  static createServiceError(message, metadata = {}) {
    return this.createError(message, 'SERVICE_ERROR', { 
      name: 'ServiceError',
      ...metadata 
    });
  }

  /**
   * Create a file error
   * @param {string} message - Error message
   * @param {string} code - Specific file error code
   * @param {Object} metadata - Additional error metadata
   * @returns {Error} File error object
   */
  static createFileError(message, code = 'FILE_ERROR', metadata = {}) {
    return this.createError(message, code, { 
      name: 'FileError',
      fileError: true,
      ...metadata 
    });
  }

  /**
   * Create an action error
   * @param {string} message - Error message
   * @param {string} code - Specific action error code
   * @param {Object} metadata - Additional error metadata
   * @returns {Error} Action error object
   */
  static createActionError(message, code = 'ACTION_ERROR', metadata = {}) {
    return this.createError(message, code, { 
      name: 'ActionError',
      actionError: true,
      ...metadata 
    });
  }
}

export default ErrorHandler;