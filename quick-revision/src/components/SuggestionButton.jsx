import styles from './SuggestionButton.module.css'

function SuggestionButton({ icon, text, color, onClick }) {
  const handleClick = () => {
    if (onClick) {
      onClick(text)
    }
  }

  return (
    <button 
      className={styles.button} 
      style={{ backgroundColor: color }}
      onClick={handleClick}
    >
      <span className={styles.icon}>{icon}</span>
      <span className={styles.text}>{text}</span>
    </button>
  )
}

export default SuggestionButton