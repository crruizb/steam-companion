import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { User } from "../types";
import {
  clearStoredData,
  fetchUserData,
  getStoredUser,
  logout,
  storeUser,
} from "../services/authService.ts";
import { isAuthError } from "../services/api.ts";

// Query Keys
export const authKeys = {
  all: ["auth"] as const,
  user: () => [...authKeys.all, "user"] as const,
  status: () => [...authKeys.all, "status"] as const,
};

/**
 * Hook to get the current user data
 * Automatically handles caching and background refetching
 */
export function useUser() {
  return useQuery({
    queryKey: authKeys.user(),
    queryFn: async (): Promise<User | null> => {
      try {
        // Try to fetch fresh data from API
        const userData = await fetchUserData();

        if (userData) {
          // Store the fresh data
          storeUser(userData);
          return userData;
        }

        return null;
      } catch (error) {
        // Check if we have stored data as fallback for network errors
        const storedUser = getStoredUser();

        // If it's a 401/403 error, clear stored data and return null
        if (isAuthError(error)) {
          clearStoredData();
          return null;
        }

        // For other errors, use stored data if available
        if (storedUser) {
          console.warn("Using stored user data due to fetch error:", error);
          return storedUser;
        }

        // No stored data available, return null (user not authenticated)
        return null;
      }
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 30, // 30 minutes
    // Don't retry on 401/403 - user is not authenticated
    retry: (failureCount, error) => !isAuthError(error) && failureCount < 2,
  });
}

/**
 * Hook for logout mutation
 * Automatically clears all auth-related cache
 */
export function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (): Promise<void> => {
      await logout();
    },
    onSuccess: () => {
      queryClient.clear();
      window.location.reload();
    },
    onError: (error) => {
      clearStoredData();
      queryClient.clear();
      window.location.reload();

      console.error("Logout error:", error);
    },
  });
}
