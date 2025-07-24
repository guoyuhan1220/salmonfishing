import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import FileUploader from './FileUploader';

// Mock LoadingIndicator component
vi.mock('../ui/LoadingIndicator', () => ({
  default: ({ progress, text }) => (
    <div data-testid="loading-indicator">
      {text}: {progress}%
    </div>
  )
}));

// Test suite
describe('FileUploader Component', () => {
  // Mock functions
  const mockOnFileSelect = vi.fn();
  const mockOnCancel = vi.fn();
  
  // Mock file
  const createMockFile = (name, type, size) => {
    const file = new File(['mock file content'], name, { type });
    Object.defineProperty(file, 'size', {
      get() { return size; }
    });
    return file;
  };
  
  beforeEach(() => {
    // Reset mocks
    mockOnFileSelect.mockReset();
    mockOnCancel.mockReset();
  });
  
  test('renders upload dialog correctly', () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    expect(screen.getByText('Upload File')).toBeInTheDocument();
    expect(screen.getByText(/Drag and drop your file here/)).toBeInTheDocument();
    expect(screen.getByText('Browse files')).toBeInTheDocument();
    expect(screen.getByText(/Supported formats/)).toBeInTheDocument();
    expect(screen.getByText(/Maximum file size/)).toBeInTheDocument();
  });
  
  test('handles file selection correctly', async () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    // Create a mock file
    const file = createMockFile('test.pdf', 'application/pdf', 1024 * 1024); // 1MB
    
    // Get the file input
    const input = screen.getByLabelText('File input');
    
    // Simulate file selection
    fireEvent.change(input, { target: { files: [file] } });
    
    // Check if the file preview is shown
    await waitFor(() => {
      expect(screen.getByText('test.pdf')).toBeInTheDocument();
      expect(screen.getByText('application/pdf')).toBeInTheDocument();
      expect(screen.getByText('1.0 MB')).toBeInTheDocument();
    });
    
    // Click the upload button
    fireEvent.click(screen.getByText('Upload'));
    
    // Check if the loading indicator is shown during upload
    await waitFor(() => {
      expect(screen.getByTestId('loading-indicator')).toBeInTheDocument();
    });
    
    // Check if onFileSelect was called with the file
    await waitFor(() => {
      expect(mockOnFileSelect).toHaveBeenCalledWith(file);
    });
  });
  
  test('validates file size', async () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    // Create a mock file that's too large
    const file = createMockFile('large.pdf', 'application/pdf', 11 * 1024 * 1024); // 11MB
    
    // Get the file input
    const input = screen.getByLabelText('File input');
    
    // Simulate file selection
    fireEvent.change(input, { target: { files: [file] } });
    
    // Check if the error message is shown
    await waitFor(() => {
      expect(screen.getByText(/File size.*exceeds the 10MB limit/)).toBeInTheDocument();
    });
    
    // Upload button should be disabled
    expect(screen.getByText('Upload')).toBeDisabled();
  });
  
  test('validates file type', async () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    // Create a mock file with unsupported type
    const file = createMockFile('test.xyz', 'application/xyz', 1024 * 1024); // 1MB
    
    // Get the file input
    const input = screen.getByLabelText('File input');
    
    // Simulate file selection
    fireEvent.change(input, { target: { files: [file] } });
    
    // Check if the error message is shown
    await waitFor(() => {
      expect(screen.getByText(/Unsupported file type/)).toBeInTheDocument();
    });
    
    // Upload button should be disabled
    expect(screen.getByText('Upload')).toBeDisabled();
  });
  
  test('handles cancel button click', () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    // Click the cancel button
    fireEvent.click(screen.getByText('Cancel'));
    
    // Check if onCancel was called
    expect(mockOnCancel).toHaveBeenCalled();
  });
  
  test('handles close button click', () => {
    render(
      <FileUploader 
        onFileSelect={mockOnFileSelect} 
        onCancel={mockOnCancel} 
      />
    );
    
    // Click the close button
    fireEvent.click(screen.getByLabelText('Close'));
    
    // Check if onCancel was called
    expect(mockOnCancel).toHaveBeenCalled();
  });
});