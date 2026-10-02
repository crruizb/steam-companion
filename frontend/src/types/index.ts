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

export interface YearGame {
  appId: number;
  name: string;
  achievements: number;
}

export interface PerfectedGame {
  appId: number;
  name: string;
  achievementsTotal: number;
  completedAt: string;
}

/** Per-game highlights of a year; day-by-day numbers come from the heatmap */
export interface YearReview {
  year: number;
  topGames: YearGame[];
  rarest: RareAchievement[];
  perfected: PerfectedGame[];
}

export interface Friend {
  steamId: string;
  displayName: string;
  avatarUrl: string | null;
  profileUrl: string | null;
  /** Null for friendships older than Steam started recording it */
  friendSince: string | null;
}

export interface SharedGame {
  appId: number;
  name: string;
  myMinutes: number;
  friendMinutes: number;
}

export interface LibraryComparison {
  friendSteamId: string;
  /** False when the friend's game details are private; the lists are empty then */
  friendLibraryPublic: boolean;
  myGameCount: number;
  friendGameCount: number;
  sharedGames: SharedGame[];
  /** The friend's most played games you don't own */
  friendOnlyTopGames: { appId: number; name: string; friendMinutes: number }[];
}

export interface AchievementComparisonRow {
  apiName: string;
  displayName: string;
  description: string | null;
  iconUrl: string | null;
  globalPercent: number | null;
  myUnlockTime: string | null;
  friendUnlockTime: string | null;
}

export interface AchievementComparison {
  appId: number;
  /** False when the friend's achievements for the game are private */
  friendAchievementsPublic: boolean;
  achievements: AchievementComparisonRow[];
}
