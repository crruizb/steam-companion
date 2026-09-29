/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(),  tailwindcss(),],
  test: {
    // Run west of UTC: date bugs from parsing "YYYY-MM-DD" as UTC only show up there
    env: { TZ: 'America/New_York' },
  },
})
