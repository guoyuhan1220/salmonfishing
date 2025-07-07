import React from 'react';

/**
 * TutorialStep component - Wraps each tutorial step
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child elements to render inside the step
 * @param {string} props.className - Additional CSS classes to apply
 */
const TutorialStep = ({ children, className = '' }) => {
  return (
    <div className={`tutorial-step-wrapper ${className}`}>
      {children}
    </div>
  );
};

export default TutorialStep;
