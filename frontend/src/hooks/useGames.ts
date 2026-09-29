import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { User } from "../types";
import {
  fetchOwnedGames,
  fetchRandomGame,
  importGamesFromSteam,
} from "../services/gamesService.ts";
import { isAuthError } from "../services/api.ts";
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
      toast.error(error.message);
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

export function useRandomGame() {
  return useMutation({
    mutationFn: fetchRandomGame,
    onError: (error) => {
      toast.error(error.message);
    },
  });
}
