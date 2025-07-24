import { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './IntermediateStepsViewer.module.css';
import { useUIContext } from '../../contexts/UIContext';
import LoadingIndicator from '../ui/LoadingIndicator';

function IntermediateStepsViewer({ steps, isGenerating }) {
  const { showIntermediateSteps, toggleIntermediateSteps } = useUIContext();
  const [visibleSteps, setVisibleSteps] = useState([]);
  const [collapsed, setCollapsed] = useState(false);
  const [expandedSteps, setExpandedSteps] = useState({});
  const contentRef = useRef(null);

  // Update visible steps when steps change
  useEffect(() => {
    if (steps && steps.length > 0) {
      setVisibleSteps(steps);
      
      // Initialize expanded state for new steps
      const newExpandedState = {};
      steps.forEach(step => {
        // If we already have a state for this step, keep it, otherwise default to expanded
        if (expandedSteps[step.id] === undefined) {
          newExpandedState[step.id] = true;
        } else {
          newExpandedState[step.id] = expandedSteps[step.id];
        }
      });
      setExpandedSteps(newExpandedState);
    }
  }, [steps]);

  // Scroll to bottom when new steps are added
  useEffect(() => {
    if (contentRef.current && !collapsed && visibleSteps.length > 0) {
      contentRef.current.scrollTop = contentRef.current.scrollHeight;
    }
  }, [visibleSteps.length, collapsed]);

  // Toggle collapsed state for the entire component
  const toggleCollapsed = () => {
    setCollapsed(!collapsed);
  };

  // Toggle expanded state for an individual step
  const toggleStepExpanded = (stepId) => {
    setExpandedSteps(prev => ({
      ...prev,
      [stepId]: !prev[stepId]
    }));
  };

  // Expand all steps
  const expandAllSteps = () => {
    const newExpandedState = {};
    visibleSteps.forEach(step => {
      newExpandedState[step.id] = true;
    });
    setExpandedSteps(newExpandedState);
  };

  // Collapse all steps
  const collapseAllSteps = () => {
    const newExpandedState = {};
    visibleSteps.forEach(step => {
      newExpandedState[step.id] = false;
    });
    setExpandedSteps(newExpandedState);
  };

  // If no steps and not generating, don't render anything
  if ((!steps || steps.length === 0) && !isGenerating) {
    return null;
  }

  // If user has disabled intermediate steps, show toggle button only
  if (!showIntermediateSteps) {
    return (
      <div className={styles.stepsToggleContainer}>
        <button 
          className={styles.stepsToggleButton}
          onClick={toggleIntermediateSteps}
          aria-label="Show reasoning steps"
        >
          <span className={styles.stepsToggleIcon}>🔍</span>
          <span className={styles.stepsToggleText}>Show reasoning steps</span>
        </button>
      </div>
    );
  }

  // Format calculation content with special styling
  const formatContent = (content) => {
    // Check if content contains calculations (simple detection for math operations)
    const hasCalculations = /[+\-*/=]/.test(content);
    
    if (hasCalculations) {
      return (
        <div className={styles.calculation}>
          {content}
        </div>
      );
    }
    
    // Format code blocks
    if (content.includes('```')) {
      const parts = content.split(/```(?:(\w+))?/);
      const formattedParts = [];
      
      for (let i = 0; i < parts.length; i++) {
        if (i % 3 === 0) {
          // Regular text
          if (parts[i]) {
            formattedParts.push(<span key={`text-${i}`}>{parts[i]}</span>);
          }
        } else if (i % 3 === 1) {
          // Language identifier (optional)
          // Skip this part
        } else {
          // Code block
          formattedParts.push(
            <pre key={`code-${i}`} className={styles.codeBlock}>
              <code>{parts[i]}</code>
            </pre>
          );
        }
      }
      
      return <div>{formattedParts}</div>;
    }
    
    return content;
  };

  return (
    <div className={`${styles.container} ${collapsed ? styles.collapsed : ''}`}>
      <div className={styles.header}>
        <div className={styles.headerLeft} onClick={toggleCollapsed}>
          <h3>Reasoning Steps {visibleSteps.length > 0 && `(${visibleSteps.length})`}</h3>
          <button 
            className={styles.toggleButton}
            aria-label={collapsed ? "Expand reasoning steps" : "Collapse reasoning steps"}
          >
            {collapsed ? '▼' : '▲'}
          </button>
        </div>
        <div className={styles.headerRight}>
          <button 
            className={styles.hideStepsButton}
            onClick={toggleIntermediateSteps}
            aria-label="Hide reasoning steps"
            title="Hide reasoning steps"
          >
            <span>✕</span>
          </button>
        </div>
      </div>
      
      {!collapsed && (
        <>
          <div className={styles.toolbar}>
            <button 
              className={styles.toolbarButton}
              onClick={expandAllSteps}
              aria-label="Expand all steps"
              title="Expand all steps"
            >
              Expand all
            </button>
            <button 
              className={styles.toolbarButton}
              onClick={collapseAllSteps}
              aria-label="Collapse all steps"
              title="Collapse all steps"
            >
              Collapse all
            </button>
          </div>
          
          <div className={styles.content} ref={contentRef}>
            {visibleSteps.length > 0 ? (
              visibleSteps.map((step) => (
                <div key={step.id} className={styles.step}>
                  <div 
                    className={styles.stepHeader} 
                    onClick={() => toggleStepExpanded(step.id)}
                  >
                    <span className={styles.stepNumber}>{step.id}</span>
                    <span className={styles.stepDescription}>{step.description}</span>
                    <button 
                      className={styles.stepToggle}
                      aria-label={expandedSteps[step.id] ? "Collapse step" : "Expand step"}
                    >
                      {expandedSteps[step.id] ? '▲' : '▼'}
                    </button>
                  </div>
                  {expandedSteps[step.id] && (
                    <div className={styles.stepContent}>
                      {formatContent(step.content)}
                    </div>
                  )}
                </div>
              ))
            ) : (
              <div className={styles.loading}>
                <LoadingIndicator 
                  type="dots" 
                  size="small" 
                  text="Thinking..." 
                  theme="light" 
                />
              </div>
            )}
            
            {isGenerating && visibleSteps.length > 0 && (
              <div className={styles.generatingIndicator}>
                <LoadingIndicator 
                  type="dots" 
                  size="small" 
                  text="Still thinking..." 
                  theme="light" 
                />
              </div>
            )}
            
            {!isGenerating && visibleSteps.length > 0 && (
              <div className={styles.completionIndicator}>
                All steps completed
              </div>
            )}
          </div>
        </>
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
  isGenerating: false
};

export default IntermediateStepsViewer;