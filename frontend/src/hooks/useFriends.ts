import { useQuery } from "@tanstack/react-query";
import {
  fetchAchievementComparison,
  fetchFriends,
  fetchLibraryComparison,
} from "../services/friendsService.ts";
import { ApiError, isAuthError } from "../services/api.ts";

export const friendsKeys = {
  all: ["friends"] as const,
  list: () => [...friendsKeys.all, "list"] as const,
  library: (steamId: string) => [...friendsKeys.all, steamId, "library"] as const,
  achievements: (steamId: string, appId: number) => [...friendsKeys.all, steamId, "achievements", appId] as const,
};

// Everything here is read live from Steam, so cache it for a while and don't retry
// answers that won't change (a private profile, not a friend)
const options = {
  staleTime: 1000 * 60 * 10, // 10 minutes
  retry: (failureCount: number, error: Error) =>
    !isAuthError(error) && !(error instanceof ApiError && error.status < 500) && failureCount < 2,
};

export function useFriends() {
  return useQuery({ queryKey: friendsKeys.list(), queryFn: fetchFriends, ...options });
}

/** Pass null to skip fetching. */
export function useLibraryComparison(friendSteamId: string | null) {
  return useQuery({
    queryKey: friendsKeys.library(friendSteamId ?? ""),
    queryFn: () => fetchLibraryComparison(friendSteamId!),
    enabled: friendSteamId !== null,
    ...options,
  });
}

/** Pass a null appId to skip fetching. */
export function useAchievementComparison(friendSteamId: string, appId: number | null) {
  return useQuery({
    queryKey: friendsKeys.achievements(friendSteamId, appId ?? 0),
    queryFn: () => fetchAchievementComparison(friendSteamId, appId!),
    enabled: appId !== null,
    ...options,
  });
}
