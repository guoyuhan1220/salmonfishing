import React, { useState } from 'react';

const SimpleApp = () => {
  const [inputValue, setInputValue] = useState('');
  
  const handleInputChange = (e) => {
    setInputValue(e.target.value);
  };
  
  const handleSubmit = (e) => {
    e.preventDefault();
    if (inputValue.trim()) {
      console.log('Message sent:', inputValue);
      setInputValue('');
    }
  };
  
  const handleSuggestionClick = (text) => {
    setInputValue(text);
  };
  
  const suggestions = [
    { icon: '📝', text: 'I want to create a...' },
    { icon: '📍', text: 'I want to ask a question about...' },
    { icon: '📍', text: 'I want to brainstorm an idea about...' }
  ];

  return (
    <div style={{
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif',
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
      margin: 0,
      padding: 0,
      backgroundColor: '#FCFCFC'
    }}>
      {/* Header */}
      <header style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 20px',
        backgroundColor: '#232f3e', // Darker Amazon blue
        color: 'white',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '20px' }}>⚡</span>
          <span style={{ fontWeight: 600, fontSize: '16px' }}>Amazon Quick</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button style={{ 
            background: 'none', 
            border: 'none', 
            color: 'white', 
            cursor: 'pointer', 
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '4px'
          }}>
            <span>Tools</span>
          </button>
          <button style={{ background: 'none', border: 'none', color: 'white', fontSize: '18px', cursor: 'pointer', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '50%' }}>
            ℹ️
          </button>
          <button style={{ background: 'none', border: 'none', color: 'white', fontSize: '18px', cursor: 'pointer', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '50%' }}>
            👤
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '0 20px',
        maxWidth: '1200px',
        margin: '0 auto',
        width: '100%'
      }}>

        {/* Chat Container */}
        <div style={{ 
          display: 'flex', 
          flexDirection: 'column', 
          alignItems: 'center', 
          width: '100%', 
          maxWidth: '800px',
          flex: 1,
          marginTop: '40px'
        }}>
          <h1 style={{ 
            fontSize: '24px', 
            fontWeight: 400, 
            marginBottom: '40px', 
            color: '#333', 
            textAlign: 'center'
          }}>
            Good morning, <span style={{ color: '#9c27b0', fontWeight: 500 }}>Erin</span>! Let's chat.
          </h1>

          {/* Chat Box */}
          <div style={{ width: '100%', marginBottom: '40px' }}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'space-between', 
              backgroundColor: '#e8e5ff', 
              padding: '8px 16px', 
              borderRadius: '20px 20px 0 0', 
              fontSize: '14px', 
              color: '#666'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span>My assistant</span>
                <span style={{ fontSize: '10px', marginLeft: '4px' }}>▼</span>
              </div>
              <button style={{ 
                background: 'none', 
                border: 'none', 
                fontSize: '16px', 
                color: '#666', 
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '24px',
                height: '24px'
              }}>
                +
              </button>
            </div>

            <form 
              onSubmit={handleSubmit}
              style={{ 
                display: 'flex', 
                alignItems: 'center', 
                border: '1px solid #ddd', 
                borderRadius: '0 0 8px 8px', 
                backgroundColor: 'white', 
                padding: '10px 16px'
              }}
            >
              <input
                type="text"
                placeholder="Ask a question"
                value={inputValue}
                onChange={handleInputChange}
                style={{ 
                  flex: 1, 
                  border: 'none', 
                  outline: 'none', 
                  fontSize: '16px', 
                  padding: '8px 0',
                  color: '#333'
                }}
              />
              <div style={{ display: 'flex', gap: '8px', marginRight: '8px' }}>
                <button type="button" style={{ background: 'none', border: 'none', color: '#666', fontSize: '16px', cursor: 'pointer', width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px' }}>
                  <span style={{ fontSize: '18px' }}>📄</span>
                </button>
                <button type="button" style={{ background: 'none', border: 'none', color: '#666', fontSize: '16px', cursor: 'pointer', width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px' }}>
                  <span style={{ fontSize: '18px' }}>📎</span>
                </button>
                <button type="button" style={{ background: 'none', border: 'none', color: '#666', fontSize: '16px', cursor: 'pointer', width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px' }}>
                  <span style={{ fontSize: '18px' }}>⚡</span>
                </button>
                <button type="button" style={{ background: 'none', border: 'none', color: '#666', fontSize: '16px', cursor: 'pointer', width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px' }}>
                  <span style={{ fontSize: '18px' }}>⋯</span>
                </button>
              </div>
              <button 
                type="submit" 
                disabled={!inputValue.trim()}
                style={{ 
                  background: inputValue.trim() ? '#9c27b0' : '#f0f0f0', 
                  border: 'none', 
                  color: inputValue.trim() ? 'white' : '#ccc', 
                  fontSize: '14px', 
                  cursor: inputValue.trim() ? 'pointer' : 'not-allowed', 
                  width: '28px', 
                  height: '28px', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  borderRadius: '50%',
                  marginLeft: '4px'
                }}
              >
                ➤
              </button>
            </form>
          </div>

          {/* Suggestions */}
          <div style={{ width: '100%' }}>
            <h3 style={{ 
              fontSize: '11px', 
              color: '#666', 
              textAlign: 'center', 
              marginBottom: '16px', 
              letterSpacing: '1px',
              textTransform: 'uppercase',
              fontWeight: '500'
            }}>
              What would you like to do today?
            </h3>
            <div style={{ 
              display: 'flex', 
              gap: '10px', 
              flexWrap: 'wrap', 
              justifyContent: 'center'
            }}>
              <button 
                onClick={() => handleSuggestionClick(suggestions[0].text)}
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px', 
                  padding: '8px 16px', 
                  border: 'none', 
                  borderRadius: '20px', 
                  backgroundColor: '#e8e5ff', 
                  color: '#333', 
                  fontSize: '14px', 
                  cursor: 'pointer', 
                  whiteSpace: 'nowrap',
                  fontWeight: '400'
                }}
              >
                <span style={{ 
                  backgroundColor: '#d4d0ff', 
                  width: '24px', 
                  height: '24px', 
                  borderRadius: '50%', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center',
                  fontSize: '14px'
                }}>
                  📝
                </span>
                <span>{suggestions[0].text}</span>
              </button>
              <button 
                onClick={() => handleSuggestionClick(suggestions[1].text)}
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px', 
                  padding: '8px 16px', 
                  border: 'none', 
                  borderRadius: '20px', 
                  backgroundColor: '#ffe5e5', 
                  color: '#333', 
                  fontSize: '14px', 
                  cursor: 'pointer', 
                  whiteSpace: 'nowrap',
                  fontWeight: '400'
                }}
              >
                <span style={{ 
                  backgroundColor: '#ffd0d0', 
                  width: '24px', 
                  height: '24px', 
                  borderRadius: '50%', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center',
                  fontSize: '14px'
                }}>
                  📍
                </span>
                <span>{suggestions[1].text}</span>
              </button>
              <button 
                onClick={() => handleSuggestionClick(suggestions[2].text)}
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px', 
                  padding: '8px 16px', 
                  border: 'none', 
                  borderRadius: '20px', 
                  backgroundColor: '#ffe5ff', 
                  color: '#333', 
                  fontSize: '14px', 
                  cursor: 'pointer', 
                  whiteSpace: 'nowrap',
                  fontWeight: '400'
                }}
              >
                <span style={{ 
                  backgroundColor: '#ffd0ff', 
                  width: '24px', 
                  height: '24px', 
                  borderRadius: '50%', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center',
                  fontSize: '14px'
                }}>
                  📍
                </span>
                <span>{suggestions[2].text}</span>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default SimpleApp;