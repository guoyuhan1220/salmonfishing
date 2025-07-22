import styles from './Header.module.css'

function Header({ onSettingsClick }) {
  return (
    <header className={styles.header}>
      <div className={styles.logo}>
        <span className={styles.icon}>⚡</span>
        <span className={styles.text}>Quick Revision</span>
      </div>
      <div className={styles.actions}>
        <button className={styles.iconButton} title="Help">
          <span>❓</span>
        </button>
        <button 
          className={styles.iconButton} 
          title="Settings"
          onClick={onSettingsClick}
        >
          <span>⚙️</span>
        </button>
        <button className={styles.iconButton} title="User Profile">
          <span>👤</span>
        </button>
      </div>
    </header>
  )
}

export default Header