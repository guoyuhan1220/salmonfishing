import React, { useState, useEffect } from "react";
import "./App.css";

function App() {
  const [currentStep, setCurrentStep] = useState(1);
  const [showToast, setShowToast] = useState(false); // Start hidden
  const totalSteps = 6;

  // Show toast after 1.5 seconds with animation
  useEffect(() => {
    const timer = setTimeout(() => {
      setShowToast(true);
    }, 1500);
    return () => clearTimeout(timer);
  }, []);

  // Handle Escape key to close toast
  useEffect(() => {
    const handleEscapeKey = (event) => {
      if (event.key === 'Escape' && showToast) {
        setShowToast(false);
      }
    };

    document.addEventListener('keydown', handleEscapeKey);
    return () => {
      document.removeEventListener('keydown', handleEscapeKey);
    };
  }, [showToast]);

  const stepTitles = {
    1: "What You'll Learn",
    2: "Setup Workspace",
    3: "Modify Text",
    4: "Try Amazon Q Plugin",
    5: "Try Amazon Q CLI",
    6: "What's Next",
  };

  const copyToClipboard = (text, buttonElement) => {
    navigator.clipboard
      .writeText(text)
      .then(() => {
        const originalText = buttonElement.textContent;

        buttonElement.textContent = "Copied!";
        buttonElement.style.backgroundColor = "#38a169";
        buttonElement.style.color = "#ffffff";
        buttonElement.style.borderColor = "#38a169";

        setTimeout(() => {
          buttonElement.textContent = originalText;
          buttonElement.style.backgroundColor = "";
          buttonElement.style.color = "";
          buttonElement.style.borderColor = "";
        }, 1000);
      })
      .catch((err) => {
        console.error("Failed to copy: ", err);
      });
  };

  const nextStep = () => {
    if (currentStep < totalSteps) {
      setCurrentStep(currentStep + 1);
      // Scroll step content to top
      const stepContent = document.querySelector(".step-content");
      if (stepContent) {
        stepContent.scrollTop = 0;
      }
    } else {
      alert("Tutorial completed! 🎉");
    }
  };

  const previousStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
      // Scroll step content to top
      const stepContent = document.querySelector(".step-content");
      if (stepContent) {
        stepContent.scrollTop = 0;
      }
    }
  };

  const goToStep = (step) => {
    // Allow navigation to any step
    // If jumping ahead, mark intermediate steps as completed
    if (step !== currentStep) {
      setCurrentStep(step);
      // Scroll step content to top
      const stepContent = document.querySelector(".step-content");
      if (stepContent) {
        stepContent.scrollTop = 0;
      }
    }
  };

  return (
    <div className="tutorial-wrapper">
      {/* Congratulations Toast */}
      {showToast && (
        <div 
          className="toast toast-show"
          onClick={() => window.open('https://survey.fieldsense.whs.amazon.dev/survey/4784af09-42f6-45a4-9391-ed171764e160', '_blank')}
          style={{ cursor: 'pointer' }}
        >
          <div className="toast-content">
            <div className="toast-text">
              <div className="toast-title">Congrats, you're an explorer!</div>
              <div className="toast-message">
                You made it through the local environment setup and are ready to start exploring new tools! 
                <br />
                <br />
                By taking this  {" "}
                <a 
                  href="https://survey.fieldsense.whs.amazon.dev/survey/4784af09-42f6-45a4-9391-ed171764e160" 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="toast-link"
                >
                  quick survey
                </a>, you can acquire a shiny new PhoneTool badge!
              </div>
            </div>
            <button 
              className="toast-close" 
              onClick={(e) => {
                e.stopPropagation();
                setShowToast(false);
              }}
              aria-label="Close notification"
            >
              ×
            </button>
          </div>
        </div>
      )}
      
      {/* Progress Sidebar */}
      <div className="progress-sidebar">
        <div className="progress-header">
          <h1>Starting up the Vibe Sandbox</h1>
          <p>
            Learn how to get your development ready local environment setup to start working with AI-assisted tools in VS Code.
          </p>
        </div>

        <div className="step-timeline">
          {[1, 2, 3, 4, 5, 6].map((step) => (
            <div
              key={step}
              className={`step-timeline-item ${
                step === currentStep ? "active" : ""
              } ${step <= currentStep ? "completed" : ""}`}
              data-step={step}
              onClick={() => goToStep(step)}
              style={{ cursor: "pointer" }}
            >
              <div className="step-timeline-marker">
                {step <= currentStep ? "" : step}
              </div>
              <div className="step-timeline-content">
                <div className="step-timeline-title">
                  {stepTitles[step]}
                </div>
                <div className="step-timeline-desc">
                  {step === 1 &&
                    "Overview of local development and AI assistant integration"}
                  {step === 2 &&
                    "Open VS Code, navigate files, and access Amazon Q"}
                  {step === 3 &&
                    "Make your first code change and see live updates"}
                  {step === 4 &&
                    "Use the conversational assistant to update the page"}
                  {step === 5 &&
                    "Use command line assistant for advanced code changes"}
                  {step === 6 && "Reset your app and start experimenting"}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Tutorial Container */}
      <div className="tutorial-container">
        {/* Header */}
        <div className="tutorial-header">
          <h2 id="current-step-title">{stepTitles[currentStep]}</h2>
        </div>

        {/* Step Content */}
        <div className="step-content">
          {/* Step 1: What You'll Learn */}
          {currentStep === 1 && (
            <div className="step-panel active" id="step-1">
              <div className="step-intro first">
                <p>
                  The tutorial is set up to run as a local React application on
                  your desktop, so you should be all set to start exploring and
                  working with your code editor. The goal of this is to allow
                  you to fully leverage the capabilities of making use of
                  different AI-assisted tools that can bring your concepts to
                  life in the browser.
                </p>
                <p>
                  The hands-on, AI-supported environment is designed to empower
                  you to dive into development with confidence. As you work
                  through the tutorial, don't hesitate to utilize the
                  assistant's knowledge and capabilities to bring your projects
                  to life and keep your skills sharp.
                </p>
              </div>

              {/* Get Started button for step 1 */}
              <div style={{ textAlign: "center", marginTop: "1rem" }}>
                <button
                  className="nav-button primary"
                  id="get-started-btn"
                  onClick={nextStep}
                >
                  Get Started →
                </button>
              </div>

              {/* Environment Setup Note */}
              <div className="console-error">
                <strong>Note:</strong> If you haven't completed the Vibe
                Environment setup, it is recommended that you visit{" "}
                <a
                  href="https://beta.console.harmony.a2z.com/vibe-environment-setup"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  the setup guide
                </a>{" "}
                to prepare your local environment first.
              </div>
            </div>
          )}

          {/* Step 2: Setting up workspace and Finding a file */}
          {currentStep === 2 && (
            <div className="step-panel active" id="step-2">
              <div className="step-intro">
                <p>
                  Let's get your development environment ready and locate the
                  main React component file. You'll learn to navigate VS Code
                  like a developer and access the Amazon Q plugin for
                  assistance.
                </p>
              </div>

              <div className="instruction-steps">
                <div className="instruction-step">
                  <div className="step-number">1</div>
                  <div className="step-details">
                    <h4>Open Project Folder</h4>
                    <p>
                      Start a new workspace by opening up the new project's
                      folder:
                      <br />
                      <br />
                      <strong>
                        File menu → Open project →
                        Desktop/vibe-sandbox
                      </strong>
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">2</div>
                  <div className="step-details">
                    <h4>Access File Explorer</h4>
                    <p>
                      Look at the left sidebar in VS Code, and click on the{" "}
                      <strong>Explorer icon</strong> (it looks like a document).
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">3</div>
                  <div className="step-details">
                    <h4>Navigate to App.js</h4>
                    <p>
                      Navigate to the <strong>'src'</strong> folder, and
                      double-click on <strong>'App.js'</strong> to open it
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">4</div>
                  <div className="step-details">
                    <h4>Open Amazon Q Plugin</h4>
                    <p>
                      Open the Q Plugin by clicking on the{" "}
                      <strong>Amazon Q icon</strong> in the sidebar, or by
                      clicking <strong>Amazon Q</strong> in the bottom status
                      bar and selecting "Open Chat panel"
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Modify some text */}
          {currentStep === 3 && (
            <div className="step-panel active" id="step-3">
              <div className="step-intro">
                <p>
                  Now let's make your first change to the React application and
                  see it update in real-time. This is where the magic happens -
                  you'll experience the instant feedback loop that makes modern
                  web development so powerful.
                </p>
              </div>

              <div className="instruction-steps">
                <div className="instruction-step">
                  <div className="step-number">1</div>
                  <div className="step-details">
                    <h4>Find the Text to Change</h4>
                    <p>
                      Use search to find this string in the section in App.js
                      file (Use <strong>⌘ + F</strong>)
                    </p>
                    <div className="code-block">
                      <span>HELLO-WORLD</span>
                      <button
                        className="copy-button"
                        onClick={(e) =>
                          copyToClipboard("HELLO-WORLD", e.target)
                        }
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">2</div>
                  <div className="step-details">
                    <h4>Make Your Change</h4>
                    <p>
                      Change that string to something else, and save your file
                      (Use <strong>⌘ + S</strong>)
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">3</div>
                  <div className="step-details">
                    <h4>See the Results</h4>
                    <p>Check your browser to see the changes applied!</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Try the Amazon Q IDE plugin */}
          {currentStep === 4 && (
            <div className="step-panel active" id="step-4">
              <div className="step-intro">
                <p>
                  Let's use the Amazon Q Plugin to make more advanced changes to
                  your application. You'll discover how AI can accelerate your
                  development workflow and help you build more sophisticated
                  features with natural language prompts.
                </p>
              </div>

              <div className="instruction-steps">
                <div className="instruction-step">
                  <div className="step-number">1</div>
                  <div className="step-details">
                    <h4>Open Amazon Q Panel</h4>
                    <p>
                      If it's not already open, look for the{" "}
                      <strong>Amazon Q icon</strong> in the sidebar
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">2</div>
                  <div className="step-details">
                    <h4>Access Chat Panel</h4>
                    <p>
                      Click on the icon to open the Q Plugin panel, or click{" "}
                      <strong>Amazon Q</strong> in the bottom status bar and
                      select "Open Chat panel"
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">3</div>
                  <div className="step-details">
                    <h4>Try This Prompt</h4>
                    <p>
                      Try asking Q the prompt below, and then check the browser
                      to see the changes applied:
                    </p>
                    <div className="code-block">
                      Can you update the code to add a small animated cloud to
                      float over the page? Make this a quick tweak, no need to
                      commit changes.
                      <button
                        className="copy-button"
                        onClick={(e) =>
                          copyToClipboard(
                            "Can you update the code to add a small animated cloud to float over the page? Make this a quick tweak, no need to commit changes.",
                            e.target
                          )
                        }
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">4</div>
                  <div className="step-details">
                    <h4>View response, and wait for page refresh</h4>
                    <p>
                      Q will process your request, update the code, and save the
                      file which will reload this page automatically!
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 5: Try Amazon Q CLI */}
          {currentStep === 5 && (
            <div className="step-panel active" id="step-5">
              <div className="step-intro">
                <p>
                  Now let's dive a little deeper and instead of using the
                  plugin, let's use the Q CLI in the terminal to get used to
                  more advanced usage.
                </p>
              </div>

              <div className="instruction-steps">
                <div className="instruction-step">
                  <div className="step-number">1</div>
                  <div className="step-details">
                    <h4>Resetting your terminal in VS Code</h4>
                    <p>
                      Depending on how you got started with this tutorial, you
                      may already have a server running in your terminal. If you
                      do, you can press <strong>Control + X</strong> to stop the
                      server.
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">2</div>
                  <div className="step-details">
                    <h4>Open a terminal in VS Code</h4>
                    <p>
                      If you don't already have a terminal open, go ahead and
                      open a new one.
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">3</div>
                  <div className="step-details">
                    <h4>Start Q CLI</h4>
                    <p>Enter the Q prompt to get started</p>
                    <div className="code-block">
                      q
                      <button
                        className="copy-button"
                        onClick={(e) => copyToClipboard("q", e.target)}
                      >
                        Copy
                      </button>
                    </div>
                    <p>or you can use Q Chat</p>
                    <div className="code-block">
                      q chat
                      <button
                        className="copy-button"
                        onClick={(e) => copyToClipboard("q chat", e.target)}
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">4</div>
                  <div className="step-details">
                    <h4>Ask some questions</h4>
                    <p>
                      The prompt functions the same as the conversational agent,
                      but you might see responses more quickly and receive more
                      detailed information.
                    </p>
                    <div className="code-block">
                      is it possible to change the color of the cloud? if so,
                      how could I do that?
                      <button
                        className="copy-button"
                        onClick={(e) =>
                          copyToClipboard(
                            "is it possible to change the color of the cloud? if so, how could I do that?",
                            e.target
                          )
                        }
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">5</div>
                  <div className="step-details">
                    <h4>Now you know</h4>
                    <p>
                      You will now see that both Q tools can be helpful in
                      different ways!
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 6: What's Next */}
          {currentStep === 6 && (
            <div className="step-panel active" id="step-6">
              <div className="step-intro">
                <p>
                  Now let's use Amazon Q to reset this application to a minimal
                  "Hello World" state and send you off to start experiment
                  what's possible
                </p>
              </div>

              <div className="instruction-steps">
                <div className="instruction-step">
                  <div className="step-number">1</div>
                  <div className="step-details">
                    <h4>Access Q CLI or Q Plugin</h4>
                    <p>
                      Open the Amazon Q Plugin panel or open the CLI as covered
                      in the last 2 steps.
                    </p>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">2</div>
                  <div className="step-details">
                    <h4>Transform Your App</h4>
                    <p>
                      Ask Q the following prompt and see your app transform into
                      a clean slate ready for your own creative work!
                    </p>
                    <div className="code-block">
                      Please transform this application into a minimal React app
                      that just displays "Hello World" centered on the page.
                      Remove all the tutorial components and extra dependencies.
                      <button
                        className="copy-button"
                        onClick={(e) =>
                          copyToClipboard(
                            'Please transform this application into a minimal React app that just displays "Hello World" centered on the page. Remove all the tutorial components and extra dependencies.',
                            e.target
                          )
                        }
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">3</div>
                  <div className="step-details">
                    <h4>Start the Server Yourself</h4>
                    <p>
                      To run the server yourself, you can do just open a
                      terminal and enter the command
                    </p>
                    <div className="code-block">
                      npm start
                      <button
                        className="copy-button"
                        onClick={(e) => copyToClipboard("npm start", e.target)}
                      >
                        Copy
                      </button>
                    </div>
                    <p>or ask the Q Plugin</p>
                    <div className="code-block">
                      start up a server for this project!
                      <button
                        className="copy-button"
                        onClick={(e) =>
                          copyToClipboard(
                            "start up a server for this project!",
                            e.target
                          )
                        }
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <div className="instruction-step">
                  <div className="step-number">4</div>
                  <div className="step-details">
                    <h4>Experiment and Build</h4>
                    <p>
                      You're just getting started, experiment and see what you
                      can put together!
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Navigation */}
        <div className="bottom-navigation">
          <button
            className="nav-button"
            id="prev-btn"
            onClick={previousStep}
            style={{ display: currentStep > 1 ? "block" : "none" }}
          >
            ← Previous
          </button>
          <div
            className="step-counter"
            id="step-counter"
            style={{ display: currentStep === 1 ? "none" : "block" }}
          >
            Step {currentStep} of {totalSteps}
          </div>
          <button
            className="nav-button primary"
            id="next-btn"
            onClick={nextStep}
            style={{ display: currentStep === 1 ? "none" : "block" }}
          >
            {currentStep < totalSteps ? "Next →" : "Complete!"}
          </button>
        </div>
      </div>

      {/* Add a special marker for step 3 */}
      <div style={{ display: "none" }}>HELLO-WORLD</div>
    </div>
  );
}

export default App;
