import { useState } from 'react'
import './App.css'

function App() {
  const [userName, setUserName] = useState('Erin')
  const [inputValue, setInputValue] = useState('')
  const [suggestions] = useState([
    { icon: '🖊️', text: 'I want to create a...' },
    { icon: '📍', text: 'I want to ask a question about...' },
    { icon: '📍', text: 'I want to brainstorm an idea about...' }
  ])

  const handleInputChange = (e) => {
    setInputValue(e.target.value)
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (inputValue.trim()) {
      console.log('Message sent:', inputValue)
      setInputValue('')
    }
  }

  const handleSuggestionClick = (text) => {
    setInputValue(text)
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="logo">
          <span className="logo-icon">⚡</span>
          <span className="logo-text">Amazon Quick</span>
        </div>
        <div className="header-tools">
          <button className="tool-button">Tools</button>
          <button className="icon-button">ℹ️</button>
          <button className="icon-button">👤</button>
        </div>
      </header>

      <main className="app-content">
        <div className="tabs">
          <button className="tab-button">Overview</button>
          <button className="tab-button active">Chat</button>
          <button className="tab-button">Create</button>
        </div>

        <div className="chat-container">
          <h1 className="greeting">
            Good morning, <span className="user-name">{userName}</span>! Let's chat.
          </h1>

          <div className="chat-box">
            <div className="assistant-selector">
              <span>My assistant</span>
              <span className="dropdown-arrow">▼</span>
            </div>

            <form className="chat-input-container" onSubmit={handleSubmit}>
              <input
                type="text"
                className="chat-input"
                placeholder="Ask a question"
                value={inputValue}
                onChange={handleInputChange}
              />
              <div className="input-actions">
                <button type="button" className="action-button">📎</button>
                <button type="button" className="action-button">📝</button>
                <button type="button" className="action-button">⚡</button>
                <button type="button" className="action-button">⋯</button>
              </div>
              <button
                type="submit"
                className={`send-button ${inputValue.trim() ? 'active' : ''}`}
                disabled={!inputValue.trim()}
              >
                ➤
              </button>
            </form>
          </div>

          <div className="suggestions-container">
            <h3 className="suggestions-title">WHAT WOULD YOU LIKE TO DO TODAY?</h3>
            <div className="suggestions">
              {suggestions.map((suggestion, index) => (
                <button
                  key={index}
                  className="suggestion-button"
                  onClick={() => handleSuggestionClick(suggestion.text)}
                >
                  <span className="suggestion-icon">{suggestion.icon}</span>
                  <span className="suggestion-text">{suggestion.text}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default App