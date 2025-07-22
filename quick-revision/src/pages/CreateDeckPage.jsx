import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createDeck } from '../utils/storage'
import styles from './CreateDeckPage.module.css'

function CreateDeckPage({ setDecks }) {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [cards, setCards] = useState([{ id: 1, question: '', answer: '' }])
  const [errors, setErrors] = useState({})
  
  const handleAddCard = () => {
    const newId = cards.length > 0 ? Math.max(...cards.map(c => c.id)) + 1 : 1
    setCards([...cards, { id: newId, question: '', answer: '' }])
  }
  
  const handleRemoveCard = (id) => {
    if (cards.length <= 1) return
    setCards(cards.filter(card => card.id !== id))
  }
  
  const handleCardChange = (id, field, value) => {
    setCards(cards.map(card => 
      card.id === id ? { ...card, [field]: value } : card
    ))
  }
  
  const validateForm = () => {
    const newErrors = {}
    
    if (!title.trim()) {
      newErrors.title = 'Title is required'
    }
    
    if (!description.trim()) {
      newErrors.description = 'Description is required'
    }
    
    let hasCardErrors = false
    const cardErrors = {}
    
    cards.forEach((card, index) => {
      if (!card.question.trim()) {
        cardErrors[`question_${card.id}`] = 'Question is required'
        hasCardErrors = true
      }
      
      if (!card.answer.trim()) {
        cardErrors[`answer_${card.id}`] = 'Answer is required'
        hasCardErrors = true
      }
    })
    
    if (hasCardErrors) {
      newErrors.cards = cardErrors
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }
  
  const handleSubmit = (e) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }
    
    const newDeck = createDeck({
      title,
      description,
      cards: cards.map(({ id, ...rest }) => rest)
    })
    
    setDecks(prevDecks => [...prevDecks, newDeck])
    navigate('/decks')
  }
  
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Create New Deck</h1>
      
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="title" className={styles.label}>Deck Title</label>
          <input
            type="text"
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className={styles.input}
            placeholder="Enter deck title"
          />
          {errors.title && <p className={styles.error}>{errors.title}</p>}
        </div>
        
        <div className={styles.formGroup}>
          <label htmlFor="description" className={styles.label}>Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className={styles.textarea}
            placeholder="Enter deck description"
            rows="3"
          />
          {errors.description && <p className={styles.error}>{errors.description}</p>}
        </div>
        
        <div className={styles.cardsSection}>
          <div className={styles.cardsSectionHeader}>
            <h2 className={styles.cardsTitle}>Flashcards</h2>
            <button 
              type="button" 
              onClick={handleAddCard}
              className={styles.addCardBtn}
            >
              Add Card
            </button>
          </div>
          
          {cards.map((card, index) => (
            <div key={card.id} className={styles.card}>
              <div className={styles.cardHeader}>
                <h3>Card {index + 1}</h3>
                {cards.length > 1 && (
                  <button
                    type="button"
                    onClick={() => handleRemoveCard(card.id)}
                    className={styles.removeCardBtn}
                  >
                    Remove
                  </button>
                )}
              </div>
              
              <div className={styles.formGroup}>
                <label htmlFor={`question_${card.id}`} className={styles.label}>Question</label>
                <textarea
                  id={`question_${card.id}`}
                  value={card.question}
                  onChange={(e) => handleCardChange(card.id, 'question', e.target.value)}
                  className={styles.textarea}
                  placeholder="Enter question"
                  rows="2"
                />
                {errors.cards && errors.cards[`question_${card.id}`] && (
                  <p className={styles.error}>{errors.cards[`question_${card.id}`]}</p>
                )}
              </div>
              
              <div className={styles.formGroup}>
                <label htmlFor={`answer_${card.id}`} className={styles.label}>Answer</label>
                <textarea
                  id={`answer_${card.id}`}
                  value={card.answer}
                  onChange={(e) => handleCardChange(card.id, 'answer', e.target.value)}
                  className={styles.textarea}
                  placeholder="Enter answer"
                  rows="2"
                />
                {errors.cards && errors.cards[`answer_${card.id}`] && (
                  <p className={styles.error}>{errors.cards[`answer_${card.id}`]}</p>
                )}
              </div>
            </div>
          ))}
        </div>
        
        <div className={styles.actions}>
          <button type="submit" className="btn">
            Create Deck
          </button>
        </div>
      </form>
    </div>
  )
}

export default CreateDeckPage