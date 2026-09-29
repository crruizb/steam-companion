import type { AchievementsHeatmap } from "../types";
import { apiFetch } from "./api";

export const importAchievementsFromUser = async (): Promise<void> => {
  await apiFetch("/achievements/import", { method: "POST" });
};

export const achievementsFromUser = async (): Promise<AchievementsHeatmap> => {
  const response = await apiFetch("/achievements", { method: "GET" });
  return response.json();
};
