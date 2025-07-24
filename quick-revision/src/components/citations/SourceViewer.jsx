import { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import styles from './SourceViewer.module.css';
import { verifySource, openSource } from '../../utils/citationUtils';

function SourceViewer({ citations, onClose }) {
  const modalRef = useRef(null);
  const [verificationStatus, setVerificationStatus] = useState({});
  const [verifying, setVerifying] = useState({});

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

  // Get source type icon
  const getSourceIcon = (type) => {
    switch (type) {
      case 'web':
        return '🌐';
      case 'company':
        return '🏢';
      case 'file':
        return '📄';
      case 'ai':
        return '🤖';
      default:
        return '📌';
    }
  };

  // Get source type label
  const getSourceTypeLabel = (type) => {
    switch (type) {
      case 'web':
        return 'Web Content';
      case 'company':
        return 'Company Resource';
      case 'file':
        return 'Uploaded File';
      case 'ai':
        return 'AI Generated';
      default:
        return 'Unknown Source';
    }
  };

  // Function to handle source verification
  const handleVerifySource = async (source, citationId) => {
    if (!source) return;
    
    // Set verifying state
    setVerifying(prev => ({ ...prev, [citationId]: true }));
    
    try {
      // Call the utility function to verify the source
      const result = await verifySource(source);
      
      // Update verification status
      setVerificationStatus(prev => ({ 
        ...prev, 
        [citationId]: result 
      }));
    } catch (error) {
      console.error('Error verifying source:', error);
      setVerificationStatus(prev => ({ 
        ...prev, 
        [citationId]: { 
          verified: false, 
          reason: 'Verification failed', 
          error: error.message 
        } 
      }));
    } finally {
      // Clear verifying state
      setVerifying(prev => ({ ...prev, [citationId]: false }));
    }
  };

  // Function to handle opening source
  const handleOpenSource = async (source) => {
    if (!source) return;
    
    try {
      // Call the utility function to open the source
      await openSource(source);
    } catch (error) {
      console.error('Error opening source:', error);
      alert(`Failed to open source: ${error.message}`);
    }
  };

  // Render source details based on type
  const renderSourceDetails = (source) => {
    const sourceIcon = getSourceIcon(source.type);
    const sourceTypeLabel = getSourceTypeLabel(source.type);
    
    return (
      <div className={`${styles.sourceDetails} ${styles[source.type + 'Source']}`}>
        <div className={styles.sourceTypeHeader}>
          <span className={styles.sourceIcon}>{sourceIcon}</span>
          <span className={styles.sourceType}>{sourceTypeLabel}</span>
          <div className={styles.sourceActions}>
            <button 
              className={`${styles.verifyButton} ${verifying[citation.id] ? styles.verifying : ''} ${verificationStatus[citation.id]?.verified ? styles.verified : ''}`}
              onClick={() => handleVerifySource(source, citation.id)}
              disabled={verifying[citation.id]}
              title="Verify this source"
            >
              {verifying[citation.id] ? 'Verifying...' : 
               verificationStatus[citation.id]?.verified ? 'Verified ✓' : 
               verificationStatus[citation.id]?.verified === false ? 'Failed to Verify' : 
               'Verify'}
            </button>
          </div>
        </div>
        
        {/* Common source details */}
        {source.title && source.title !== sourceTypeLabel && (
          <div className={styles.sourceTitle}>{source.title}</div>
        )}
        
        {/* Type-specific details */}
        {source.type === 'web' && (
          <div className={styles.sourceDetails}>
            {source.url && (
              <>
                <a 
                  href={source.url} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className={styles.sourceUrl}
                >
                  {source.url}
                </a>
                <div className={styles.sourceLinkActions}>
                  <button 
                    className={styles.openButton}
                    onClick={() => handleOpenSource(source)}
                  >
                    Open Link
                  </button>
                </div>
              </>
            )}
          </div>
        )}
        
        {source.type === 'company' && (
          <div className={styles.sourceDetails}>
            {source.location && (
              <div className={styles.sourceLocation}>
                <span className={styles.locationLabel}>Location:</span> {source.location}
              </div>
            )}
            <div className={styles.sourceLinkActions}>
              <button 
                className={styles.openButton}
                onClick={() => handleOpenSource(source)}
              >
                Open Resource
              </button>
            </div>
          </div>
        )}
        
        {source.type === 'file' && (
          <div className={styles.sourceDetails}>
            {source.fileId && (
              <div className={styles.fileId}>
                <span className={styles.fileIdLabel}>File ID:</span> {source.fileId}
              </div>
            )}
            {source.location && (
              <div className={styles.sourceLocation}>
                <span className={styles.locationLabel}>Section:</span> {source.location}
              </div>
            )}
            <div className={styles.sourceLinkActions}>
              <button 
                className={styles.openButton}
                onClick={() => handleOpenSource(source)}
              >
                Open File
              </button>
            </div>
          </div>
        )}
        
        {source.type === 'ai' && (
          <div className={styles.sourceDetails}>
            {source.title && (
              <div className={styles.sourceModel}>{source.title}</div>
            )}
            <div className={styles.aiDisclaimer}>
              This information is generated by an AI model and may not be factually accurate.
            </div>
          </div>
        )}
        
        {/* Verification status */}
        {verificationStatus[citation.id] && (
          <div className={`${styles.verificationStatus} ${
            verificationStatus[citation.id].verified ? styles.statusVerified : 
            verificationStatus[citation.id].verified === false ? styles.statusFailed : 
            styles.statusNeutral
          }`}>
            {verificationStatus[citation.id].verified ? (
              <>
                <span className={styles.verifiedIcon}>✓</span>
                <div>
                  <div className={styles.verifiedText}>Source Verified</div>
                  {verificationStatus[citation.id].lastVerified && (
                    <div className={styles.verifiedTime}>
                      Verified on {new Date(verificationStatus[citation.id].lastVerified).toLocaleString()}
                    </div>
                  )}
                  {verificationStatus[citation.id].verificationMethod && (
                    <div className={styles.verificationMethod}>
                      Method: {verificationStatus[citation.id].verificationMethod}
                    </div>
                  )}
                </div>
              </>
            ) : verificationStatus[citation.id].verified === false ? (
              <>
                <span className={styles.failedIcon}>✗</span>
                <div>
                  <div className={styles.failedText}>Verification Failed</div>
                  <div className={styles.failedReason}>
                    {verificationStatus[citation.id].reason || 'Unknown reason'}
                  </div>
                </div>
              </>
            ) : (
              <>
                <span className={styles.neutralIcon}>ℹ</span>
                <div>
                  <div className={styles.neutralText}>Cannot Verify</div>
                  <div className={styles.neutralReason}>
                    {verificationStatus[citation.id].reason || 'This source type cannot be verified'}
                  </div>
                </div>
              </>
            )}
          </div>
        )}
        
        {/* Snippet (common to all types) */}
        {source.snippet && (
          <div className={styles.snippet}>
            <h4>Relevant Text:</h4>
            <p>"{source.snippet}"</p>
          </div>
        )}
      </div>
    );
  };

  // Group citations by source type
  const groupedCitations = citations.reduce((groups, citation) => {
    const type = citation.source.type;
    if (!groups[type]) {
      groups[type] = [];
    }
    groups[type].push(citation);
    return groups;
  }, {});

  // Order of source types to display
  const sourceTypeOrder = ['web', 'company', 'file', 'ai'];

  return (
    <div className={styles.overlay}>
      <div ref={modalRef} className={styles.modal}>
        <div className={styles.header}>
          <h3>Sources ({citations.length})</h3>
          <button className={styles.closeButton} onClick={onClose} aria-label="Close">×</button>
        </div>
        
        <div className={styles.content}>
          {/* Display citations grouped by type */}
          {sourceTypeOrder.map(type => {
            const typeCitations = groupedCitations[type] || [];
            if (typeCitations.length === 0) return null;
            
            return (
              <div key={type} className={styles.sourceGroup}>
                {typeCitations.map((citation) => (
                  <div key={citation.id} className={styles.citation}>
                    <div className={styles.citationHeader}>
                      <span className={styles.citationId}>{citation.id}</span>
                      <span className={styles.citationTitle}>{citation.text || citation.source.title}</span>
                    </div>
                    {renderSourceDetails(citation.source)}
                  </div>
                ))}
              </div>
            );
          })}
          
          {/* Show message if no citations */}
          {citations.length === 0 && (
            <div className={styles.noCitations}>
              No sources available for this response.
            </div>
          )}
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