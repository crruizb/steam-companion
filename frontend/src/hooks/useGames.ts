import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Game, RandomGameFilter, User } from "../types";
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

export const RANDOM_GAME_FILTERS: { value: RandomGameFilter; label: string }[] = [
  { value: "ANY", label: "Any game" },
  { value: "NEVER_PLAYED", label: "Never played" },
  { value: "UNDER_TWO_HOURS", label: "Under 2 hours" },
  { value: "NOT_PLAYED_IN_A_YEAR", label: "Not played in a year" },
  { value: "ACHIEVEMENTS_LEFT", label: "Achievements left" },
];

// The last used filter is a per-browser convenience; storage can be unavailable (private mode)
const FILTER_STORAGE_KEY = "randomGameFilter";
const loadFilter = (): RandomGameFilter => {
  try {
    const stored = localStorage.getItem(FILTER_STORAGE_KEY);
    return RANDOM_GAME_FILTERS.some((f) => f.value === stored) ? (stored as RandomGameFilter) : "ANY";
  } catch {
    return "ANY";
  }
};
const saveFilter = (filter: RandomGameFilter) => {
  try {
    localStorage.setItem(FILTER_STORAGE_KEY, filter);
  } catch {
    // Not remembered, but the filter still applies
  }
};

/**
 * State for the random game picker dialog. Picks are a mutation, not a query, although it's a
 * GET: they only run on click and every click needs a fresh pick, so no caching applies.
 * Rerolls and filter changes skip the game on screen, so the same game never shows twice in a row.
 */
export function useRandomGamePicker() {
  const [isOpen, setIsOpen] = useState(false);
  const [filter, setFilter] = useState<RandomGameFilter>(loadFilter);
  const [game, setGame] = useState<Game | null>(null);
  const { mutate, isPending, error, reset } = useMutation({ mutationFn: fetchRandomGame });

  const pick = (nextFilter: RandomGameFilter, exclude?: number) =>
    mutate({ filter: nextFilter, exclude }, { onSuccess: setGame, onError: () => setGame(null) });

  return {
    isOpen,
    filter,
    game,
    isPicking: isPending,
    // 404 carries the reason: no games imported, or none match the filter
    error: error
      ? error instanceof ApiError && error.status === 404
        ? error.message
        : "Could not pick a random game. Please try again."
      : null,
    open: () => {
      setIsOpen(true);
      pick(filter);
    },
    reroll: () => pick(filter, game?.appId),
    changeFilter: (next: RandomGameFilter) => {
      setFilter(next);
      saveFilter(next);
      pick(next, game?.appId);
    },
    close: () => {
      setIsOpen(false);
      setGame(null);
      reset();
    },
  };
}
