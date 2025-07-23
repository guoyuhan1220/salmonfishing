import { useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './CitationTooltip.module.css';

function CitationTooltip({ citation, position }) {
  const tooltipRef = useRef(null);

  // Adjust position to ensure tooltip stays within viewport
  useEffect(() => {
    if (!tooltipRef.current) return;

    const tooltip = tooltipRef.current;
    const rect = tooltip.getBoundingClientRect();
    
    // Check if tooltip would go off the right edge of the screen
    if (position.x + rect.width > window.innerWidth) {
      tooltip.style.left = `${window.innerWidth - rect.width - 10}px`;
    } else {
      tooltip.style.left = `${position.x}px`;
    }
    
    // Check if tooltip would go off the bottom of the screen
    if (position.y + rect.height > window.innerHeight) {
      tooltip.style.top = `${position.y - rect.height - 10}px`;
    } else {
      tooltip.style.top = `${position.y + 10}px`;
    }
  }, [position]);

  // Render source information based on type
  const renderSourceInfo = () => {
    const { source } = citation;
    
    switch (source.type) {
      case 'web':
        return (
          <>
            <div className={styles.sourceTitle}>{source.title}</div>
            {source.url && (
              <a 
                href={source.url} 
                target="_blank" 
                rel="noopener noreferrer"
                className={styles.sourceUrl}
              >
                {source.url}
              </a>
            )}
          </>
        );
      
      case 'company':
        return (
          <>
            <div className={styles.sourceTitle}>{source.title}</div>
            <div className={styles.sourceType}>Company Resource</div>
          </>
        );
      
      case 'file':
        return (
          <>
            <div className={styles.sourceTitle}>{source.title}</div>
            {source.location && (
              <div className={styles.sourceLocation}>
                Location: {source.location}
              </div>
            )}
          </>
        );
      
      case 'ai':
        return (
          <>
            <div className={styles.sourceTitle}>AI Generated</div>
            {source.title && (
              <div className={styles.sourceModel}>{source.title}</div>
            )}
          </>
        );
      
      default:
        return <div className={styles.sourceTitle}>{source.title}</div>;
    }
  };

  return (
    <div 
      ref={tooltipRef}
      className={styles.tooltip}
      style={{
        position: 'absolute',
        left: position.x,
        top: position.y + 10
      }}
    >
      <div className={styles.tooltipContent}>
        <div className={styles.citationId}>Source {citation.id}</div>
        {renderSourceInfo()}
        {citation.source.snippet && (
          <div className={styles.snippet}>
            "{citation.source.snippet}"
          </div>
        )}
      </div>
    </div>
  );
}

CitationTooltip.propTypes = {
  citation: PropTypes.shape({
    id: PropTypes.number.isRequired,
    text: PropTypes.string.isRequired,
    source: PropTypes.shape({
      type: PropTypes.oneOf(['web', 'company', 'file', 'ai']).isRequired,
      title: PropTypes.string.isRequired,
      url: PropTypes.string,
      fileId: PropTypes.string,
      location: PropTypes.string,
      snippet: PropTypes.string
    }).isRequired
  }).isRequired,
  position: PropTypes.shape({
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired
  }).isRequired
};

export default CitationTooltip;