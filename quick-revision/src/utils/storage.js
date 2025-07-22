// Local storage keys
const DECKS_STORAGE_KEY = 'quickrevision_decks'

/**
 * Generate a unique ID
 * @returns {string} Unique ID
 */
export const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 5)
}

/**
 * Load decks from localStorage
 * @returns {Array} Array of deck objects
 */
export const loadDecks = () => {
  try {
    const decksJson = localStorage.getItem(DECKS_STORAGE_KEY)
    return decksJson ? JSON.parse(decksJson) : []
  } catch (error) {
    console.error('Error loading decks from localStorage:', error)
    return []
  }
}

/**
 * Save decks to localStorage
 * @param {Array} decks - Array of deck objects
 */
export const saveDecks = (decks) => {
  try {
    localStorage.setItem(DECKS_STORAGE_KEY, JSON.stringify(decks))
  } catch (error) {
    console.error('Error saving decks to localStorage:', error)
  }
}

/**
 * Get a deck by ID
 * @param {string} id - Deck ID
 * @returns {Object|null} Deck object or null if not found
 */
export const getDeckById = (id) => {
  const decks = loadDecks()
  return decks.find(deck => deck.id === id) || null
}

/**
 * Create a new deck
 * @param {Object} deckData - Deck data
 * @returns {Object} Created deck
 */
export const createDeck = (deckData) => {
  const decks = loadDecks()
  
  const newDeck = {
    id: generateId(),
    title: deckData.title,
    description: deckData.description,
    cards: deckData.cards || [],
    created: new Date().toISOString(),
    lastStudied: null
  }
  
  const updatedDecks = [...decks, newDeck]
  saveDecks(updatedDecks)
  
  return newDeck
}

/**
 * Update an existing deck
 * @param {string} id - Deck ID
 * @param {Object} deckData - Updated deck data
 * @returns {Object|null} Updated deck or null if not found
 */
export const updateDeck = (id, deckData) => {
  const decks = loadDecks()
  const deckIndex = decks.findIndex(deck => deck.id === id)
  
  if (deckIndex === -1) {
    return null
  }
  
  const updatedDeck = {
    ...decks[deckIndex],
    ...deckData
  }
  
  const updatedDecks = [
    ...decks.slice(0, deckIndex),
    updatedDeck,
    ...decks.slice(deckIndex + 1)
  ]
  
  saveDecks(updatedDecks)
  return updatedDeck
}

/**
 * Delete a deck
 * @param {string} id - Deck ID
 * @returns {boolean} Success status
 */
export const deleteDeck = (id) => {
  const decks = loadDecks()
  const updatedDecks = decks.filter(deck => deck.id !== id)
  
  if (updatedDecks.length === decks.length) {
    return false
  }
  
  saveDecks(updatedDecks)
  return true
}

/**
 * Update study progress for a deck
 * @param {string} deckId - Deck ID
 * @param {Object} progressData - Progress data
 */
export const updateStudyProgress = (deckId, progressData) => {
  const decks = loadDecks()
  const deckIndex = decks.findIndex(deck => deck.id === deckId)
  
  if (deckIndex === -1) {
    return null
  }
  
  const updatedDeck = {
    ...decks[deckIndex],
    lastStudied: new Date().toISOString(),
    progress: {
      ...(decks[deckIndex].progress || {}),
      ...progressData
    }
  }
  
  const updatedDecks = [
    ...decks.slice(0, deckIndex),
    updatedDeck,
    ...decks.slice(deckIndex + 1)
  ]
  
  saveDecks(updatedDecks)
  return updatedDeck
}