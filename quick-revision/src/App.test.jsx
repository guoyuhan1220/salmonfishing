import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'

// Simple test to check if the App renders without crashing
const div = document.createElement('div')
const root = createRoot(div)
root.render(<App />)
console.log('App rendered successfully!')