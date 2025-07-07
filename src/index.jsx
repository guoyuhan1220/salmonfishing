import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// Removing the service worker by default as it can cause issues with users not being able to see new
// versions of the app.
// More info: https://console.harmony.a2z.com/docs/troubleshooting.html#React%20Gotchas

// registerServiceWorker();

reportWebVitals();
