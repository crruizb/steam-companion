import type { AchievementsHeatmap, AchievementsImportStatus } from "../types";
import { apiFetch } from "./api";

export const importAchievementsFromUser = async (): Promise<AchievementsImportStatus> => {
  const response = await apiFetch("/achievements/import", { method: "POST" });
  return response.json();
};

export const achievementsImportStatus = async (): Promise<AchievementsImportStatus> => {
  const response = await apiFetch("/achievements/import/status", { method: "GET" });
  return response.json();
};

export const achievementsFromUser = async (): Promise<AchievementsHeatmap> => {
  const response = await apiFetch("/achievements", { method: "GET" });
  return response.json();
};
