import React from "react";

/**
 * TipBox component - Displays a tip or hint
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Tip content to display
 * @param {string} props.className - Additional CSS class names
 */
const TipBox = ({ children, className }) => (
  <div className={`tip-box ${className || ""}`}>{children}</div>
);

export default TipBox;
