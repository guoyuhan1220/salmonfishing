import { useState } from 'react'
import styles from './ChatInput.module.css'

function ChatInput({ onSendMessage }) {
  const [message, setMessage] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (message.trim()) {
      onSendMessage(message.trim())
      setMessage('')
    }
  }

  return (
    <form className={styles.inputContainer} onSubmit={handleSubmit}>
      <input
        type="text"
        className={styles.input}
        placeholder="Ask a question"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
      />
      <div className={styles.actions}>
        <button type="button" className={styles.actionButton} title="Attach file">
          <span>📎</span>
        </button>
        <button type="button" className={styles.actionButton} title="Format text">
          <span>📝</span>
        </button>
        <button type="button" className={styles.actionButton} title="Add code">
          <span>⚡</span>
        </button>
        <button type="button" className={styles.actionButton} title="More options">
          <span>⋯</span>
        </button>
      </div>
      <button 
        type="submit" 
        className={`${styles.sendButton} ${message.trim() ? styles.active : ''}`}
        disabled={!message.trim()}
      >
        <span>➤</span>
      </button>
    </form>
  )
}

export default ChatInput