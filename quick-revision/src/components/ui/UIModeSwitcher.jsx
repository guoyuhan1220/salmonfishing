import PropTypes from 'prop-types';
import { useUIContext } from '../../contexts/UIContext';
import styles from './UIModeSwitcher.module.css';

function UIModeSwitcher() {
  const { mode, setMode } = useUIContext();

  const handleModeChange = (newMode) => {
    setMode(newMode);
  };

  return (
    <div className={styles.container}>
      <button
        className={`${styles.modeButton} ${mode === 'focus' ? styles.active : ''}`}
        onClick={() => handleModeChange('focus')}
        title="Focus Mode"
      >
        <span className={styles.icon}>🔍</span>
        <span className={styles.label}>Focus</span>
      </button>
      <button
        className={`${styles.modeButton} ${mode === 'compact' ? styles.active : ''}`}
        onClick={() => handleModeChange('compact')}
        title="Compact Mode"
      >
        <span className={styles.icon}>📐</span>
        <span className={styles.label}>Compact</span>
      </button>
    </div>
  );
}

export default UIModeSwitcher;