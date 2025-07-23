// Message Types
export interface Message {
  id: string;
  text: string;
  isUser: boolean;
  timestamp: Date;
  citations?: Citation[];
  intermediateSteps?: Step[];
  actions?: Action[];
}

export interface Citation {
  id: number;
  text: string;
  source: Source;
}

export interface Source {
  type: 'web' | 'company' | 'file' | 'ai';
  title: string;
  url?: string;
  fileId?: string;
  location?: string; // Section/page in document
  snippet?: string; // Relevant text snippet
}

export interface Step {
  id: number;
  description: string;
  content: string;
}

export interface Action {
  id: string;
  name: string;
  description: string;
  parameters?: Record<string, any>;
  handler: () => void;
}

// User Preferences
export interface UserPreferences {
  uiMode: 'focus' | 'compact';
  showIntermediateSteps: boolean;
  theme: 'light' | 'dark' | 'system';
}

// Chat Session
export interface ChatSession {
  id: string;
  title: string;
  createdAt: Date;
  updatedAt: Date;
  messages: Message[];
  uploadedFiles: UploadedFile[];
}

// Uploaded File
export interface UploadedFile {
  id: string;
  name: string;
  type: string;
  size: number;
  uploadedAt: Date;
  processingStatus: 'pending' | 'processing' | 'completed' | 'failed';
  processingError?: string;
  metadata?: Record<string, any>;
}

// Service Interfaces
export interface AIOptions {
  temperature?: number;
  maxTokens?: number;
  streamResponse?: boolean;
  includeIntermediateSteps?: boolean;
}

export interface AIResponse {
  text: string;
  citations?: Citation[];
  intermediateSteps?: Step[];
}

export interface Resource {
  id: string;
  type: string;
  title: string;
  content: any;
  metadata: Record<string, any>;
}

export interface WebContent {
  url: string;
  title: string;
  content: string;
  metadata: Record<string, any>;
}

export interface FileUploadResult {
  fileId: string;
  status: 'success' | 'error';
  error?: string;
}

export interface FileProcessingResult {
  fileId: string;
  status: 'success' | 'error';
  content?: string;
  error?: string;
  metadata?: Record<string, any>;
}

export interface ActionContext {
  message: Message;
  session: ChatSession;
  userPreferences: UserPreferences;
}

export interface ActionResult {
  success: boolean;
  message: string;
  data?: any;
}

export interface WorkflowContext {
  message: Message;
  session: ChatSession;
  userPreferences: UserPreferences;
}

export interface Workflow {
  id: string;
  name: string;
  description: string;
  steps: WorkflowStep[];
}

export interface WorkflowStep {
  id: string;
  name: string;
  description: string;
  action: Action;
}

export interface WorkflowResult {
  success: boolean;
  message: string;
  completedSteps: string[];
  failedSteps: string[];
  data?: any;
}