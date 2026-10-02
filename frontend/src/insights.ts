import type { Game } from "./types";

export type PlaytimeBucket = { label: string; count: number };

// Upper bounds in hours, exclusive; the last bucket takes everything above
const BUCKETS: { label: string; maxHours: number }[] = [
  { label: "Never", maxHours: 0 },
  { label: "<1h", maxHours: 1 },
  { label: "1–5h", maxHours: 5 },
  { label: "5–20h", maxHours: 20 },
  { label: "20–50h", maxHours: 50 },
  { label: "50–100h", maxHours: 100 },
  { label: "100h+", maxHours: Infinity },
];

export type PlaytimeInsights = {
  totalMinutes: number;
  playedCount: number;
  /** Median playtime of the games played at least once, 0 when none are */
  medianPlayedMinutes: number;
  /** The 10 most played games, most played first; never-played games are left out */
  topPlayed: Game[];
  /** Share of all playtime that went into topPlayed, 0..1 */
  topPlayedShare: number;
  /** How many games fall in each playtime range, in order */
  distribution: PlaytimeBucket[];
};

export function playtimeInsights(games: readonly Game[]): PlaytimeInsights {
  const played = games
    .filter((g) => g.playTimeForeverMinutes > 0)
    .sort((a, b) => b.playTimeForeverMinutes - a.playTimeForeverMinutes);
  const totalMinutes = played.reduce((total, g) => total + g.playTimeForeverMinutes, 0);
  const topPlayed = played.slice(0, 10);
  const topMinutes = topPlayed.reduce((total, g) => total + g.playTimeForeverMinutes, 0);

  const distribution = BUCKETS.map(({ label }) => ({ label, count: 0 }));
  for (const game of games) {
    const hours = game.playTimeForeverMinutes / 60;
    const index = hours === 0 ? 0 : BUCKETS.findIndex((b, i) => i > 0 && hours < b.maxHours);
    distribution[index].count++;
  }

  return {
    totalMinutes,
    playedCount: played.length,
    medianPlayedMinutes: median(played.map((g) => g.playTimeForeverMinutes)),
    topPlayed,
    topPlayedShare: totalMinutes === 0 ? 0 : topMinutes / totalMinutes,
    distribution,
  };
}

function median(values: number[]): number {
  if (values.length === 0) return 0;
  const sorted = [...values].sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 1 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
}

/** A game with achievement data from Steam, and at least one achievement to earn. */
export type GameProgress = Game & { achievementsTotal: number; achievementsUnlocked: number };

export type AchievementProgress = {
  /** Games with achievements that an import has covered */
  trackedCount: number;
  /** Mean completion of the games with at least one unlock (as Steam counts it), 0..1; null when none */
  averageCompletion: number | null;
  /** Games with every achievement unlocked, biggest first */
  perfect: GameProgress[];
  /** Started but unfinished games, closest to 100% first */
  closest: GameProgress[];
};

export const completion = (game: GameProgress) => game.achievementsUnlocked / game.achievementsTotal;

/** Share of Steam players with an achievement, e.g. "0.4%", "3.2%", "57%". */
export function formatRarity(percent: number): string {
  if (percent > 0 && percent < 0.1) return "<0.1%";
  return percent < 10 ? `${percent.toFixed(1)}%` : `${Math.round(percent)}%`;
}

export function achievementProgress(games: readonly Game[], closestLimit = 6): AchievementProgress {
  const tracked = games.filter((g): g is GameProgress => (g.achievementsTotal ?? 0) > 0);
  const started = tracked.filter((g) => g.achievementsUnlocked > 0);
  const perfect = started
    .filter((g) => g.achievementsUnlocked >= g.achievementsTotal)
    .sort((a, b) => b.achievementsTotal - a.achievementsTotal || a.name.localeCompare(b.name));
  const closest = started
    .filter((g) => g.achievementsUnlocked < g.achievementsTotal)
    // Ties go to the game with fewer achievements left
    .sort(
      (a, b) =>
        completion(b) - completion(a) ||
        a.achievementsTotal - a.achievementsUnlocked - (b.achievementsTotal - b.achievementsUnlocked),
    )
    .slice(0, closestLimit);

  return {
    trackedCount: tracked.length,
    averageCompletion:
      started.length === 0 ? null : started.reduce((total, g) => total + completion(g), 0) / started.length,
    perfect,
    closest,
  };
}
