import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import ChatMessage from './ChatMessage';

// Mock child components
vi.mock('../citations/CitationTooltip', () => ({
  default: ({ citation }) => (
    <div data-testid="citation-tooltip">
      Citation: {citation.id}
    </div>
  )
}));

vi.mock('../citations/SourceViewer', () => ({
  default: ({ citations, onClose }) => (
    <div data-testid="source-viewer">
      Sources: {citations.length}
      <button onClick={onClose}>Close</button>
    </div>
  )
}));

vi.mock('./IntermediateStepsViewer', () => ({
  default: ({ steps }) => (
    <div data-testid="steps-viewer">
      Steps: {steps.length}
    </div>
  )
}));

// Test suite
describe('ChatMessage Component', () => {
  test('renders user message correctly', () => {
    const message = {
      id: '1',
      text: 'Hello, this is a test message',
      isUser: true,
      timestamp: new Date()
    };
    
    render(<ChatMessage message={message} />);
    
    expect(screen.getByText('You')).toBeInTheDocument();
    expect(screen.getByText('Hello, this is a test message')).toBeInTheDocument();
  });

  test('renders assistant message correctly', () => {
    const message = {
      id: '2',
      text: 'Hello, I am the assistant',
      isUser: false,
      timestamp: new Date()
    };
    
    render(<ChatMessage message={message} />);
    
    expect(screen.getByText('My assistant')).toBeInTheDocument();
    expect(screen.getByText('Hello, I am the assistant')).toBeInTheDocument();
  });

  test('renders loading indicator when generating', () => {
    const message = {
      id: '3',
      text: '',
      isUser: false,
      timestamp: new Date(),
      isGenerating: true
    };
    
    render(<ChatMessage message={message} />);
    
    expect(screen.getByText('My assistant')).toBeInTheDocument();
    expect(screen.getByText(/Generating response/i)).toBeInTheDocument();
  });

  test('renders citations correctly', () => {
    const message = {
      id: '4',
      text: 'This message has a citation[1].',
      isUser: false,
      timestamp: new Date(),
      citations: [
        {
          id: 1,
          text: 'Citation text',
          source: {
            type: 'web',
            title: 'Web Source',
            url: 'https://example.com'
          }
        }
      ]
    };
    
    render(<ChatMessage message={message} />);
    
    expect(screen.getByText(/This message has a citation/)).toBeInTheDocument();
    
    // Find the citation number
    const citation = screen.getByText('1');
    expect(citation).toBeInTheDocument();
    
    // Hover over the citation to show the tooltip
    fireEvent.mouseEnter(citation.parentElement);
    
    // Check if the tooltip is shown
    expect(screen.getByTestId('citation-tooltip')).toBeInTheDocument();
    
    // Check if the source button is shown
    expect(screen.getByText(/Sources/)).toBeInTheDocument();
    
    // Click the source button to show the source viewer
    fireEvent.click(screen.getByText(/Sources/));
    
    // Check if the source viewer is shown
    expect(screen.getByTestId('source-viewer')).toBeInTheDocument();
    
    // Close the source viewer
    fireEvent.click(screen.getByText('Close'));
  });

  test('renders intermediate steps correctly', () => {
    const message = {
      id: '5',
      text: 'This message has intermediate steps.',
      isUser: false,
      timestamp: new Date(),
      intermediateSteps: [
        {
          id: 1,
          description: 'Step 1',
          content: 'Step 1 content'
        },
        {
          id: 2,
          description: 'Step 2',
          content: 'Step 2 content'
        }
      ]
    };
    
    render(<ChatMessage message={message} />);
    
    expect(screen.getByText('This message has intermediate steps.')).toBeInTheDocument();
    expect(screen.getByTestId('steps-viewer')).toBeInTheDocument();
    expect(screen.getByText('Steps: 2')).toBeInTheDocument();
  });

  test('renders error message correctly', () => {
    const message = {
      id: '6',
      text: 'This is an error message',
      isUser: false,
      timestamp: new Date(),
      isError: true
    };
    
    render(<ChatMessage message={message} />);
    
    const messageElement = screen.getByText('This is an error message');
    expect(messageElement).toBeInTheDocument();
    
    // Check if the message has the error class
    expect(messageElement.closest('div[class*="errorMessage"]')).toBeInTheDocument();
  });

  test('renders system message correctly', () => {
    const message = {
      id: '7',
      text: 'This is a system message',
      isUser: false,
      timestamp: new Date(),
      isSystem: true
    };
    
    render(<ChatMessage message={message} />);
    
    const messageElement = screen.getByText('This is a system message');
    expect(messageElement).toBeInTheDocument();
    
    // Check if the message has the system class
    expect(messageElement.closest('div[class*="systemMessage"]')).toBeInTheDocument();
  });
});