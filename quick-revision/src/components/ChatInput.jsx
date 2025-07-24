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
    </form>
  )
}

export default ChatInput