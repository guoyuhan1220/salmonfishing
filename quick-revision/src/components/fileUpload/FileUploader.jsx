import { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import styles from './FileUploader.module.css';
import LoadingIndicator from '../ui/LoadingIndicator';

function FileUploader({ onFileSelect, onCancel }) {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef(null);
  const modalRef = useRef(null);

  // Supported file types and size limit
  const supportedTypes = [
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
  ];
  const maxSizeMB = 10;
  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  // Handle click outside to close
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        onCancel();
      }
    };

    // Handle escape key to close
    const handleEscKey = (event) => {
      if (event.key === 'Escape') {
        onCancel();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscKey);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscKey);
    };
  }, [onCancel]);

  // Handle drag events
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  // Handle drop event
  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  // Handle file input change
  const handleChange = (e) => {
    e.preventDefault();
    
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  // Validate and set the file
  const validateAndSetFile = (file) => {
    setError('');
    
    // Check file type
    if (!supportedTypes.includes(file.type)) {
      setError(`Unsupported file type: ${file.type}. Please upload a supported file type.`);
      return false;
    }
    
    // Check file size
    if (file.size > maxSizeBytes) {
      setError(`File size (${(file.size / (1024 * 1024)).toFixed(2)}MB) exceeds the ${maxSizeMB}MB limit.`);
      return false;
    }
    
    // Check if file is empty
    if (file.size === 0) {
      setError('File is empty. Please upload a valid file.');
      return false;
    }
    
    // Check file name length
    if (file.name.length > 255) {
      setError('File name is too long. Please rename the file and try again.');
      return false;
    }
    
    setSelectedFile(file);
    return true;
  };

  // Handle button click to open file dialog
  const handleButtonClick = () => {
    fileInputRef.current.click();
  };

  // Handle upload button click
  const handleUpload = () => {
    if (selectedFile) {
      setIsUploading(true);
      setUploadProgress(0);
      
      // Simulate progress updates
      const progressInterval = setInterval(() => {
        setUploadProgress(prev => {
          const newProgress = prev + Math.random() * 15;
          return newProgress >= 100 ? 100 : newProgress;
        });
      }, 300);
      
      // In a real implementation, we would use the onProgress callback from FileProcessingService
      // For now, simulate a file upload with progress
      setTimeout(() => {
        clearInterval(progressInterval);
        setUploadProgress(100);
        
        // Small delay at 100% to show completion
        setTimeout(() => {
          onFileSelect(selectedFile);
          setIsUploading(false);
        }, 500);
      }, 2000);
    }
  };

  // Get file icon based on type
  const getFileIcon = (fileType) => {
    if (fileType.includes('pdf')) return '📄';
    if (fileType.includes('word')) return '📝';
    if (fileType.includes('excel') || fileType.includes('spreadsheet') || fileType.includes('csv')) return '📊';
    if (fileType.includes('image')) return '🖼️';
    if (fileType.includes('text') || fileType.includes('json') || fileType.includes('markdown')) return '📃';
    if (fileType.includes('html')) return '🌐';
    if (fileType.includes('powerpoint') || fileType.includes('presentation')) return '📊';
    return '📁';
  };

  // Format file size
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' bytes';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };
  
  // Get supported file types as a readable list
  const getSupportedFileTypesList = () => {
    const typeMap = {
      'application/pdf': 'PDF',
      'text/plain': 'TXT',
      'text/csv': 'CSV',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'DOCX',
      'application/msword': 'DOC',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'XLSX',
      'application/vnd.ms-excel': 'XLS',
      'application/json': 'JSON',
      'text/markdown': 'MD',
      'image/jpeg': 'JPG',
      'image/png': 'PNG',
      'image/gif': 'GIF',
      'image/webp': 'WEBP',
      'application/vnd.ms-powerpoint': 'PPT',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPTX',
      'text/html': 'HTML'
    };
    
    return supportedTypes.map(type => typeMap[type] || type).join(', ');
  };

  return (
    <div className={styles.overlay}>
      <div ref={modalRef} className={styles.modal}>
        <div className={styles.header}>
          <h3>Upload File</h3>
          <button className={styles.closeButton} onClick={onCancel} aria-label="Close">×</button>
        </div>
        
        <div className={styles.content}>
          {!selectedFile ? (
            <div 
              className={`${styles.dropzone} ${dragActive ? styles.active : ''}`}
              onDragEnter={handleDrag}
              onDragOver={handleDrag}
              onDragLeave={handleDrag}
              onDrop={handleDrop}
              aria-label="File upload dropzone"
            >
              <input
                ref={fileInputRef}
                type="file"
                className={styles.fileInput}
                onChange={handleChange}
                accept={supportedTypes.join(',')}
                aria-label="File input"
              />
              
              <div className={styles.dropzoneContent}>
                <div className={styles.uploadIcon}>📤</div>
                <p>Drag and drop your file here, or</p>
                <button 
                  type="button" 
                  className={styles.browseButton}
                  onClick={handleButtonClick}
                >
                  Browse files
                </button>
                <p className={styles.fileInfo}>
                  Supported formats: {getSupportedFileTypesList()}<br />
                  Maximum file size: {maxSizeMB}MB
                </p>
              </div>
            </div>
          ) : isUploading ? (
            <div className={styles.uploadingContainer}>
              <div className={styles.filePreview}>
                <div className={styles.fileIcon}>
                  {getFileIcon(selectedFile.type)}
                </div>
                <div className={styles.fileDetails}>
                  <div className={styles.fileName}>{selectedFile.name}</div>
                  <div className={styles.fileType}>{selectedFile.type}</div>
                  <div className={styles.fileSize}>{formatFileSize(selectedFile.size)}</div>
                </div>
              </div>
              
              <div className={styles.progressContainer}>
                <LoadingIndicator 
                  type="progress" 
                  size="medium" 
                  progress={uploadProgress} 
                  text={`Uploading ${selectedFile.name}`}
                  isIndeterminate={false}
                  theme="light"
                />
              </div>
            </div>
          ) : (
            <div className={styles.filePreview}>
              <div className={styles.fileIcon}>
                {getFileIcon(selectedFile.type)}
              </div>
              <div className={styles.fileDetails}>
                <div className={styles.fileName}>{selectedFile.name}</div>
                <div className={styles.fileType}>{selectedFile.type}</div>
                <div className={styles.fileSize}>{formatFileSize(selectedFile.size)}</div>
              </div>
              <button 
                className={styles.removeButton}
                onClick={() => setSelectedFile(null)}
                aria-label="Remove file"
              >
                ×
              </button>
            </div>
          )}
          
          {error && (
            <div className={styles.error} role="alert">
              {error}
            </div>
          )}
        </div>
        
        <div className={styles.footer}>
          <button 
            className={styles.cancelButton}
            onClick={onCancel}
          >
            Cancel
          </button>
          <button 
            className={`${styles.uploadButton} ${selectedFile && !isUploading ? styles.active : ''}`}
            disabled={!selectedFile || isUploading}
            onClick={handleUpload}
          >
            {isUploading ? 'Uploading...' : 'Upload'}
          </button>
        </div>
      </div>
    </div>
  );
}

FileUploader.propTypes = {
  onFileSelect: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired
};

export default FileUploader;