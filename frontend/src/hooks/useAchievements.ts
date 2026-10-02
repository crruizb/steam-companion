import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  achievementsFromUser,
  achievementsImportStatus,
  importAchievementsFromUser,
} from "../services/achievementsService.ts";
import { ApiError, isAuthError } from "../services/api.ts";
import type { AchievementsHeatmap, AchievementsImportStatus } from "../types/index.ts";

export const achievementsKeys = {
  all: ["achievements"] as const,
  user: () => [...achievementsKeys.all, "userachievements"] as const,
  import: () => [...achievementsKeys.all, "import"] as const,
  importStatus: () => [...achievementsKeys.all, "import", "status"] as const,
};

// One id for every import toast, so the progress toast is replaced by the result
const IMPORT_TOAST_ID = "achievements-import";
const IMPORT_POLL_INTERVAL_MS = 2000;

/**
 * Status of the background achievements import. Polls while an import runs, and when it
 * finishes refreshes the heatmap and reports the result. Also picks up an import that was
 * started before a page reload.
 */
export function useAchievementsImportStatus() {
  const queryClient = useQueryClient();
  return useQuery({
    queryKey: achievementsKeys.importStatus(),
    queryFn: async (): Promise<AchievementsImportStatus> => {
      const previous = queryClient.getQueryData<AchievementsImportStatus>(
        achievementsKeys.importStatus()
      );
      const status = await achievementsImportStatus();
      // Detected here rather than in a component effect, so it runs once however many components use the hook
      if (previous?.state === "RUNNING" && status.state !== "RUNNING") {
        onImportFinished(status);
        queryClient.invalidateQueries({ queryKey: achievementsKeys.user() });
      }
      return status;
    },
    refetchInterval: (query) =>
      query.state.data?.state === "RUNNING" ? IMPORT_POLL_INTERVAL_MS : false,
    retry: (failureCount, error) => !isAuthError(error) && failureCount < 2,
  });
}

function onImportFinished(status: AchievementsImportStatus) {
  if (status.state === "FAILED") {
    toast.error("The achievements import stopped early. Please try again.", { id: IMPORT_TOAST_ID });
  } else if (status.importedAchievements === 0) {
    toast.success("Achievements are up to date, nothing new to import.", { id: IMPORT_TOAST_ID });
  } else {
    const count = status.importedAchievements;
    toast.success(`Imported ${count} new achievement${count === 1 ? "" : "s"}!`, { id: IMPORT_TOAST_ID });
  }
}

export function useImportAchievements() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: achievementsKeys.import(),
    mutationFn: importAchievementsFromUser,
    onSuccess: (status) => {
      // Seeding the status starts the polling in useAchievementsImportStatus
      queryClient.setQueryData(achievementsKeys.importStatus(), status);
      toast.loading("Importing achievements from Steam...", { id: IMPORT_TOAST_ID });
    },
    onError: (error) => {
      // 400 carries the backend's reason, e.g. no games imported yet
      toast.error(
        error instanceof ApiError && error.status === 400
          ? error.message
          : "Could not import achievements. Please try again.",
        { id: IMPORT_TOAST_ID }
      );
      console.error(error);
    },
  });
}

/** Starts an import and tracks it, for the buttons that trigger one. */
export function useAchievementsImport() {
  const { mutate: startImport, isPending } = useImportAchievements();
  const { data: status } = useAchievementsImportStatus();
  const isRunning = status?.state === "RUNNING";

  return {
    startImport: () => startImport(),
    isImporting: isPending || isRunning,
    // e.g. "12/160 games", shown on the button while the import runs
    progress: isRunning && status.totalGames > 0
      ? `${status.processedGames}/${status.totalGames} games`
      : null,
  };
}

export function useAchievements() {
  return useQuery({
    queryKey: achievementsKeys.user(),
    queryFn: async (): Promise<AchievementsHeatmap> => {
      return await achievementsFromUser();
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 30, // 30 minutes
    // Don't retry on 401/403 - user is not authenticated
    retry: (failureCount, error) => !isAuthError(error) && failureCount < 2,
  });
}
