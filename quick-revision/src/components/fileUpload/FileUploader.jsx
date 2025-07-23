import { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import styles from './FileUploader.module.css';

function FileUploader({ onFileSelect, onCancel }) {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
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
    'image/png'
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
      setError(`Unsupported file type. Please upload a supported file type.`);
      return;
    }
    
    // Check file size
    if (file.size > maxSizeBytes) {
      setError(`File size exceeds the ${maxSizeMB}MB limit.`);
      return;
    }
    
    setSelectedFile(file);
  };

  // Handle button click to open file dialog
  const handleButtonClick = () => {
    fileInputRef.current.click();
  };

  // Handle upload button click
  const handleUpload = () => {
    if (selectedFile) {
      onFileSelect(selectedFile);
    }
  };

  // Get file icon based on type
  const getFileIcon = (fileType) => {
    if (fileType.includes('pdf')) return '📄';
    if (fileType.includes('word')) return '📝';
    if (fileType.includes('excel') || fileType.includes('spreadsheet') || fileType.includes('csv')) return '📊';
    if (fileType.includes('image')) return '🖼️';
    if (fileType.includes('text') || fileType.includes('json') || fileType.includes('markdown')) return '📃';
    return '📁';
  };

  // Format file size
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' bytes';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  return (
    <div className={styles.overlay}>
      <div ref={modalRef} className={styles.modal}>
        <div className={styles.header}>
          <h3>Upload File</h3>
          <button className={styles.closeButton} onClick={onCancel}>×</button>
        </div>
        
        <div className={styles.content}>
          {!selectedFile ? (
            <div 
              className={`${styles.dropzone} ${dragActive ? styles.active : ''}`}
              onDragEnter={handleDrag}
              onDragOver={handleDrag}
              onDragLeave={handleDrag}
              onDrop={handleDrop}
            >
              <input
                ref={fileInputRef}
                type="file"
                className={styles.fileInput}
                onChange={handleChange}
                accept={supportedTypes.join(',')}
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
                  Supported formats: PDF, TXT, CSV, DOCX, XLSX, JSON, MD, JPG, PNG<br />
                  Maximum file size: {maxSizeMB}MB
                </p>
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
              >
                ×
              </button>
            </div>
          )}
          
          {error && (
            <div className={styles.error}>
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
            className={`${styles.uploadButton} ${selectedFile ? styles.active : ''}`}
            disabled={!selectedFile}
            onClick={handleUpload}
          >
            Upload
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