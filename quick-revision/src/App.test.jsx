import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import App from './App';

// Mock services
vi.mock('./services/AIService', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      generateResponse: vi.fn().mockResolvedValue({
        text: 'Mock AI response',
        citations: [],
        intermediateSteps: []
      }),
      streamResponse: vi.fn().mockImplementation((messages, onChunk) => {
        // Simulate streaming response
        setTimeout(() => {
          onChunk({
            text: 'Mock ',
            fullText: 'Mock ',
            isComplete: false
          });
        }, 100);
        
        setTimeout(() => {
          onChunk({
            text: 'AI ',
            fullText: 'Mock AI ',
            isComplete: false
          });
        }, 200);
        
        setTimeout(() => {
          onChunk({
            text: 'response',
            fullText: 'Mock AI response',
            intermediateSteps: [
              { id: 1, description: 'Step 1', content: 'Thinking...' }
            ],
            citations: [
              { 
                id: 1, 
                text: 'Citation', 
                source: { 
                  type: 'ai', 
                  title: 'AI Model' 
                } 
              }
            ],
            isComplete: true
          });
        }, 300);
        
        return Promise.resolve({
          text: 'Mock AI response',
          citations: [
            { 
              id: 1, 
              text: 'Citation', 
              source: { 
                type: 'ai', 
                title: 'AI Model' 
              } 
            }
          ],
          intermediateSteps: [
            { id: 1, description: 'Step 1', content: 'Thinking...' }
          ]
        });
      })
    }))
  };
});

vi.mock('./services/ResourceService', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      fetchDashboard: vi.fn().mockResolvedValue({
        title: 'Mock Dashboard',
        content: 'Mock dashboard content'
      }),
      fetchReport: vi.fn().mockResolvedValue({
        title: 'Mock Report',
        content: 'Mock report content'
      }),
      fetchSpace: vi.fn().mockResolvedValue({
        title: 'Mock Space',
        content: 'Mock space content'
      })
    }))
  };
});

vi.mock('./services/WebContentService', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      fetchContent: vi.fn().mockResolvedValue({
        title: 'Mock Web Page',
        content: 'Mock web content'
      }),
      processUrl: vi.fn().mockResolvedValue({
        title: 'Mock Web Page',
        content: 'Mock web content',
        summary: 'Mock summary'
      })
    }))
  };
});

vi.mock('./services/FileProcessingService', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      uploadFile: vi.fn().mockImplementation((file, onProgress) => {
        // Simulate upload progress
        onProgress && onProgress(50);
        setTimeout(() => onProgress && onProgress(100), 100);
        
        return Promise.resolve({
          success: true,
          fileId: 'mock-file-id',
          name: file.name,
          type: file.type,
          size: file.size
        });
      }),
      processFile: vi.fn().mockResolvedValue({
        success: true,
        fileId: 'mock-file-id',
        status: 'completed',
        metadata: {
          pageCount: 5,
          wordCount: 1000
        }
      }),
      extractText: vi.fn().mockResolvedValue({
        success: true,
        text: 'Mock extracted text'
      })
    }))
  };
});

vi.mock('./services/ActionService', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      detectActions: vi.fn().mockResolvedValue([
        {
          id: 'mock-action-1',
          name: 'Mock Action',
          description: 'Mock action description',
          handler: vi.fn()
        }
      ]),
      executeAction: vi.fn().mockResolvedValue({
        success: true,
        result: 'Mock action result'
      })
    }))
  };
});

// Test suite
describe('App Component', () => {
  test('renders landing page initially', () => {
    render(<App />);
    expect(screen.getByText(/Let's chat/i)).toBeInTheDocument();
  });

  test('can start a new conversation', async () => {
    render(<App />);
    
    // Click on "New conversation" button
    fireEvent.click(screen.getByText('New conversation'));
    
    // Should show the initial assistant message
    expect(screen.getByText('Hi there! How can I help you today?')).toBeInTheDocument();
  });

  test('can send a message and receive a response', async () => {
    render(<App />);
    
    // Click on a quick starter to start a conversation
    fireEvent.click(screen.getByText("What's the assistant capable of?"));
    
    // Wait for the message to be sent
    await waitFor(() => {
      expect(screen.getByText("What's the assistant capable of?")).toBeInTheDocument();
    });
    
    // Wait for the response
    await waitFor(() => {
      expect(screen.getByText('Mock AI response')).toBeInTheDocument();
    }, { timeout: 1000 });
  });

  test('can switch between focus and compact modes', () => {
    render(<App />);
    
    // Get the app container
    const appContainer = document.querySelector('.app-container');
    
    // Initially should be in compact mode
    expect(appContainer).toHaveClass('compact-mode');
    
    // Find and click the mode toggle button
    const modeToggleButton = screen.getByText(/mode/i);
    fireEvent.click(modeToggleButton);
    
    // Should now be in focus mode
    expect(appContainer).toHaveClass('focus-mode');
    
    // Click again to switch back
    fireEvent.click(modeToggleButton);
    
    // Should be back in compact mode
    expect(appContainer).toHaveClass('compact-mode');
  });
});