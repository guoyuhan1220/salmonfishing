import { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './ChatMessage.module.css';
import CitationTooltip from '../citations/CitationTooltip';
import SourceViewer from '../citations/SourceViewer';
import IntermediateStepsViewer from './IntermediateStepsViewer';
import LoadingIndicator from '../ui/LoadingIndicator';

function ChatMessage({ message }) {
  const [showSourceViewer, setShowSourceViewer] = useState(false);
  const [activeCitation, setActiveCitation] = useState(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });

  // Function to handle citation hover
  const handleCitationHover = (citation, event) => {
    setActiveCitation(citation);
    // Calculate position based on the event target
    const rect = event.target.getBoundingClientRect();
    setTooltipPosition({
      x: rect.left + window.scrollX,
      y: rect.bottom + window.scrollY
    });
  };

  // Function to handle citation hover end
  const handleCitationHoverEnd = () => {
    setActiveCitation(null);
  };

  // Function to toggle source viewer
  const toggleSourceViewer = () => {
    setShowSourceViewer(!showSourceViewer);
  };

  // Function to render text with citations
  const renderTextWithCitations = (text, citations) => {
    if (!citations || citations.length === 0) {
      return <p>{text}</p>;
    }

    // Replace citation markers [1], [2], etc. with interactive elements
    let parts = [];
    let lastIndex = 0;

    // Sort citations by their ID to ensure correct ordering
    const sortedCitations = [...citations].sort((a, b) => a.id - b.id);

    sortedCitations.forEach((citation, index) => {
      const marker = `[${citation.id}]`;
      const markerIndex = text.indexOf(marker, lastIndex);
      
      if (markerIndex !== -1) {
        // Add text before the citation marker
        if (markerIndex > lastIndex) {
          parts.push(
            <span key={`text-${index}`}>
              {text.substring(lastIndex, markerIndex)}
            </span>
          );
        }
        
        // Add the citation marker as an interactive element with enhanced styling
        parts.push(
          <span 
            key={`citation-${index}`}
            className={styles.citation}
            onMouseEnter={(e) => handleCitationHover(citation, e)}
            onMouseLeave={handleCitationHoverEnd}
            role="button"
            tabIndex={0}
            aria-label={`Citation ${citation.id}: ${citation.source.title}`}
            onClick={(e) => handleCitationHover(citation, e)}
            onKeyPress={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                handleCitationHover(citation, e);
              }
            }}
          >
            <sup className={styles.citationNumber}>{citation.id}</sup>
          </span>
        );
        
        lastIndex = markerIndex + marker.length;
      }
    });
    
    // Add any remaining text
    if (lastIndex < text.length) {
      parts.push(
        <span key="text-end">
          {text.substring(lastIndex)}
        </span>
      );
    }
    
    return <p>{parts}</p>;
  };

  // Determine message type classes
  const messageClasses = [
    styles.message,
    message.isUser ? styles.userMessage : styles.assistantMessage,
    message.isError ? styles.errorMessage : '',
    message.isSystem ? styles.systemMessage : ''
  ].filter(Boolean).join(' ');

  // Ref for the message content to scroll to bottom when streaming
  const messageContentRef = useRef(null);
  
  // Scroll to bottom when streaming new content
  useEffect(() => {
    if (messageContentRef.current && message.isStreaming) {
      messageContentRef.current.scrollTop = messageContentRef.current.scrollHeight;
    }
  }, [message.text, message.isStreaming]);

  return (
    <div className={messageClasses}>
      <div className={styles.avatar}>
        {message.isUser ? '' : '🤖'}
      </div>
      <div className={styles.content}>
        <div className={styles.sender}>
          {message.isUser ? 'You' : 'My assistant'}
        </div>
        <div className={styles.text} ref={messageContentRef}>
          {message.isGenerating && !message.text ? (
            <div className={styles.loadingContainer}>
              <LoadingIndicator 
                type="typing" 
                size="small" 
                text="Generating response" 
                theme="light" 
              />
            </div>
          ) : (
            <>
              {message.citations 
                ? renderTextWithCitations(message.text, message.citations)
                : <p>{message.text}</p>
              }
              
              {/* Show typing indicator at the end of streaming text */}
              {message.typingIndicator && (
                <div className={styles.typingIndicator}>
                  <span className={styles.typingDot}></span>
                  <span className={styles.typingDot}></span>
                  <span className={styles.typingDot}></span>
                </div>
              )}
            </>
          )}
          
          {/* Use IntermediateStepsViewer component for steps */}
          {message.intermediateSteps && (
            <IntermediateStepsViewer 
              steps={message.intermediateSteps} 
              isGenerating={message.isGenerating} 
            />
          )}
          
          {/* Show actions if available */}
          {message.actions && message.actions.length > 0 && (
            <div className={styles.actions}>
              {message.actions.map((action) => (
                <button 
                  key={action.id}
                  className={styles.actionButton}
                  onClick={action.handler}
                  title={action.description}
                >
                  {action.name}
                </button>
              ))}
            </div>
          )}
          
          {/* Show source button if citations are available */}
          {message.citations && message.citations.length > 0 && (
            <button 
              className={styles.sourceButton}
              onClick={toggleSourceViewer}
              aria-label={`View ${message.citations.length} sources`}
            >
              <span className={styles.sourceIcon}>📚</span>
              <span className={styles.sourceText}>Sources ({message.citations.length})</span>
            </button>
          )}
        </div>
        
        {/* Timestamp */}
        <div className={styles.timestamp}>
          {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </div>
      </div>
      
      {/* Citation tooltip */}
      {activeCitation && (
        <CitationTooltip 
          citation={activeCitation} 
          position={tooltipPosition} 
        />
      )}
      
      {/* Source viewer */}
      {showSourceViewer && (
        <SourceViewer 
          citations={message.citations} 
          onClose={toggleSourceViewer} 
        />
      )}
    </div>
  );
}

ChatMessage.propTypes = {
  message: PropTypes.shape({
    id: PropTypes.string.isRequired,
    text: PropTypes.string.isRequired,
    isUser: PropTypes.bool.isRequired,
    timestamp: PropTypes.instanceOf(Date).isRequired,
    citations: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.number.isRequired,
        text: PropTypes.string.isRequired,
        source: PropTypes.object.isRequired
      })
    ),
    intermediateSteps: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.number.isRequired,
        description: PropTypes.string.isRequired,
        content: PropTypes.string.isRequired
      })
    ),
    actions: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        handler: PropTypes.func.isRequired
      })
    ),
    isError: PropTypes.bool,
    isSystem: PropTypes.bool,
    isGenerating: PropTypes.bool,
    isStreaming: PropTypes.bool,
    typingIndicator: PropTypes.bool
  }).isRequired
};

export default ChatMessage;