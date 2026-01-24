let API_URL = "https://api.steam-companion.dpdns.org/api";
const isLocalhost =
  window.location.hostname === "localhost" ||
  window.location.hostname === "127.0.0.1";
if (isLocalhost) {
  API_URL = "http://localhost:8080/api";
}

const config = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || API_URL,
};

export default config;
