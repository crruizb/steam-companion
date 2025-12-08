import {RefreshTokenRequest, TokenResponse} from '../types';
import config from '../config';

export interface IAuthService {
  initiateSteamLogin(): void;
  refreshToken(refreshToken: string): Promise<TokenResponse>;
  checkAuthenticationStatus(): Promise<boolean>;
  fetchUserData(): Promise<any>;
  logout(): Promise<void>;
  clearStoredData(): void;
  storeUser(user: any): void;
  getStoredUser(): any | null;
}

export class AuthService implements IAuthService {
  private readonly apiBaseUrl: string;
  private readonly storageKey: string = 'user';

  constructor(apiBaseUrl?: string) {
    this.apiBaseUrl = apiBaseUrl || config.API_BASE_URL;
  }

  /**
   * Initiates Steam login by redirecting to the backend Steam auth endpoint
   */
  initiateSteamLogin(): void {
      window.location.href = `${this.apiBaseUrl}/auth/steam/login`;
  }

  /**
   * Refreshes the access token using a refresh token
   */
  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await fetch(`${this.apiBaseUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken } as RefreshTokenRequest),
    });

    if (!response.ok) {
      throw new Error('Failed to refresh token');
    }

    return response.json();
  }

  /**
   * Check if user is authenticated by making a request to a protected endpoint
   */
  async checkAuthenticationStatus(): Promise<boolean> {
    try {
      console.log('Checking authentication status...');
      const response = await fetch(`${this.apiBaseUrl}/user/me`, {
        method: 'GET',
        credentials: 'include', // Include cookies in the request
        headers: {
          'Content-Type': 'application/json',
        },
      });
      console.log('Authentication check response:', response.status, response.ok);
      return response.ok;
    } catch (error) {
      console.error('Authentication check failed:', error);
      return false;
    }
  }

  /**
   * Fetch user data from the API
   */
  async fetchUserData(): Promise<any> {
    console.log('Fetching user data...');
    const response = await fetch(`${this.apiBaseUrl}/user/me`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    console.log('Fetch user data response:', response.status, response.ok);

    if (!response.ok) {
      throw new Error(`Failed to fetch user data: ${response.status}`);
    }

    const userData = await response.json();
    console.log('User data received:', userData);
    return userData;
  }

  /**
   * Logs out the user by calling the backend logout endpoint
   */
  async logout(): Promise<void> {
    try {
      await fetch(`${this.apiBaseUrl}/auth/logout`, {
        method: 'POST',
        credentials: 'include', // Include cookies in the request
        headers: {
          'Content-Type': 'application/json',
        },
      });
    } catch (error) {
      console.error('Logout request failed:', error);
    } finally {
      // Clear user data from localStorage
      this.clearStoredData();
    }
  }

  /**
   * Clears stored user data from localStorage
   */
  clearStoredData(): void {
    localStorage.removeItem(this.storageKey);
  }

  /**
   * Stores user data in localStorage
   */
  storeUser(user: any): void {
    localStorage.setItem(this.storageKey, JSON.stringify(user));
  }

  /**
   * Retrieves stored user data from localStorage
   */
  getStoredUser(): any | null {
    const userData = localStorage.getItem(this.storageKey);
    return userData ? JSON.parse(userData) : null;
  }
}

export const authService = new AuthService();
