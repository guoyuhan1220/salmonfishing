import { createContext, useContext, useState, useEffect } from 'react';

// Simple ID generator function to replace uuid dependency
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

// Import types
// Note: Since we're focusing on the front-end, we'll use PropTypes for now
// and can convert to TypeScript later if needed

const ChatContext = createContext();

export const useChatContext = () => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error('useChatContext must be used within a ChatProvider');
  }
  return context;
};

export const ChatProvider = ({ children }) => {
  // State for chat sessions
  const [sessions, setSessions] = useState([]);
  const [currentSessionId, setCurrentSessionId] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [availableActions, setAvailableActions] = useState([]);

  // Initialize with a default session
  useEffect(() => {
    if (sessions.length === 0) {
      const initialSession = {
        id: generateId(),
        title: 'New Conversation',
        createdAt: new Date(),
        updatedAt: new Date(),
        messages: [
          {
            id: generateId(),
            text: "Hi there! How can I help you today?",
            isUser: false,
            timestamp: new Date(),
          }
        ],
        uploadedFiles: []
      };
      
      setSessions([initialSession]);
      setCurrentSessionId(initialSession.id);
    }
  }, [sessions]);

  // Get current session
  const currentSession = sessions.find(session => session.id === currentSessionId) || null;
  
  // Get messages from current session
  const messages = currentSession ? currentSession.messages : [];

  // Send a message
  const sendMessage = async (text) => {
    if (!text.trim() || !currentSessionId) return;

    // Create user message
    const userMessage = {
      id: generateId(),
      text,
      isUser: true,
      timestamp: new Date()
    };

    // Update session with user message
    setSessions(prevSessions => {
      return prevSessions.map(session => {
        if (session.id === currentSessionId) {
          return {
            ...session,
            messages: [...session.messages, userMessage],
            updatedAt: new Date()
          };
        }
        return session;
      });
    });

    // Set generating state
    setIsGenerating(true);

    try {
      // In a real implementation, this would call an AI service
      // For now, we'll simulate a response
      setTimeout(() => {
        const assistantMessage = {
          id: generateId(),
          text: `I received your message: "${text}"`,
          isUser: false,
          timestamp: new Date()
        };

        // Update session with assistant message
        setSessions(prevSessions => {
          return prevSessions.map(session => {
            if (session.id === currentSessionId) {
              return {
                ...session,
                messages: [...session.messages, assistantMessage],
                updatedAt: new Date()
              };
            }
            return session;
          });
        });
        
        // Simulate generating actions based on user input
        if (text.toLowerCase().includes('search') || text.toLowerCase().includes('find')) {
          const searchAction = {
            id: generateId(),
            name: 'Search Web',
            description: 'Search the web for information',
            icon: '🔍',
            handler: () => {
              console.log('Searching the web...');
              // In a real implementation, this would trigger a web search
            }
          };
          
          addActions([searchAction]);
        }
        
        if (text.toLowerCase().includes('schedule') || text.toLowerCase().includes('meeting')) {
          const scheduleAction = {
            id: generateId(),
            name: 'Schedule Meeting',
            description: 'Create a calendar event',
            icon: '📅',
            handler: () => {
              console.log('Scheduling a meeting...');
              // In a real implementation, this would open a calendar interface
            }
          };
          
          addActions([scheduleAction]);
        }
        
        if (text.toLowerCase().includes('email') || text.toLowerCase().includes('send')) {
          const emailAction = {
            id: generateId(),
            name: 'Compose Email',
            description: 'Create a new email',
            icon: '✉️',
            handler: () => {
              console.log('Composing an email...');
              // In a real implementation, this would open an email interface
            }
          };
          
          addActions([emailAction]);
        }

        setIsGenerating(false);
      }, 1000);
    } catch (error) {
      console.error('Error generating response:', error);
      setIsGenerating(false);
      
      // Add error message
      const errorMessage = {
        id: generateId(),
        text: "Sorry, I encountered an error while generating a response. Please try again.",
        isUser: false,
        timestamp: new Date(),
        isError: true
      };

      setSessions(prevSessions => {
        return prevSessions.map(session => {
          if (session.id === currentSessionId) {
            return {
              ...session,
              messages: [...session.messages, errorMessage],
              updatedAt: new Date()
            };
          }
          return session;
        });
      });
    }
  };

  // Upload a file
  const uploadFile = async (file) => {
    if (!file || !currentSessionId) return;

    // Create a new uploaded file object
    const newFile = {
      id: generateId(),
      name: file.name,
      type: file.type,
      size: file.size,
      uploadedAt: new Date(),
      processingStatus: 'pending'
    };

    // Add file to uploaded files
    setUploadedFiles(prev => [...prev, newFile]);

    // Update current session with the file
    setSessions(prevSessions => {
      return prevSessions.map(session => {
        if (session.id === currentSessionId) {
          return {
            ...session,
            uploadedFiles: [...session.uploadedFiles, newFile],
            updatedAt: new Date()
          };
        }
        return session;
      });
    });

    try {
      // Simulate file processing
      setTimeout(() => {
        // Update file status to processing
        setUploadedFiles(prev => 
          prev.map(f => 
            f.id === newFile.id 
              ? { ...f, processingStatus: 'processing' } 
              : f
          )
        );

        // Simulate processing completion
        setTimeout(() => {
          // Update file status to completed
          setUploadedFiles(prev => 
            prev.map(f => 
              f.id === newFile.id 
                ? { ...f, processingStatus: 'completed' } 
                : f
            )
          );

          // Add system message about the file
          const systemMessage = {
            id: generateId(),
            text: `File "${file.name}" has been processed. You can now ask questions about it.`,
            isUser: false,
            timestamp: new Date(),
            isSystem: true
          };

          setSessions(prevSessions => {
            return prevSessions.map(session => {
              if (session.id === currentSessionId) {
                return {
                  ...session,
                  messages: [...session.messages, systemMessage],
                  updatedAt: new Date()
                };
              }
              return session;
            });
          });
        }, 2000);
      }, 1000);
    } catch (error) {
      console.error('Error processing file:', error);
      
      // Update file status to failed
      setUploadedFiles(prev => 
        prev.map(f => 
          f.id === newFile.id 
            ? { ...f, processingStatus: 'failed', processingError: error.message } 
            : f
        )
      );

      // Add error message
      const errorMessage = {
        id: generateId(),
        text: `Sorry, I encountered an error while processing the file "${file.name}". Please try again.`,
        isUser: false,
        timestamp: new Date(),
        isError: true
      };

      setSessions(prevSessions => {
        return prevSessions.map(session => {
          if (session.id === currentSessionId) {
            return {
              ...session,
              messages: [...session.messages, errorMessage],
              updatedAt: new Date()
            };
          }
          return session;
        });
      });
    }
  };

  // Clear chat history
  const clearHistory = () => {
    if (!currentSessionId) return;

    setSessions(prevSessions => {
      return prevSessions.map(session => {
        if (session.id === currentSessionId) {
          return {
            ...session,
            messages: [
              {
                id: generateId(),
                text: "Hi there! How can I help you today?",
                isUser: false,
                timestamp: new Date()
              }
            ],
            updatedAt: new Date()
          };
        }
        return session;
      });
    });
  };

  // Create a new session
  const createNewSession = () => {
    const newSession = {
      id: generateId(),
      title: 'New Conversation',
      createdAt: new Date(),
      updatedAt: new Date(),
      messages: [
        {
          id: generateId(),
          text: "Hi there! How can I help you today?",
          isUser: false,
          timestamp: new Date()
        }
      ],
      uploadedFiles: []
    };

    setSessions(prev => [...prev, newSession]);
    setCurrentSessionId(newSession.id);
  };

  // Switch to a different session
  const switchSession = (sessionId) => {
    const sessionExists = sessions.some(session => session.id === sessionId);
    if (sessionExists) {
      setCurrentSessionId(sessionId);
    }
  };

  // Delete a session
  const deleteSession = (sessionId) => {
    setSessions(prev => prev.filter(session => session.id !== sessionId));
    
    // If we deleted the current session, switch to another one
    if (currentSessionId === sessionId) {
      const remainingSessions = sessions.filter(session => session.id !== sessionId);
      if (remainingSessions.length > 0) {
        setCurrentSessionId(remainingSessions[0].id);
      } else {
        // If no sessions left, create a new one
        createNewSession();
      }
    }
  };

  // Rename a session
  const renameSession = (sessionId, newTitle) => {
    setSessions(prevSessions => {
      return prevSessions.map(session => {
        if (session.id === sessionId) {
          return {
            ...session,
            title: newTitle,
            updatedAt: new Date()
          };
        }
        return session;
      });
    });
  };

  // Execute an action
  const executeAction = (action) => {
    if (!action || !action.id) return;
    
    // If the action has a handler, execute it
    if (typeof action.handler === 'function') {
      action.handler();
    }
    
    // Add a system message about the action
    const systemMessage = {
      id: generateId(),
      text: `Executing action: ${action.name}`,
      isUser: false,
      timestamp: new Date(),
      isSystem: true
    };

    setSessions(prevSessions => {
      return prevSessions.map(session => {
        if (session.id === currentSessionId) {
          return {
            ...session,
            messages: [...session.messages, systemMessage],
            updatedAt: new Date()
          };
        }
        return session;
      });
    });
    
    // Remove the action from available actions
    setAvailableActions(prev => prev.filter(a => a.id !== action.id));
  };
  
  // Add actions
  const addActions = (actions) => {
    if (!actions || !Array.isArray(actions)) return;
    
    setAvailableActions(prev => {
      // Filter out any existing actions with the same IDs
      const existingIds = new Set(prev.map(a => a.id));
      const newActions = actions.filter(a => !existingIds.has(a.id));
      
      return [...prev, ...newActions];
    });
  };

  const value = {
    sessions,
    currentSession,
    messages,
    isGenerating,
    uploadedFiles,
    availableActions,
    sendMessage,
    uploadFile,
    clearHistory,
    createNewSession,
    switchSession,
    deleteSession,
    renameSession,
    executeAction,
    addActions
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};

export default ChatContext;