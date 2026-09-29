// Shared button looks, so every action in the app matches
const base =
  "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-accent";

export const primaryButton = `${base} bg-primary text-white hover:bg-primary-hover`;

export const secondaryButton = `${base} border border-line bg-card text-fg hover:bg-card-hover hover:text-heading`;
