import { useState } from 'react'
import styles from './FlashCard.module.css'

function FlashCard({ card, onAnswer }) {
  const [flipped, setFlipped] = useState(false)
  
  const handleFlip = () => {
    setFlipped(!flipped)
  }
  
  const handleAnswer = (correct) => {
    onAnswer(card.id, correct)
    setFlipped(false)
  }
  
  return (
    <div className={styles.cardContainer}>
      <div 
        className={`${styles.card} ${flipped ? styles.flipped : ''}`} 
        onClick={handleFlip}
      >
        <div className={styles.front}>
          <p className={styles.question}>{card.question}</p>
          <p className={styles.hint}>Click to flip</p>
        </div>
        <div className={styles.back}>
          <p className={styles.answer}>{card.answer}</p>
          <p className={styles.hint}>Click to flip back</p>
        </div>
      </div>
      
      {flipped && (
        <div className={styles.actions}>
          <button 
            className={`${styles.btn} ${styles.incorrect}`}
            onClick={() => handleAnswer(false)}
          >
            Incorrect
          </button>
          <button 
            className={`${styles.btn} ${styles.correct}`}
            onClick={() => handleAnswer(true)}
          >
            Correct
          </button>
        </div>
      )}
    </div>
  )
}

export default FlashCard