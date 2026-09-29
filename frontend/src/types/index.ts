export interface User {
  id: number | null;
  steamId: string;
  username: string;
  displayName: string | null;
  avatarUrl: string | null;
  profileUrl: string;
  ownedGames?: Game[] | null;
}

export interface Game {
  appId: number;
  playTimeForeverMinutes: number;
  name: string;
  imgUrl: string | null;
  /** ISO-8601; null if never played or not re-imported since this field was added */
  lastPlayedAt?: string | null;
}

export interface AchievementsPerDate {
  unlockDate: string;
  count: number;
}

export interface AchievementsHeatmap {
  achievementsPerDate: Record<number, AchievementsPerDate[]>;
}
