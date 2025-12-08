import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {authService} from "../services/authService.ts";
import { User } from '../types'

// Query Keys
export const authKeys = {
    all: ['auth'] as const,
    user: () => [...authKeys.all, 'user'] as const,
    status: () => [...authKeys.all, 'status'] as const,
}

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
                const userData = await authService.fetchUserData()

                if (userData) {
                    // Store the fresh data
                    authService.storeUser(userData)
                    return userData
                }

                return null
            } catch (error: any) {
                // Check if we have stored data as fallback for network errors
                const storedUser = authService.getStoredUser()

                // If it's a 401/403 error, clear stored data and return null
                if (error?.message?.includes('401') || error?.message?.includes('403')) {
                    authService.clearStoredData()
                    return null
                }

                // For other errors, use stored data if available
                if (storedUser) {
                    console.warn('Using stored user data due to fetch error:', error)
                    return storedUser
                }

                // No stored data available, return null (user not authenticated)
                return null
            }
        },
        staleTime: 1000 * 60 * 5, // 5 minutes
        gcTime: 1000 * 60 * 30, // 30 minutes
        retry: (failureCount, error: any) => {
            // Don't retry on 401/403 - user is not authenticated
            if (error?.message?.includes('401') || error?.message?.includes('403')) {
                return false
            }
            return failureCount < 2
        },
    })
}

/**
 * Hook for logout mutation
 * Automatically clears all auth-related cache
 */
export function useLogout() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (): Promise<void> => {
            await authService.logout()
        },
        onSuccess: () => {
            queryClient.removeQueries({ queryKey: authKeys.all })

            console.log('User logged out successfully')
        },
        onError: (error) => {
            authService.clearStoredData()
            queryClient.removeQueries({ queryKey: authKeys.all })

            console.error('Logout error:', error)
        },
        onSettled: () => {
            queryClient.removeQueries({ queryKey: authKeys.all })
        }
    })
}

/**
 * Hook for token refresh mutation
 */
export function useRefreshToken() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (refreshToken: string) => {
            return await authService.refreshToken(refreshToken)
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: authKeys.user() })
            console.log('Token refreshed successfully')
        },
        onError: (error) => {
            authService.clearStoredData()
            queryClient.removeQueries({ queryKey: authKeys.all })
            console.error('Token refresh failed:', error)
        },
    })
}

export default function useAuth() {
    const { data: user, isLoading: userLoading, error: userError } = useUser()
    const logoutMutation = useLogout()

    // User is authenticated if we have user data
    const isAuthenticated = !!user

    return {
        user,
        isAuthenticated,
        isLoading: userLoading,
        error: userError,
        logout: () => logoutMutation.mutate(),
        isLoggingOut: logoutMutation.isPending,
    }
}


