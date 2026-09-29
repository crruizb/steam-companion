import { apiFetch } from "./api";

export const importAchievementsFromUser = async () => {
  const response = await apiFetch("/achievements/import", { method: "POST" });

  if (!response.ok) {
    throw new Error(`Failed to import achievements: ${response.status}`);
  }
};

export const achievementsFromUser = async () => {
  const response = await apiFetch("/achievements", { method: "GET" });

  if (!response.ok) {
    throw new Error(`Failed to fetch achievements: ${response.status}`);
  }

  const data = await response.json();
  return data;
};
