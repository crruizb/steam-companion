import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  achievementsFromUser,
  importAchievementsFromUser,
} from "../services/achievementsService.ts";
import type { AchievementsHeatmap } from "../types/index.ts";

export const achievementsKeys = {
  all: ["achievements"] as const,
  user: () => [...achievementsKeys.all, "userachievements"] as const,
  import: () => [...achievementsKeys.all, "import"] as const,
};

export function useImportAchievements() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: achievementsKeys.import(),
    mutationFn: async (): Promise<void> => {
      await importAchievementsFromUser();
    },
    onSuccess: () => {
      toast.dismiss();
      toast.success(
        "Queued import of achievements from Steam... Please refresh in a moment."
      );
      queryClient.invalidateQueries({ queryKey: achievementsKeys.user() });
    },
    onError: (error) => {
      toast.dismiss();
      toast.error("Could not import achievements. Please try again.");
      console.error(error);
    },
  });
}

export function useAchievements() {
  return useQuery({
    queryKey: achievementsKeys.user(),
    queryFn: async (): Promise<AchievementsHeatmap> => {
      return await achievementsFromUser();
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
