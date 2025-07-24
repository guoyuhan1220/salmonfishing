/**
 * Utility functions for managing citations in the chat assistant
 */

/**
 * Processes text to add citation markers at appropriate positions
 * @param {string} text - The original text
 * @param {Array} citations - Array of citation objects
 * @returns {string} - Text with citation markers inserted
 */
export const addCitationMarkers = (text, citations) => {
  if (!citations || citations.length === 0) {
    return text;
  }

  // Sort citations by ID to ensure consistent numbering
  const sortedCitations = [...citations].sort((a, b) => a.id - b.id);
  
  // This is a simplified implementation - in a real app, you would
  // use NLP or other techniques to determine where to place citations
  let result = text;
  
  sortedCitations.forEach(citation => {
    // For now, we'll just append the citation marker at the end if it's not already in the text
    const marker = `[${citation.id}]`;
    if (!result.includes(marker)) {
      // Find a good place to insert the citation - for now just at the end
      result = `${result} ${marker}`;
    }
  });
  
  return result;
};

/**
 * Extracts citations from a response
 * @param {Object} response - The AI response object
 * @returns {Array} - Array of citation objects with IDs assigned
 */
export const extractCitations = (response) => {
  if (!response || !response.citations || !Array.isArray(response.citations)) {
    return [];
  }
  
  // Ensure all citations have unique IDs
  return response.citations.map((citation, index) => ({
    ...citation,
    id: citation.id || index + 1
  }));
};

/**
 * Formats citation text for display
 * @param {Object} citation - The citation object
 * @returns {string} - Formatted citation text
 */
export const formatCitationText = (citation) => {
  if (!citation || !citation.source) {
    return '';
  }
  
  const { source } = citation;
  
  switch (source.type) {
    case 'web':
      return `${source.title}${source.url ? ` (${source.url})` : ''}`;
    
    case 'company':
      return `${source.title}${source.location ? ` - ${source.location}` : ''}`;
    
    case 'file':
      return `${source.title}${source.location ? ` - ${source.location}` : ''}`;
    
    case 'ai':
      return `AI Generated${source.title ? ` - ${source.title}` : ''}`;
    
    default:
      return source.title || 'Unknown Source';
  }
};
/**
 * 
Verifies a source by checking its authenticity
 * @param {Object} source - The source object to verify
 * @returns {Promise<Object>} - Promise resolving to verification result
 */
export const verifySource = async (source) => {
  if (!source) {
    return { verified: false, reason: 'No source provided' };
  }
  
  // In a real implementation, this would make API calls to verify different source types
  switch (source.type) {
    case 'web':
      // Verify web source by checking URL validity, content matching, etc.
      if (!source.url) {
        return { verified: false, reason: 'No URL provided for web source' };
      }
      
      try {
        // Simulate API call to verify web content
        await new Promise(resolve => setTimeout(resolve, 500));
        
        return { 
          verified: true, 
          lastVerified: new Date().toISOString(),
          verificationMethod: 'URL content verification'
        };
      } catch (error) {
        return { 
          verified: false, 
          reason: 'Failed to verify web content',
          error: error.message
        };
      }
      
    case 'company':
      // Verify company resource by checking internal database
      try {
        // Simulate API call to verify company resource
        await new Promise(resolve => setTimeout(resolve, 500));
        
        return { 
          verified: true, 
          lastVerified: new Date().toISOString(),
          verificationMethod: 'Internal resource verification'
        };
      } catch (error) {
        return { 
          verified: false, 
          reason: 'Failed to verify company resource',
          error: error.message
        };
      }
      
    case 'file':
      // Verify file by checking if it exists and content matches
      if (!source.fileId) {
        return { verified: false, reason: 'No file ID provided' };
      }
      
      try {
        // Simulate API call to verify file
        await new Promise(resolve => setTimeout(resolve, 500));
        
        return { 
          verified: true, 
          lastVerified: new Date().toISOString(),
          verificationMethod: 'File content verification'
        };
      } catch (error) {
        return { 
          verified: false, 
          reason: 'Failed to verify file content',
          error: error.message
        };
      }
      
    case 'ai':
      // AI sources can't be verified in the same way
      return { 
        verified: null, 
        reason: 'AI-generated content cannot be independently verified',
        disclaimer: 'This information is derived from the AI model\'s training data and may not be factually accurate.'
      };
      
    default:
      return { verified: false, reason: 'Unknown source type' };
  }
};

/**
 * Opens a source in the appropriate viewer
 * @param {Object} source - The source object to open
 * @returns {Promise<boolean>} - Promise resolving to success status
 */
export const openSource = async (source) => {
  if (!source) {
    return false;
  }
  
  switch (source.type) {
    case 'web':
      if (source.url) {
        window.open(source.url, '_blank', 'noopener,noreferrer');
        return true;
      }
      return false;
      
    case 'company':
      // In a real implementation, this would open the company resource in the appropriate viewer
      console.log(`Opening company resource: ${source.title}`);
      // Simulate opening resource
      return true;
      
    case 'file':
      if (source.fileId) {
        // In a real implementation, this would open the file in the appropriate viewer
        console.log(`Opening file: ${source.fileId}`);
        // Simulate opening file
        return true;
      }
      return false;
      
    default:
      return false;
  }
};