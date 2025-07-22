import { Link } from 'react-router-dom'
import DeckCard from '../components/DeckCard'
import styles from './DeckListPage.module.css'

function DeckListPage({ decks }) {
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>My Decks</h1>
        <Link to="/create" className="btn">
          Create New Deck
        </Link>
      </div>
      
      {decks.length === 0 ? (
        <div className={styles.emptyState}>
          <p>You don't have any decks yet.</p>
          <Link to="/create" className="btn">
            Create Your First Deck
          </Link>
        </div>
      ) : (
        <div className={styles.deckGrid}>
          {decks.map(deck => (
            <DeckCard key={deck.id} deck={deck} />
          ))}
        </div>
      )}
    </div>
  )
}

export default DeckListPage