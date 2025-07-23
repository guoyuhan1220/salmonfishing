import { useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useUIContext } from '../../contexts/UIContext';
import ChatMessage from './ChatMessage';
import IntermediateStepsViewer from './IntermediateStepsViewer';
import styles from './ChatHistory.module.css';

function ChatHistory({ messages, isGenerating }) {
  const messagesEndRef = useRef(null);
  // Get UI context with a default value for showIntermediateSteps
  const { showIntermediateSteps = false } = useUIContext() || {};

  // Auto-scroll to bottom when new messages arrive or when generating
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isGenerating]);

  // If no messages, show empty state
  if (messages.length === 0) {
    return (
      <div className={styles.emptyState}>
        <div className={styles.emptyIcon}>💬</div>
        <h3>No messages yet</h3>
        <p>Start a conversation by sending a message below.</p>
      </div>
    );
  }

  return (
    <div className={styles.history}>
      {messages.map((msg, index) => (
        <ChatMessage 
          key={msg.id || index} 
          message={msg} 
        />
      ))}
      
      {/* Show typing indicator when generating response */}
      {isGenerating && (
        <div className={styles.typingIndicator}>
          <div className={styles.typingAvatar}>🤖</div>
          <div className={styles.typingContent}>
            <div className={styles.typingText}>
              <span className={styles.dot}></span>
              <span className={styles.dot}></span>
              <span className={styles.dot}></span>
            </div>
          </div>
        </div>
      )}
      
      {/* Show intermediate steps viewer if enabled and generating */}
      {isGenerating && showIntermediateSteps && (
        <IntermediateStepsViewer />
      )}
      
      <div ref={messagesEndRef} />
    </div>
  );
}

ChatHistory.propTypes = {
  messages: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      text: PropTypes.string.isRequired,
      isUser: PropTypes.bool.isRequired,
      timestamp: PropTypes.instanceOf(Date).isRequired,
      citations: PropTypes.array,
      intermediateSteps: PropTypes.array,
      actions: PropTypes.array,
      isError: PropTypes.bool,
      isSystem: PropTypes.bool
    })
  ),
  isGenerating: PropTypes.bool
};

ChatHistory.defaultProps = {
  messages: [],
  isGenerating: false
};

export default ChatHistory;