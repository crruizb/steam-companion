import type { RefreshTokenRequest, TokenResponse } from "../types";
import config from "../config";

const storageKey = "user";

/**
 * Initiates Steam login by redirecting to the backend Steam auth endpoint
 */
export const initiateSteamLogin = (): void => {
  window.location.href = `${config.API_BASE_URL}/auth/steam/login`;
};

/**
 * Refreshes the access token using a refresh token
 */
export const refreshToken = async (
  refreshToken: string
): Promise<TokenResponse> => {
  const response = await fetch(`${config.API_BASE_URL}/auth/refresh`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ refreshToken } as RefreshTokenRequest),
  });

  if (!response.ok) {
    throw new Error("Failed to refresh token");
  }

  return response.json();
};

/**
 * Check if user is authenticated by making a request to a protected endpoint
 */
export const checkAuthenticationStatus = async (): Promise<boolean> => {
  try {
    const response = await fetch(`${config.API_BASE_URL}/user/me`, {
      method: "GET",
      credentials: "include", // Include cookies in the request
      headers: {
        "Content-Type": "application/json",
      },
    });
    return response.ok;
  } catch (error) {
    console.error("Authentication check failed:", error);
    return false;
  }
};

/**
 * Fetch user data from the API
 */
export const fetchUserData = async (): Promise<any> => {
  const response = await fetch(`${config.API_BASE_URL}/user/me`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch user data: ${response.status}`);
  }

  const userData = await response.json();
  return userData;
};

/**
 * Logs out the user by calling the backend logout endpoint
 */
export const logout = async (): Promise<void> => {
  try {
    await fetch(`${config.API_BASE_URL}/auth/logout`, {
      method: "POST",
      credentials: "include", // Include cookies in the request
      headers: {
        "Content-Type": "application/json",
      },
    });
  } catch (error) {
    console.error("Logout request failed:", error);
  } finally {
    // Clear user data from localStorage
    this.clearStoredData();
  }
};

/**
 * Clears stored user data from localStorage
 */
export const clearStoredData = (): void => {
  localStorage.removeItem(storageKey);
};

/**
 * Stores user data in localStorage
 */
export const storeUser = (user: any): void => {
  localStorage.setItem(storageKey, JSON.stringify(user));
};

/**
 * Retrieves stored user data from localStorage
 */
export const getStoredUser = (): any | null => {
  const userData = localStorage.getItem(storageKey);
  return userData ? JSON.parse(userData) : null;
};
