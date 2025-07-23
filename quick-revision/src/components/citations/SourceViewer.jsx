import { useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './SourceViewer.module.css';

function SourceViewer({ citations, onClose }) {
  const modalRef = useRef(null);

  // Handle click outside to close
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        onClose();
      }
    };

    // Handle escape key to close
    const handleEscKey = (event) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscKey);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscKey);
    };
  }, [onClose]);

  // Render source details based on type
  const renderSourceDetails = (source) => {
    switch (source.type) {
      case 'web':
        return (
          <div className={styles.sourceDetails}>
            <div className={styles.sourceType}>Web Content</div>
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
            {source.snippet && (
              <div className={styles.snippet}>
                <h4>Relevant Text:</h4>
                <p>"{source.snippet}"</p>
              </div>
            )}
          </div>
        );
      
      case 'company':
        return (
          <div className={styles.sourceDetails}>
            <div className={styles.sourceType}>Company Resource</div>
            {source.location && (
              <div className={styles.sourceLocation}>
                Location: {source.location}
              </div>
            )}
            {source.snippet && (
              <div className={styles.snippet}>
                <h4>Relevant Text:</h4>
                <p>"{source.snippet}"</p>
              </div>
            )}
          </div>
        );
      
      case 'file':
        return (
          <div className={styles.sourceDetails}>
            <div className={styles.sourceType}>Uploaded File</div>
            {source.location && (
              <div className={styles.sourceLocation}>
                Location: {source.location}
              </div>
            )}
            {source.snippet && (
              <div className={styles.snippet}>
                <h4>Relevant Text:</h4>
                <p>"{source.snippet}"</p>
              </div>
            )}
          </div>
        );
      
      case 'ai':
        return (
          <div className={styles.sourceDetails}>
            <div className={styles.sourceType}>AI Generated</div>
            {source.title && (
              <div className={styles.sourceModel}>{source.title}</div>
            )}
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className={styles.overlay}>
      <div ref={modalRef} className={styles.modal}>
        <div className={styles.header}>
          <h3>Sources</h3>
          <button className={styles.closeButton} onClick={onClose}>×</button>
        </div>
        <div className={styles.content}>
          {citations.map((citation) => (
            <div key={citation.id} className={styles.citation}>
              <div className={styles.citationHeader}>
                <span className={styles.citationId}>[{citation.id}]</span>
                <span className={styles.citationTitle}>{citation.source.title}</span>
              </div>
              {renderSourceDetails(citation.source)}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

SourceViewer.propTypes = {
  citations: PropTypes.arrayOf(
    PropTypes.shape({
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
    })
  ).isRequired,
  onClose: PropTypes.func.isRequired
};

export default SourceViewer;