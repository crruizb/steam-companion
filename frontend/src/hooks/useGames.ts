import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { User } from "../types";
import {
  fetchOwnedGames,
  importGamesFromSteam,
} from "../services/gamesService.ts";
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
      toast.error("Could not import games. Please try again.");
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
    retry: (failureCount, error: any) => {
      // Don't retry on 401/403 - user is not authenticated
      if (error?.message?.includes("401") || error?.message?.includes("403")) {
        return false;
      }
      return failureCount < 2;
    },
  });
}
