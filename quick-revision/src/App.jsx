import { useState, useEffect } from 'react';
import './App.css';
import { ChatProvider, useChatContext } from './contexts/ChatContext';
import { UIProvider, useUIContext } from './contexts/UIContext';
import ChatHistory from './components/chat/ChatHistory';
import ChatInput from './components/chat/ChatInput';
import ActionPanel from './components/actions/ActionPanel';
import FileUploader from './components/fileUpload/FileUploader';
import UIModeSwitcher from './components/ui/UIModeSwitcher';
import LoadingIndicator from './components/ui/LoadingIndicator';

// Import services
import AIService from './services/AIService';
import ResourceService from './services/ResourceService';
import WebContentService from './services/WebContentService';
import ActionService from './services/ActionService';
import FileProcessingService from './services/FileProcessingService';
import ErrorHandler from './services/ErrorHandler';

// Create service instances
const aiService = new AIService();
const resourceService = new ResourceService();
const webContentService = new WebContentService({}, aiService);
const actionService = new ActionService({}, aiService);
const fileProcessingService = new FileProcessingService();

// Main App component that provides contexts and services
function App() {
  return (
    <UIProvider>
      <ChatProvider 
        aiService={aiService}
        resourceService={resourceService}
        webContentService={webContentService}
        actionService={actionService}
        fileProcessingService={fileProcessingService}
      >
        <AppContent />
      </ChatProvider>
    </UIProvider>
  );
}

