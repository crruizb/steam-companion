import type {
  AchievementsHeatmap,
  AchievementsImportStatus,
  GameAchievements,
  RareAchievement,
  YearReview,
} from "../types";
import { apiFetch } from "./api";

export const importAchievementsFromUser = async (): Promise<AchievementsImportStatus> => {
  const response = await apiFetch("/achievements/import", { method: "POST" });
  return response.json();
};

export const achievementsImportStatus = async (): Promise<AchievementsImportStatus> => {
  const response = await apiFetch("/achievements/import/status", { method: "GET" });
  return response.json();
};

export const gameAchievements = async (appId: number): Promise<GameAchievements> => {
  const response = await apiFetch(`/achievements/games/${appId}`, { method: "GET" });
  return response.json();
};

export const rarestAchievements = async (): Promise<RareAchievement[]> => {
  const response = await apiFetch("/achievements/rarest", { method: "GET" });
  return response.json();
};

export const yearReview = async (year: number): Promise<YearReview> => {
  const response = await apiFetch(`/achievements/review/${year}`, { method: "GET" });
  return response.json();
};

export const achievementsFromUser = async (): Promise<AchievementsHeatmap> => {
  const response = await apiFetch("/achievements", { method: "GET" });
  return response.json();
};
