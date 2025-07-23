// Simple service to handle LLM API communication
// Replace the API_URL and API_KEY with your actual LLM provider details

const API_URL = 'https://api.openai.com/v1/chat/completions';
const API_KEY = ''; // Add your API key here or use environment variables

export async function sendMessageToLLM(messages) {
  try {
    // Format messages for the API
    const formattedMessages = messages.map(msg => ({
      role: msg.isUser ? 'user' : 'assistant',
      content: msg.text
    }));

    // Add a system message if needed
    formattedMessages.unshift({
      role: 'system',
      content: 'You are a helpful assistant for the Quick Revision app. You help users study and learn new topics.'
    });

    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        model: 'gpt-3.5-turbo',
        messages: formattedMessages,
        temperature: 0.7,
        max_tokens: 500
      })
    });

    if (!response.ok) {
      throw new Error(`API request failed with status ${response.status}`);
    }

    const data = await response.json();
    return data.choices[0].message.content;
  } catch (error) {
    console.error('Error sending message to LLM:', error);
    return 'Sorry, I encountered an error while processing your request.';
  }
}

// Mock function for development without API key
export async function mockSendMessageToLLM(messages) {
  return new Promise((resolve) => {
    // Simulate API delay
    setTimeout(() => {
      const lastMessage = messages[messages.length - 1];
      
      if (lastMessage.isUser) {
        const userMessage = lastMessage.text.toLowerCase();
        
        // Simple response logic
        if (userMessage.includes('hello') || userMessage.includes('hi')) {
          resolve('Hello! How can I help you with your studies today?');
        } else if (userMessage.includes('help')) {
          resolve('I can help you study, create flashcards, quiz you on topics, or explain concepts. What would you like to do?');
        } else if (userMessage.includes('thank')) {
          resolve('You\'re welcome! Let me know if you need anything else.');
        } else {
          resolve(`I understand you're asking about "${lastMessage.text}". How can I help you learn more about this topic?`);
        }
      } else {
        resolve('How else can I assist you with your studies?');
      }
    }, 1000);
  });
}