// App content that consumes contexts
function AppContent() {
  const { 
    mode, 
    showIntermediateSteps, 
    setMode, 
    toggleIntermediateSteps 
  } = useUIContext();
  
  const {
    messages,
    isGenerating,
    createNewSession,
    sendMessage,
    uploadFile,
    availableActions,
    executeAction,
    sessions,
    switchSession,
    currentSession
  } = useChatContext();
  
  const [showHistory, setShowHistory] = useState(false);
  const [showLandingPage, setShowLandingPage] = useState(true);
  const [showFileUploader, setShowFileUploader] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());

  // Effect to hide landing page when messages exist, but don't show it when clicking New Conversation
  useEffect(() => {
    if (messages && messages.length > 1) { // More than just the welcome message
      setShowLandingPage(false);
    }
  }, [messages]);

  // Update clock every minute
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 60000);

    return () => clearInterval(timer);
  }, []);

  const handleNewConversation = () => {
    createNewSession();
    // Don't show landing page when creating a new conversation
    setShowLandingPage(false);
    setShowHistory(false);
  };

  const handleQuickStart = (message) => {
    setShowLandingPage(false);
    setTimeout(() => {
      sendMessage(message);
    }, 100);
  };

  const toggleHistory = () => {
    setShowHistory(prev => !prev);
  };

  const handleFileUpload = (file) => {
    if (file) {
      uploadFile(file);
    }
    setShowFileUploader(false);
  };

  const handleFileButtonClick = () => {
    setShowFileUploader(true);
  };

  return (
    <div className={`app-container ${mode === 'focus' ? 'focus-mode' : 'compact-mode'}`}>
      <header className="app-header">
        <div className="header-left">
          <UIModeSwitcher 
            currentMode={mode} 
            onModeChange={setMode} 
          />
        </div>
        <div className="header-right">
          <button className="icon-button" onClick={toggleIntermediateSteps} title="Toggle Steps">
            <span role="img" aria-label="Steps">🔍</span>
            {showIntermediateSteps ? 'Hide Steps' : 'Show Steps'}
          </button>
          <button className="icon-button" onClick={toggleHistory} title="Chat History">
            <img src="./icons/mode icon.svg" alt="History" className="button-icon" />
          </button>
          <div className="time-display">
            {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </div>
        </div>
      </header>

      <main className="app-content">
        {showLandingPage ? (
          <div className="landing-page">
            <div className="landing-content">
              <h1 className="greeting">
                Good {getTimeOfDay()}! Let's chat.
              </h1>

              <div className="chat-input-wrapper">
                <div className="input-header">
                  <div className="assistant-selector">
                    <span>My assistant</span>
                    <span className="dropdown-arrow">▼</span>
                  </div>
                  <button
                    className="new-conversation-button"
                    onClick={handleNewConversation}
                    title="Start a new conversation"
                  >
                    <img src="./icons/action.svg" alt="New" className="icon" />
                    <span>New conversation</span>
                  </button>
                </div>

                <div className="input-wrapper">
                  <ChatInput />

                  <div className="input-actions">
                    <button 
                      className="action-button" 
                      title="Upload File"
                      onClick={handleFileButtonClick}
                    >
                      <img src="./icons/paperclip.svg" alt="Upload File" className="action-icon" />
                    </button>
                  </div>
                </div>

                <div className="quick-starters-container">
                  <h3 className="quick-starters-title">QUICK STARTERS</h3>
                  <div className="quick-starters-buttons">
                    <button
                      className="starter-button"
                      onClick={() => handleQuickStart("What's the assistant capable of?")}
                    >
                      <img src="./icons/ai mode.svg" alt="AI Mode" className="starter-icon" />
                      <span>What's the assistant capable of?</span>
                    </button>
                    <button
                      className="starter-button"
                      onClick={() => handleQuickStart("I'd like to upload a file to generate useful insights. How can I do that?")}
                    >
                      <img src="./icons/paperclip.svg" alt="Upload File" className="starter-icon" />
                      <span>Upload a file to generate useful insights</span>
                    </button>
                    <button
                      className="starter-button"
                      onClick={() => handleQuickStart("I want to start a new research project. Can you help me with that?")}
                    >
                      <img src="./icons/flow.svg" alt="Research Project" className="starter-icon" />
                      <span>Start a new research project</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="chat-container">
            <div className="chat-area">
              {showHistory ? (
                <div className="history-panel">
                  <h3>
                    Conversation History
                    <button className="close-button" onClick={() => setShowHistory(false)}>
                      <img src="./icons/Minimize.svg" alt="Close" className="button-icon" />
                    </button>
                  </h3>
                  <div className="history-list">
                    {sessions.map(session => (
                      <div
                        key={session.id}
                        className={`history-item ${session.id === currentSession?.id ? 'active' : ''}`}
                        onClick={() => {
                          switchSession(session.id);
                          setShowHistory(false);
                        }}
                      >
                        <span className="history-date">
                          {new Date(session.createdAt).toLocaleDateString()}
                        </span>
                        <span className="history-title">
                          {session.title}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <>
                  <ChatHistory 
                    messages={messages || []} 
                    isGenerating={isGenerating} 
                    showIntermediateSteps={showIntermediateSteps}
                  />
                  {availableActions && availableActions.length > 0 && (
                    <ActionPanel
                      actions={availableActions}
                      onActionSelect={executeAction}
                    />
                  )}
                </>
              )}
            </div>

            <div className="input-area">
              <div className="input-header">
                <div className="assistant-selector">
                  <span>My assistant</span>
                  <span className="dropdown-arrow">▼</span>
                </div>
                <button
                  className="new-conversation-button"
                  onClick={handleNewConversation}
                  title="Start a new conversation"
                >
                  <img src="./icons/action.svg" alt="New" className="icon" />
                  <span>New conversation</span>
                </button>
              </div>

              <ChatInput />
            </div>
          </div>
        )}
      </main>

      {/* File uploader modal */}
      {showFileUploader && (
        <FileUploader 
          onFileSelect={handleFileUpload}
          onCancel={() => setShowFileUploader(false)}
        />
      )}
    </div>
  );
}

// Helper function to get time of day greeting
function getTimeOfDay() {
  const hour = new Date().getHours();
  if (hour < 12) return 'morning';
  if (hour < 18) return 'afternoon';
  return 'evening';
}

export default App;