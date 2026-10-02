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
  /** From the achievements import; null until an import has covered the game, 0 when it has none */
  achievementsTotal?: number | null;
  achievementsUnlocked?: number | null;
}

export interface AchievementsPerDate {
  unlockDate: string;
  count: number;
}

export interface AchievementsHeatmap {
  achievementsPerDate: Record<number, AchievementsPerDate[]>;
}

export type AchievementsImportState = "IDLE" | "RUNNING" | "COMPLETED" | "FAILED";

export interface AchievementsImportStatus {
  state: AchievementsImportState;
  processedGames: number;
  totalGames: number;
  importedAchievements: number;
}

export interface GameAchievement {
  apiName: string;
  displayName: string;
  /** Null for hidden achievements that are still locked */
  description: string | null;
  /** Gray version while locked */
  iconUrl: string | null;
  unlocked: boolean;
  unlockTime: string | null;
  /** Share of all Steam players who unlocked it, 0..100 */
  globalPercent: number | null;
}

export interface GameAchievements {
  appId: number;
  /** False until an import has fetched names, icons and rarity from Steam */
  hasDetails: boolean;
  achievements: GameAchievement[];
}

export interface RareAchievement {
  appId: number;
  gameName: string;
  displayName: string;
  description: string | null;
  iconUrl: string | null;
  globalPercent: number;
  unlockTime: string | null;
}
