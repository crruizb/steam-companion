import config from "../config";

// Shared by concurrent requests, so a burst of 401s triggers a single refresh.
// Refresh tokens are single-use, so parallel refreshes would invalidate each other.
let refreshInFlight: Promise<boolean> | null = null;

/**
 * Asks the backend for new auth cookies using the refresh token cookie.
 * Resolves to false when the session can't be refreshed (the user must log in again).
 */
export const refreshSession = (): Promise<boolean> => {
  refreshInFlight ??= fetch(`${config.API_BASE_URL}/auth/refresh`, {
    method: "POST",
    credentials: "include",
  })
    .then((response) => response.ok)
    .catch(() => false)
    .finally(() => {
      refreshInFlight = null;
    });
  return refreshInFlight;
};

/**
 * True when a request failed because the user isn't logged in (401/403).
 * The services include the HTTP status in their error messages.
 */
export const isAuthError = (error: unknown): boolean =>
  error instanceof Error && /\b40[13]\b/.test(error.message);

/**
 * fetch() for the backend API: sends the auth cookies and, when the access token
 * has expired (401), refreshes the session once and retries the request.
 */
export const apiFetch = async (
  path: string,
  init: RequestInit = {},
): Promise<Response> => {
  const request = () =>
    fetch(`${config.API_BASE_URL}${path}`, {
      ...init,
      credentials: "include",
      headers: { "Content-Type": "application/json", ...init.headers },
    });

  const response = await request();
  if (response.status !== 401 || !(await refreshSession())) {
    return response;
  }
  return request();
};
