import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { ChatProvider, useChatContext } from './ChatContext';

// Test component that uses the ChatContext
function TestComponent() {
  const { 
    messages, 
    sendMessage, 
    createNewSession, 
    isGenerating 
  } = useChatContext();
  
  return (
    <div>
      <div data-testid="message-count">{messages.length}</div>
      <div data-testid="generating">{isGenerating ? 'true' : 'false'}</div>
      <button onClick={() => sendMessage('Test message')}>Send Message</button>
      <button onClick={createNewSession}>New Session</button>
      <div>
        {messages.map(msg => (
          <div key={msg.id} data-testid="message">
            {msg.isUser ? 'User: ' : 'AI: '}{msg.text}
          </div>
        ))}
      </div>
    </div>
  );
}

// Mock services
const mockAIService = {
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
        text: 'response',
        fullText: 'Mock response',
        isComplete: true
      });
    }, 200);
    
    return Promise.resolve({
      text: 'Mock response',
      citations: [],
      intermediateSteps: []
    });
  })
};

// Test suite
describe('ChatContext', () => {
  test('initializes with a default session and welcome message', () => {
    render(
      <ChatProvider aiService={mockAIService}>
        <TestComponent />
      </ChatProvider>
    );
    
    // Should have 1 message (the welcome message)
    expect(screen.getByTestId('message-count').textContent).toBe('1');
    
    // Should have the welcome message
    expect(screen.getByTestId('message').textContent).toContain('Hi there!');
  });

  test('can send a message and receive a response', async () => {
    render(
      <ChatProvider aiService={mockAIService}>
        <TestComponent />
      </ChatProvider>
    );
    
    // Send a message
    fireEvent.click(screen.getByText('Send Message'));
    
    // Should be generating
    await waitFor(() => {
      expect(screen.getByTestId('generating').textContent).toBe('true');
    });
    
    // Should have 2 messages (welcome + user message)
    expect(screen.getByTestId('message-count').textContent).toBe('2');
    
    // Wait for the response
    await waitFor(() => {
      expect(screen.getAllByTestId('message').length).toBe(3);
    }, { timeout: 1000 });
    
    // Should have the response
    expect(screen.getAllByTestId('message')[2].textContent).toContain('Mock response');
    
    // Should not be generating anymore
    expect(screen.getByTestId('generating').textContent).toBe('false');
  });

  test('can create a new session', async () => {
    render(
      <ChatProvider aiService={mockAIService}>
        <TestComponent />
      </ChatProvider>
    );
    
    // Send a message to have more than just the welcome message
    fireEvent.click(screen.getByText('Send Message'));
    
    // Wait for the response
    await waitFor(() => {
      expect(screen.getAllByTestId('message').length).toBe(3);
    }, { timeout: 1000 });
    
    // Create a new session
    fireEvent.click(screen.getByText('New Session'));
    
    // Should have 1 message (the welcome message)
    await waitFor(() => {
      expect(screen.getByTestId('message-count').textContent).toBe('1');
    });
    
    // Should have the welcome message
    expect(screen.getByTestId('message').textContent).toContain('Hi there!');
  });
});