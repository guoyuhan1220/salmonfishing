import React from "react";
import { Box, Typography } from "@mui/material";

const TutorialHeader = ({ children }) => {
  return (
    <Box
      sx={{
        backgroundColor: "#e8eaf6",
        p: 3,
        mb: 4,
        borderRadius: 2,
        textAlign: "center",
        borderBottom: "5px solid #3f51b5",
      }}
    >
      <Typography
        variant="h4"
        component="h2"
        gutterBottom
        sx={{
          color: "#3f51b5",
          fontWeight: "bold",
          textTransform: "uppercase",
        }}
      >
        {children}
      </Typography>
    </Box>
  );
};

export default TutorialHeader;
