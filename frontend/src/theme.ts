import { useState } from "react";

export type Theme = "light" | "dark";

// Also read by the inline script in index.html, which applies the theme before React loads
const STORAGE_KEY = "theme";

const currentTheme = (): Theme =>
  document.documentElement.dataset.theme === "light" ? "light" : "dark";

/** The active theme and a toggle that applies it to <html> and remembers it. */
export function useTheme() {
  const [theme, setTheme] = useState<Theme>(currentTheme);

  const toggleTheme = () => {
    const next: Theme = theme === "dark" ? "light" : "dark";
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Storage can be unavailable (private mode); the theme still applies for this visit
    }
    setTheme(next);
  };

  return { theme, toggleTheme };
}
