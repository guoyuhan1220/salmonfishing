import { useState } from 'react';
import PropTypes from 'prop-types';
import styles from './ActionPanel.module.css';

/**
 * ActionPanel component displays a set of actionable buttons or links
 * identified by the AI assistant.
 */
function ActionPanel({ actions, onActionSelect }) {
  const [expanded, setExpanded] = useState(false);

  // Toggle expanded state
  const toggleExpanded = () => {
    setExpanded(!expanded);
  };

  // Handle action click
  const handleActionClick = (action) => {
    onActionSelect(action);
  };

  // If no actions, don't render anything
  if (!actions || actions.length === 0) {
    return null;
  }

  return (
    <div className={`${styles.container} ${expanded ? styles.expanded : ''}`}>
      <div className={styles.header} onClick={toggleExpanded}>
        <h3>Suggested Actions</h3>
        <button className={styles.toggleButton}>
          {expanded ? '▼' : '▲'}
        </button>
      </div>
      
      {expanded && (
        <div className={styles.content}>
          <div className={styles.actionList}>
            {actions.map((action) => (
              <button
                key={action.id}
                className={styles.actionButton}
                onClick={() => handleActionClick(action)}
                title={action.description}
              >
                {action.icon && (
                  <span className={styles.actionIcon}>{action.icon}</span>
                )}
                <span className={styles.actionName}>{action.name}</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

ActionPanel.propTypes = {
  actions: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
      icon: PropTypes.string,
      parameters: PropTypes.object,
      handler: PropTypes.func
    })
  ).isRequired,
  onActionSelect: PropTypes.func.isRequired
};

export default ActionPanel;