import React from 'react';
import { Box, Typography } from '@mui/material';

const SuccessBanner = ({ children }) => {
  return (
    <Box 
      sx={{ 
        backgroundColor: '#d4edda',
        color: '#155724',
        p: 2,
        borderRadius: 1,
        my: 3,
        borderLeft: '5px solid #28a745',
        display: 'flex',
        alignItems: 'center'
      }}
    >
      <Typography 
        variant="body1" 
        sx={{ 
          fontWeight: 'bold',
          display: 'flex',
          alignItems: 'center'
        }}
      >
        <span role="img" aria-label="check" style={{ marginRight: '8px' }}>✓ ✓ ✓</span>
        {children}
        <span role="img" aria-label="check" style={{ marginLeft: '8px' }}>✓ ✓ ✓</span>
      </Typography>
    </Box>
  );
};

export default SuccessBanner;
