import config from "../config";

export const importAchievementsFromUser = async () => {
  const response = await fetch(`${config.API_BASE_URL}/achievements/import`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to import achievements: ${response.status}`);
  }
};

export const achievementsFromUser = async () => {
  const response = await fetch(`${config.API_BASE_URL}/achievements`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch achievements: ${response.status}`);
  }

  const data = await response.json();
  return data;
};
