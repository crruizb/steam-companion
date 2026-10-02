import type { AchievementComparison, Friend, LibraryComparison } from "../types";
import { apiFetch } from "./api";

export const fetchFriends = async (): Promise<Friend[]> => {
  const response = await apiFetch("/friends", { method: "GET" });
  return response.json();
};

export const fetchLibraryComparison = async (friendSteamId: string): Promise<LibraryComparison> => {
  const response = await apiFetch(`/friends/${friendSteamId}/library`, { method: "GET" });
  return response.json();
};

export const fetchAchievementComparison = async (
  friendSteamId: string,
  appId: number,
): Promise<AchievementComparison> => {
  const response = await apiFetch(`/friends/${friendSteamId}/games/${appId}/achievements`, { method: "GET" });
  return response.json();
};
