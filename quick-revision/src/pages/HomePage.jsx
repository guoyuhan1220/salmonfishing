import { Link } from 'react-router-dom'
import styles from './HomePage.module.css'

function HomePage() {
  return (
    <div className={styles.home}>
      <section className={styles.hero}>
        <h1 className={styles.title}>Quick Revision</h1>
        <p className={styles.subtitle}>
          Boost your learning with flashcards and spaced repetition
        </p>
        <div className={styles.actions}>
          <Link to="/decks" className={`btn ${styles.primaryBtn}`}>
            My Decks
          </Link>
          <Link to="/create" className={`btn btn-accent ${styles.secondaryBtn}`}>
            Create New Deck
          </Link>
        </div>
      </section>
      
      <section className={styles.features}>
        <h2 className={styles.sectionTitle}>Features</h2>
        <div className={styles.featureGrid}>
          <div className={styles.feature}>
            <h3>Flashcards</h3>
            <p>Create and study flashcards to memorize information effectively</p>
          </div>
          <div className={styles.feature}>
            <h3>Spaced Repetition</h3>
            <p>Optimize your learning with scientifically proven study methods</p>
          </div>
          <div className={styles.feature}>
            <h3>Progress Tracking</h3>
            <p>Monitor your learning progress and identify areas for improvement</p>
          </div>
          <div className={styles.feature}>
            <h3>Simple Interface</h3>
            <p>Focus on learning with our clean and intuitive user interface</p>
          </div>
        </div>
      </section>
      
      <section className={styles.cta}>
        <h2>Ready to start learning?</h2>
        <Link to="/create" className="btn">
          Create Your First Deck
        </Link>
      </section>
    </div>
  )
}

export default HomePage