# Quick Revision Chat Interface

A functional chat interface for the Quick Revision application.

## Features

- Modern UI with tabs for navigation
- Personalized greeting based on time of day
- Interactive chat input with formatting options
- Suggestion buttons for quick actions

## Tech Stack

- React
- Vite
- CSS Modules

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser and navigate to the URL shown in the terminal (typically http://localhost:5173)

## Project Structure

- `src/App.jsx` - Main application component
- `src/components/` - Reusable UI components
  - `Header.jsx` - Top navigation bar
  - `ChatInput.jsx` - Interactive chat input field
  - `SuggestionButton.jsx` - Clickable suggestion buttons

## Customization

- Change the user name by modifying the localStorage item 'userName'
- Modify the suggestion buttons in App.jsx
- Update colors in the CSS modules