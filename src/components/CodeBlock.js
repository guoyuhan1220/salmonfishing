import React from 'react';

/**
 * CodeBlock component - Displays code with a copy button
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Code content to display
 */
const CodeBlock = ({ children }) => {
  const [copied, setCopied] = React.useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(children);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="code-block">
      <div 
        onClick={handleCopy}
        title="Copy source code"
        className={`copy-button ${copied ? 'copied' : ''}`}
      >
        {copied ? "Copied!" : "Copy"}
      </div>
      {children}
    </div>
  );
};

export default CodeBlock;
