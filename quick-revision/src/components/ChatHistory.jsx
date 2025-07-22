import { useRef, useEffect } from 'react'
import ChatMessage from './ChatMessage'
import styles from './ChatHistory.module.css'

function ChatHistory({ messages }) {
  const messagesEndRef = useRef(null)

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [messages])

  // If no messages, show empty state
  if (messages.length === 0) {
    return null
  }

  return (
    <div className={styles.history}>
      {messages.map((msg, index) => (
        <ChatMessage 
          key={index} 
          message={msg.text} 
          isUser={msg.isUser} 
        />
      ))}
      <div ref={messagesEndRef} />
    </div>
  )
}

export default ChatHistory