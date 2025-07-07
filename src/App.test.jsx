import { render, screen } from "@testing-library/react";
import App from "./App";
import { vi } from "vitest";

const originalHarmony = global.harmony;

describe("App", () => {
  let mockGetUser = vi.fn();
  mockGetUser.mockReturnValue({firstName: 'John'});

  beforeEach(() => {
    global.harmony = {
      api: {
        getUser: mockGetUser
      }
    }
  });

  afterEach(() => {
    global.harmony = originalHarmony
  });

  it("should render successfully", async () => {
    render(<App />);
    expect(screen.getByText('Welcome to React in Harmony', {exact: false})).toBeInTheDocument();
  });
  
  it("should render the username successfully", async () => {
    mockGetUser.mockReturnValue({firstName: 'firstNameForTesting'});

    render(<App />);
    expect(screen.getByText('firstNameForTesting')).toBeInTheDocument();
  });

  it('should render the TT queue link with the correct href', () => {
    render(<App />);
    const ttQueueLink = screen.getByTestId('tt-queue') // This looks for `data-testid` in the attributes of the HTML element
    expect(ttQueueLink).toBeInTheDocument();
    expect(ttQueueLink).toHaveAttribute('href', 'https://t.corp.amazon.com/create/templates/ea99e1bb-99d1-43d0-815f-c9927730bd4d')
  });
});
