import styles from './ChatMessage.module.css'

function ChatMessage({ message, isUser }) {
  return (
    <div className={`${styles.message} ${isUser ? styles.userMessage : styles.assistantMessage}`}>
      <div className={styles.avatar}>
        {isUser ? '👤' : '⚡'}
      </div>
      <div className={styles.content}>
        <div className={styles.sender}>
          {isUser ? 'You' : 'Quick Revision Assistant'}
        </div>
        <div className={styles.text}>
          {message}
        </div>
      </div>
    </div>
  )
}

export default ChatMessage