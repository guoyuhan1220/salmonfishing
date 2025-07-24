#!/bin/bash

# Run all tests with Vitest
echo "Running tests..."
npx vitest run --run

# Check if tests passed
if [ $? -eq 0 ]; then
  echo "All tests passed!"
  exit 0
else
  echo "Some tests failed. Please check the output above."
  exit 1
fi