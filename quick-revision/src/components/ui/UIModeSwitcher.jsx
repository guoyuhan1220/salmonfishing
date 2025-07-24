import { useState } from 'react';
import PropTypes from 'prop-types';
import styles from './UIModeSwitcher.module.css';

/**
 * UIModeSwitcher component for switching between focus and compact modes
 */
function UIModeSwitcher({ currentMode, onModeChange }) {
  const [isHovered, setIsHovered] = useState(false);
  
  const handleModeChange = () => {
    const newMode = currentMode === 'focus' ? 'compact' : 'focus';
    onModeChange(newMode);
  };
  
  return (
    <button 
      className={`${styles.modeSwitcher} ${styles[currentMode]}`}
      onClick={handleModeChange}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      aria-label={`Switch to ${currentMode === 'focus' ? 'compact' : 'focus'} mode`}
    >
      <div className={styles.iconContainer}>
        <img 
          src={currentMode === 'focus' ? './icons/Minimize.svg' : './icons/Maximize.svg'} 
          alt={currentMode === 'focus' ? 'Compact' : 'Focus'} 
          className={styles.icon}
        />
      </div>
      <span className={styles.label}>
        {isHovered 
          ? `Switch to ${currentMode === 'focus' ? 'compact' : 'focus'} mode` 
          : `${currentMode === 'focus' ? 'Focus' : 'Compact'} mode`}
      </span>
    </button>
  );
}

UIModeSwitcher.propTypes = {
  currentMode: PropTypes.oneOf(['focus', 'compact']).isRequired,
  onModeChange: PropTypes.func.isRequired
};

export default UIModeSwitcher;