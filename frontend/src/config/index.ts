const config = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  FRONTEND_BASE_URL: import.meta.env.VITE_FRONTEND_BASE_URL || 'http://localhost:5173',
};

export default config;
