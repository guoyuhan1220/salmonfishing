import React from 'react';
import PropTypes from 'prop-types';
import styles from './LoadingIndicator.module.css';

/**
 * LoadingIndicator component for displaying loading states and progress
 * Can be used for message generation, file uploads, and other async operations
 */
function LoadingIndicator({ 
  type = 'spinner', 
  size = 'medium', 
  progress = null, 
  text = null,
  isIndeterminate = true,
  theme = 'light'
}) {
  // Determine the appropriate CSS classes based on props
  const containerClasses = [
    styles.container,
    styles[size],
    styles[`theme-${theme}`]
  ].join(' ');

  // Render different types of loading indicators
  const renderLoadingIndicator = () => {
    switch (type) {
      case 'spinner':
        return (
          <div className={styles.spinner} role="status" aria-label={text || 'Loading'}>
            <div className={styles.spinnerInner}></div>
          </div>
        );
      
      case 'dots':
        return (
          <div className={styles.dots} role="status" aria-label={text || 'Loading'}>
            <div className={styles.dot}></div>
            <div className={styles.dot}></div>
            <div className={styles.dot}></div>
          </div>
        );
      
      case 'progress':
        return (
          <div className={styles.progressContainer} role="progressbar" 
            aria-valuenow={isIndeterminate ? null : Math.round(progress)} 
            aria-valuemin="0" 
            aria-valuemax="100"
            aria-label={text || 'Loading'}>
            <div className={`${styles.progressBar} ${isIndeterminate ? styles.indeterminate : ''}`} 
              style={!isIndeterminate ? { width: `${progress}%` } : {}}>
            </div>
          </div>
        );
      
      case 'pulse':
        return (
          <div className={styles.pulse} role="status" aria-label={text || 'Loading'}>
            <div className={styles.pulseRing}></div>
          </div>
        );
      
      case 'typing':
        return (
          <div className={styles.typing} role="status" aria-label={text || 'Typing'}>
            <div className={styles.typingDot}></div>
            <div className={styles.typingDot}></div>
            <div className={styles.typingDot}></div>
          </div>
        );
      
      default:
        return (
          <div className={styles.spinner} role="status" aria-label={text || 'Loading'}>
            <div className={styles.spinnerInner}></div>
          </div>
        );
    }
  };

  return (
    <div className={containerClasses}>
      {renderLoadingIndicator()}
      {text && <div className={styles.text}>{text}</div>}
      {!isIndeterminate && type === 'progress' && progress !== null && (
        <div className={styles.progressText}>{Math.round(progress)}%</div>
      )}
    </div>
  );
}

LoadingIndicator.propTypes = {
  /** Type of loading indicator to display */
  type: PropTypes.oneOf(['spinner', 'dots', 'progress', 'pulse', 'typing']),
  
  /** Size of the loading indicator */
  size: PropTypes.oneOf(['small', 'medium', 'large']),
  
  /** Progress value (0-100) for progress bar type */
  progress: PropTypes.number,
  
  /** Optional text to display with the loading indicator */
  text: PropTypes.string,
  
  /** Whether the progress bar should be indeterminate */
  isIndeterminate: PropTypes.bool,
  
  /** Theme for the loading indicator */
  theme: PropTypes.oneOf(['light', 'dark'])
};

export default LoadingIndicator;