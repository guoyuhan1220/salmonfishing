import { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useChatContext } from '../../contexts/ChatContext';
import FileUploader from '../fileUpload/FileUploader';
import styles from './ChatInput.module.css';

function ChatInput() {
  const { sendMessage, uploadFile, isGenerating } = useChatContext();
  const [message, setMessage] = useState('');
  const [showFileUploader, setShowFileUploader] = useState(false);
  const [activeMode, setActiveMode] = useState('ai'); // 'ai', 'resource', or 'web'
  const fileInputRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (message.trim() && !isGenerating) {
      sendMessage(message.trim());
      setMessage('');
    }
  };

  const handleKeyDown = (e) => {
    // Submit on Enter (but not with Shift+Enter for multiline)
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const handleFileButtonClick = () => {
    setShowFileUploader(true);
  };

  const handleFileSelect = (file) => {
    if (file) {
      uploadFile(file);
    }
    setShowFileUploader(false);
  };

  const handleFileCancel = () => {
    setShowFileUploader(false);
  };
  
  // Get placeholder text based on active mode
  const getPlaceholder = () => {
    if (isGenerating) return "Thinking...";
    
    switch (activeMode) {
      case 'ai':
        return "Ask a question";
      case 'resource':
        return "Search for resources...";
      case 'web':
        return "Search the web...";
      default:
        return "Ask a question";
    }
  };

  return (
    <>
      <form 
        className={`${styles.inputContainer} ${isGenerating ? styles.disabled : ''}`} 
        onSubmit={handleSubmit}
      >
        <textarea
          className={styles.input}
          placeholder={getPlaceholder()}
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={isGenerating}
          rows={1}
          autoFocus
        />
        <div className={styles.bottomBar}>
          <div className={styles.leftSection}>
            <div className={styles.buttonGroup}>
              <button 
                type="button" 
                className={`${styles.buttonGroupItem} ${activeMode === 'ai' ? styles.active : ''}`}
                title="AI Mode"
                onClick={() => setActiveMode('ai')}
                disabled={isGenerating}
              >
                <img src="./src/icons/ai mode.svg" alt="AI Mode" className={styles.buttonGroupIcon} />
              </button>
              <button 
                type="button" 
                className={`${styles.buttonGroupItem} ${activeMode === 'resource' ? styles.active : ''}`}
                title="Quick Resources"
                onClick={() => setActiveMode('resource')}
                disabled={isGenerating}
              >
                <img src="./src/icons/quick resource.svg" alt="Resources" className={styles.buttonGroupIcon} />
              </button>
              <button 
                type="button" 
                className={`${styles.buttonGroupItem} ${activeMode === 'web' ? styles.active : ''}`}
                title="Web Search"
                onClick={() => setActiveMode('web')}
                disabled={isGenerating}
              >
                <img src="./src/icons/globe.svg" alt="Web" className={styles.buttonGroupIcon} />
              </button>
            </div>
            
            <div className={styles.divider}></div>
            
            <div className={styles.actions}>
              <button 
                type="button" 
                className={styles.actionButton} 
                title="Upload file"
                onClick={handleFileButtonClick}
                disabled={isGenerating}
              >
                <img src="./src/icons/paperclip.svg" alt="Upload" className={styles.actionIcon} />
              </button>
              <button 
                type="button" 
                className={styles.actionButton} 
                title="Action"
                disabled={isGenerating}
              >
                <img src="./src/icons/action.svg" alt="Action" className={styles.actionIcon} />
              </button>
              <button 
                type="button" 
                className={styles.actionButton} 
                title="Flow"
                disabled={isGenerating}
              >
                <img src="./src/icons/flow.svg" alt="Flow" className={styles.actionIcon} />
              </button>
            </div>
          </div>
          <button 
            type="submit" 
            className={`${styles.sendButton} ${message.trim() && !isGenerating ? styles.active : ''}`}
            disabled={isGenerating}
          >
            {isGenerating ? 
              <span className={styles.loadingIcon}>⏳</span> : 
              <>
                <img src="./src/icons/Send.svg" alt="Send" className={styles.sendIcon} />
                <span className={styles.sendText}>Send</span>
              </>
            }
          </button>
        </div>
      </form>

      {/* File uploader modal */}
      {showFileUploader && (
        <FileUploader 
          onFileSelect={handleFileSelect}
          onCancel={handleFileCancel}
        />
      )}
    </>
  );
}

export default ChatInput;