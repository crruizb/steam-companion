import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { User } from "../types";
import {
  fetchOwnedGames,
  fetchRandomGame,
  importGamesFromSteam,
} from "../services/gamesService.ts";
import { ApiError, isAuthError } from "../services/api.ts";
import toast from "react-hot-toast";

export const gamesKeys = {
  all: ["games"] as const,
  user: () => [...gamesKeys.all, "usergames"] as const,
  import: () => [...gamesKeys.all, "import"] as const,
};

export function useImportGames() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: gamesKeys.import(),
    mutationFn: async (): Promise<void> => {
      await importGamesFromSteam();
    },
    onSuccess: () => {
      toast.dismiss();
      toast.success("Games imported successfully!");
      queryClient.invalidateQueries({ queryKey: gamesKeys.user() });
    },
    onError: (error) => {
      toast.dismiss();
      // 400 carries the backend's reason, e.g. a private Steam profile
      toast.error(
        error instanceof ApiError && error.status === 400
          ? error.message
          : "Could not import games. Please try again."
      );
      console.error(error);
    },
    onMutate: () => {
      toast.loading("Importing games from Steam...");
    },
  });
}

export function useUserGames() {
  return useQuery({
    queryKey: gamesKeys.user(),
    queryFn: async (): Promise<User> => {
      return await fetchOwnedGames();
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 30, // 30 minutes
    // Don't retry on 401/403 - user is not authenticated
    retry: (failureCount, error) => !isAuthError(error) && failureCount < 2,
  });
}

// A mutation, not a query, although it's a GET: it should only run on click and every
// click needs a fresh pick, so none of useQuery's automatic fetching or caching applies
export function useRandomGame() {
  return useMutation({
    mutationFn: fetchRandomGame,
    onError: (error) => {
      toast.error(
        error instanceof ApiError && error.status === 404
          ? "Import your games first, then we can pick one for you."
          : "Could not pick a random game. Please try again."
      );
    },
  });
}
