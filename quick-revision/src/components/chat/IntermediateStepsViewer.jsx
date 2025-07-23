import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import styles from './IntermediateStepsViewer.module.css';

function IntermediateStepsViewer({ steps, isGenerating }) {
  const [visibleSteps, setVisibleSteps] = useState([]);
  const [collapsed, setCollapsed] = useState(false);

  // Update visible steps when steps change
  useEffect(() => {
    if (steps && steps.length > 0) {
      setVisibleSteps(steps);
    }
  }, [steps]);

  // Toggle collapsed state
  const toggleCollapsed = () => {
    setCollapsed(!collapsed);
  };

  // If no steps and not generating, don't render anything
  if ((!steps || steps.length === 0) && !isGenerating) {
    return null;
  }

  return (
    <div className={`${styles.container} ${collapsed ? styles.collapsed : ''}`}>
      <div className={styles.header} onClick={toggleCollapsed}>
        <h3>Reasoning Steps</h3>
        <button className={styles.toggleButton}>
          {collapsed ? '▼' : '▲'}
        </button>
      </div>
      
      {!collapsed && (
        <div className={styles.content}>
          {visibleSteps.length > 0 ? (
            visibleSteps.map((step) => (
              <div key={step.id} className={styles.step}>
                <div className={styles.stepHeader}>
                  <span className={styles.stepNumber}>{step.id}</span>
                  <span className={styles.stepDescription}>{step.description}</span>
                </div>
                <div className={styles.stepContent}>
                  {step.content}
                </div>
              </div>
            ))
          ) : (
            <div className={styles.loading}>
              <div className={styles.loadingText}>
                Thinking...
                <div className={styles.loadingDots}>
                  <span className={styles.dot}></span>
                  <span className={styles.dot}></span>
                  <span className={styles.dot}></span>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

IntermediateStepsViewer.propTypes = {
  steps: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      description: PropTypes.string.isRequired,
      content: PropTypes.string.isRequired
    })
  ),
  isGenerating: PropTypes.bool
};

IntermediateStepsViewer.defaultProps = {
  steps: [],
  isGenerating: true
};

export default IntermediateStepsViewer;