/**
 * FileProcessingService - Service for handling file uploads and processing
 * 
 * This service handles file uploads, processing, and text extraction
 * for use in the chat interface.
 */

// Default configuration
const DEFAULT_CONFIG = {
  uploadEndpoint: '/api/upload',
  processEndpoint: '/api/process-file',
  extractTextEndpoint: '/api/extract-text',
  maxFileSizeMB: 10,
  supportedFileTypes: [
    'application/pdf',
    'text/plain',
    'text/csv',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/json',
    'text/markdown',
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'application/msword',
    'application/vnd.ms-excel',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/html'
  ]
};

/**
 * FileProcessingService class for file handling
 */
class FileProcessingService {
  /**
   * Create a new FileProcessingService instance
   * @param {Object} config - Configuration for the file processing service
   */
  constructor(config = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
  }

  /**
   * Validate a file before upload
   * @param {File} file - File to validate
   * @returns {Object} - Validation result with success flag and error message if applicable
   */
  validateFile(file) {
    // Check file size
    const maxSizeBytes = this.config.maxFileSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      return {
        valid: false,
        error: `File size (${(file.size / (1024 * 1024)).toFixed(2)}MB) exceeds the ${this.config.maxFileSizeMB}MB limit.`
      };
    }
    
    // Check file type
    if (!this.config.supportedFileTypes.includes(file.type)) {
      return {
        valid: false,
        error: `Unsupported file type: ${file.type}. Please upload a supported file type.`
      };
    }
    
    // Check if file is empty
    if (file.size === 0) {
      return {
        valid: false,
        error: 'File is empty. Please upload a valid file.'
      };
    }
    
    return { valid: true };
  }

  /**
   * Upload a file
   * @param {File} file - File to upload
   * @param {Function} onProgress - Progress callback
   * @returns {Promise<Object>} - Promise resolving to the upload result
   */
  async uploadFile(file, onProgress = null) {
    // Validate the file
    const validation = this.validateFile(file);
    if (!validation.valid) {
      throw new Error(validation.error);
    }
    
    try {
      // Create FormData for the file
      const formData = new FormData();
      formData.append('file', file);
      
      // Use XMLHttpRequest for progress tracking
      return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        
        // Set up progress tracking
        if (onProgress && typeof onProgress === 'function') {
          xhr.upload.addEventListener('progress', (event) => {
            if (event.lengthComputable) {
              const progress = Math.round((event.loaded / event.total) * 100);
              onProgress(progress);
            }
          });
        }
        
        // Set up completion handler
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const response = JSON.parse(xhr.responseText);
              resolve({
                success: true,
                fileId: response.fileId,
                name: file.name,
                type: file.type,
                size: file.size
              });
            } catch (error) {
              reject(new Error('Invalid response format'));
            }
          } else {
            reject(new Error(`Upload failed with status ${xhr.status}`));
          }
        };
        
        // Set up error handler
        xhr.onerror = () => {
          reject(new Error('Network error during upload'));
        };
        
        // Send the request
        xhr.open('POST', this.config.uploadEndpoint);
        xhr.send(formData);
      });
    } catch (error) {
      console.error('Error uploading file:', error);
      throw error;
    }
  }

  /**
   * Process an uploaded file
   * @param {string} fileId - ID of the uploaded file
   * @returns {Promise<Object>} - Promise resolving to the processing result
   */
  async processFile(fileId) {
    try {
      const response = await fetch(`${this.config.processEndpoint}/${fileId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`File processing failed with status ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error processing file:', error);
      throw error;
    }
  }

  /**
   * Extract text from a processed file
   * @param {string} fileId - ID of the processed file
   * @param {Object} options - Text extraction options
   * @returns {Promise<Object>} - Promise resolving to the extracted text
   */
  async extractText(fileId, options = {}) {
    try {
      const response = await fetch(`${this.config.extractTextEndpoint}/${fileId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(options)
      });

      if (!response.ok) {
        throw new Error(`Text extraction failed with status ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error extracting text:', error);
      throw error;
    }
  }

  /**
   * Mock upload file for development without API
   * @param {File} file - File to upload
   * @param {Function} onProgress - Progress callback
   * @returns {Promise<Object>} - Promise resolving to mock upload result
   */
  async mockUploadFile(file, onProgress = null) {
    return new Promise((resolve) => {
      // Validate the file
      const validation = this.validateFile(file);
      if (!validation.valid) {
        throw new Error(validation.error);
      }
      
      // Simulate upload progress
      let progress = 0;
      const progressInterval = setInterval(() => {
        progress += 5;
        if (progress > 100) progress = 100;
        
        if (onProgress && typeof onProgress === 'function') {
          onProgress(progress);
        }
        
        if (progress === 100) {
          clearInterval(progressInterval);
          
          // Simulate a short delay for server processing
          setTimeout(() => {
            resolve({
              success: true,
              fileId: `file_${Date.now()}`,
              name: file.name,
              type: file.type,
              size: file.size
            });
          }, 500);
        }
      }, 100);
    });
  }

  /**
   * Mock process file for development without API
   * @param {string} fileId - ID of the uploaded file
   * @returns {Promise<Object>} - Promise resolving to mock processing result
   */
  async mockProcessFile(fileId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: true,
          fileId,
          status: 'completed',
          metadata: {
            pageCount: Math.floor(Math.random() * 20) + 1,
            wordCount: Math.floor(Math.random() * 5000) + 100,
            extractedText: `Sample extracted text from file ${fileId}`
          }
        });
      }, 1500);
    });
  }

  /**
   * Mock extract text for development without API
   * @param {string} fileId - ID of the processed file
   * @returns {Promise<Object>} - Promise resolving to mock extracted text
   */
  async mockExtractText(fileId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: true,
          fileId,
          text: `This is sample extracted text from file ${fileId}. It contains multiple paragraphs of text that would normally be extracted from the actual file content. The text extraction process would analyze the file format and extract readable content for use in the chat interface.`,
          pages: [
            { page: 1, text: 'Sample text from page 1' },
            { page: 2, text: 'Sample text from page 2' },
            { page: 3, text: 'Sample text from page 3' }
          ]
        });
      }, 800);
    });
  }
}

export default FileProcessingService;