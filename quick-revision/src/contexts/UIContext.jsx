import { createContext, useContext, useState, useEffect } from 'react';

const UIContext = createContext();

export const useUIContext = () => {
  const context = useContext(UIContext);
  if (!context) {
    throw new Error('useUIContext must be used within a UIProvider');
  }
  return context;
};

export const UIProvider = ({ children }) => {
  // Load preferences from localStorage if available
  const loadPreferences = () => {
    try {
      const savedPreferences = localStorage.getItem('uiPreferences');
      if (savedPreferences) {
        return JSON.parse(savedPreferences);
      }
    } catch (error) {
      console.error('Error loading preferences:', error);
    }
    
    // Default preferences
    return {
      mode: 'compact',
      showIntermediateSteps: true,
      theme: 'light'
    };
  };

  // State for UI preferences
  const [preferences, setPreferences] = useState(loadPreferences);

  // Save preferences to localStorage when they change
  useEffect(() => {
    try {
      localStorage.setItem('uiPreferences', JSON.stringify(preferences));
    } catch (error) {
      console.error('Error saving preferences:', error);
    }
  }, [preferences]);

  // Set UI mode (focus or compact)
  const setMode = (mode) => {
    if (mode !== 'focus' && mode !== 'compact') return;
    setPreferences(prev => ({ ...prev, mode }));
  };

  // Toggle intermediate steps visibility
  const toggleIntermediateSteps = () => {
    setPreferences(prev => ({ 
      ...prev, 
      showIntermediateSteps: !prev.showIntermediateSteps 
    }));
    
    // Log the change for debugging
    console.log(`Intermediate steps visibility toggled to: ${!preferences.showIntermediateSteps}`);
  };

  // Set theme
  const setTheme = (theme) => {
    if (theme !== 'light' && theme !== 'dark' && theme !== 'system') return;
    setPreferences(prev => ({ ...prev, theme }));
    
    // Apply theme to document
    if (theme === 'system') {
      const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', systemTheme);
    } else {
      document.documentElement.setAttribute('data-theme', theme);
    }
  };

  // Listen for system theme changes if using system theme
  useEffect(() => {
    if (preferences.theme === 'system') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      
      const handleChange = (e) => {
        document.documentElement.setAttribute('data-theme', e.matches ? 'dark' : 'light');
      };
      
      // Set initial theme
      handleChange(mediaQuery);
      
      // Listen for changes
      mediaQuery.addEventListener('change', handleChange);
      
      // Cleanup
      return () => mediaQuery.removeEventListener('change', handleChange);
    } else {
      // Apply theme directly
      document.documentElement.setAttribute('data-theme', preferences.theme);
    }
  }, [preferences.theme]);

  const value = {
    mode: preferences.mode,
    showIntermediateSteps: preferences.showIntermediateSteps,
    theme: preferences.theme,
    setMode,
    toggleIntermediateSteps,
    setTheme
  };

  return <UIContext.Provider value={value}>{children}</UIContext.Provider>;
};

export default UIContext;