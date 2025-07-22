import React from 'react';
import {
    FiGlobe,
    FiBook,
    FiZap,
    FiBarChart,
    FiLayers,
    FiGitBranch,
    FiSettings,
    FiMinimize,
    FiPlus,
    FiChevronDown,
    FiX,
    FiSliders,
    FiFileText,
    FiDatabase,
    FiMessageSquare,
    FiPaperclip,
    FiSend,
    FiHelpCircle,
    FiActivity,
    FiStar // Using FiStar instead of FiSparkles
} from 'react-icons/fi';
import { FaSlack, FaJira } from 'react-icons/fa';

// Create an Icons object that contains all the icons
export const Icons = {
    Globe: ({ size, ...props }) => <FiGlobe size={size} {...props} />,
    Book: ({ size, ...props }) => <FiBook size={size} {...props} />,
    Zap: ({ size, ...props }) => <FiZap size={size} {...props} />,
    BarChart: ({ size, ...props }) => <FiBarChart size={size} {...props} />,
    Layers: ({ size, ...props }) => <FiLayers size={size} {...props} />,
    GitBranch: ({ size, ...props }) => <FiGitBranch size={size} {...props} />,
    Settings: ({ size, ...props }) => <FiSettings size={size} {...props} />,
    Minimize: ({ size, ...props }) => <FiMinimize size={size} {...props} />,
    Plus: ({ size, ...props }) => <FiPlus size={size} {...props} />,
    ChevronDown: ({ size, ...props }) => <FiChevronDown size={size} {...props} />,
    X: ({ size, ...props }) => <FiX size={size} {...props} />,
    Sliders: ({ size, ...props }) => <FiSliders size={size} {...props} />,
    FileText: ({ size, ...props }) => <FiFileText size={size} {...props} />,
    Database: ({ size, ...props }) => <FiDatabase size={size} {...props} />,
    MessageSquare: ({ size, ...props }) => <FiMessageSquare size={size} {...props} />,
    Paperclip: ({ size, ...props }) => <FiPaperclip size={size} {...props} />,
    Send: ({ size, ...props }) => <FiSend size={size} {...props} />,
    HelpCircle: ({ size, ...props }) => <FiHelpCircle size={size} {...props} />,
    Sparkles: ({ size, ...props }) => <FiStar size={size} {...props} />, // Using FiStar instead of FiSparkles
    Activity: ({ size, ...props }) => <FiActivity size={size} {...props} />,

    // Third-party icons
    Slack: ({ size, ...props }) => <FaSlack size={size} {...props} />,
    Jira: ({ size, ...props }) => <FaJira size={size} {...props} />
};