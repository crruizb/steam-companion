import config from "../config";

/** A request the backend answered with an error status. */
export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

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

/** True when a request failed because the user isn't logged in (401/403). */
export const isAuthError = (error: unknown): boolean =>
  error instanceof ApiError && (error.status === 401 || error.status === 403);

/**
 * fetch() for the backend API: sends the auth cookies and, when the access token
 * has expired (401), refreshes the session once and retries the request.
 * Throws an ApiError for error responses, using the backend's { message } when it sends one.
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

  let response = await request();
  if (response.status === 401 && (await refreshSession())) {
    response = await request();
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(
      response.status,
      body?.message ?? `Request to ${path} failed with status ${response.status}`,
    );
  }
  return response;
};
