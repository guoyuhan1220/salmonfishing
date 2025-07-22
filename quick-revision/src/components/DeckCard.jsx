import { Link } from 'react-router-dom'
import styles from './DeckCard.module.css'

function DeckCard({ deck }) {
  const { id, title, description, cards, lastStudied } = deck
  
  // Format the last studied date
  const formattedDate = lastStudied 
    ? new Date(lastStudied).toLocaleDateString() 
    : 'Never'
  
  return (
    <div className={styles.card}>
      <h3 className={styles.title}>{title}</h3>
      <p className={styles.description}>{description}</p>
      <div className={styles.meta}>
        <span>{cards.length} cards</span>
        <span>Last studied: {formattedDate}</span>
      </div>
      <div className={styles.actions}>
        <Link to={`/decks/${id}`} className={styles.viewBtn}>
          View Deck
        </Link>
        <Link to={`/quiz/${id}`} className={styles.studyBtn}>
          Study Now
        </Link>
      </div>
    </div>
  )
}

export default DeckCard