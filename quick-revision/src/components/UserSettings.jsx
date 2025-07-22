import { useState } from 'react'
import styles from './UserSettings.module.css'

function UserSettings({ userName, onSave, onClose }) {
  const [name, setName] = useState(userName)

  const handleSubmit = (e) => {
    e.preventDefault()
    if (name.trim()) {
      onSave(name.trim())
      onClose()
    }
  }

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <h2 className={styles.title}>User Settings</h2>
        
        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="userName" className={styles.label}>Your Name</label>
            <input
              type="text"
              id="userName"
              className={styles.input}
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your name"
            />
          </div>
          
          <div className={styles.actions}>
            <button 
              type="button" 
              className={styles.cancelButton}
              onClick={onClose}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className={styles.saveButton}
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default UserSettings