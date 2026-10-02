import { describe, expect, it } from "vitest";
import type { Game } from "./types";
import { achievementProgress, formatRarity, playtimeInsights, yearSummary } from "./insights";

const game = (name: string, playTimeForeverMinutes: number, achievements?: [number, number]): Game => ({
  appId: name.length,
  name,
  playTimeForeverMinutes,
  imgUrl: null,
  achievementsUnlocked: achievements?.[0] ?? null,
  achievementsTotal: achievements?.[1] ?? null,
});

describe("playtimeInsights", () => {
  it("summarizes playtime and buckets every game by hours played", () => {
    const games = [
      game("Never", 0),
      game("Half hour", 30),
      game("One hour", 60),
      game("Twenty hours", 20 * 60),
      game("Two hundred hours", 200 * 60),
    ];

    const insights = playtimeInsights(games);

    expect(insights.totalMinutes).toBe(30 + 60 + 1200 + 12000);
    expect(insights.playedCount).toBe(4);
    expect(insights.medianPlayedMinutes).toBe((60 + 1200) / 2);
    expect(insights.topPlayed.map((g) => g.name)).toEqual([
      "Two hundred hours",
      "Twenty hours",
      "One hour",
      "Half hour",
    ]);
    expect(insights.topPlayedShare).toBe(1);
    // Bucket bounds are exclusive at the top: 1h goes in 1–5h, 20h in 20–50h
    expect(insights.distribution.map((b) => b.count)).toEqual([1, 1, 1, 0, 1, 0, 1]);
  });

  it("keeps only the 10 most played games and their share of all playtime", () => {
    const games = Array.from({ length: 12 }, (_, i) => game(`Game ${i}`, (i + 1) * 60));

    const insights = playtimeInsights(games);

    expect(insights.topPlayed).toHaveLength(10);
    expect(insights.topPlayed[0].name).toBe("Game 11");
    expect(insights.topPlayedShare).toBeCloseTo((78 - 3) / 78);
  });

  it("handles a library nobody has played", () => {
    const insights = playtimeInsights([game("Never", 0)]);

    expect(insights.medianPlayedMinutes).toBe(0);
    expect(insights.topPlayed).toEqual([]);
    expect(insights.topPlayedShare).toBe(0);
  });
});

describe("achievementProgress", () => {
  it("finds perfect games and the unfinished games closest to 100%", () => {
    const games = [
      game("Not imported", 10),
      game("No achievements", 10, [0, 0]),
      game("Not started", 10, [0, 50]),
      game("Perfect small", 10, [10, 10]),
      game("Perfect big", 10, [80, 80]),
      game("Nine of ten", 10, [9, 10]),
      game("Ninety of hundred", 10, [90, 100]),
      game("Half", 10, [5, 10]),
    ];

    const progress = achievementProgress(games);

    expect(progress.trackedCount).toBe(6);
    expect(progress.perfect.map((g) => g.name)).toEqual(["Perfect big", "Perfect small"]);
    // Same 90%: one achievement left beats ten left
    expect(progress.closest.map((g) => g.name)).toEqual(["Nine of ten", "Ninety of hundred", "Half"]);
    // Mean of 100%, 100%, 90%, 90%, 50%; "Not started" doesn't count, like on Steam
    expect(progress.averageCompletion).toBeCloseTo(0.86);
  });

  it("limits the closest list", () => {
    const games = Array.from({ length: 8 }, (_, i) => game(`Game ${i}`, 10, [i + 1, 10]));

    expect(achievementProgress(games, 3).closest.map((g) => g.name)).toEqual(["Game 7", "Game 6", "Game 5"]);
  });

  it("reports no average before any achievement data exists", () => {
    const progress = achievementProgress([game("Not imported", 10)]);

    expect(progress).toEqual({ trackedCount: 0, averageCompletion: null, perfect: [], closest: [] });
  });
});

describe("formatRarity", () => {
  it("keeps a decimal for rare achievements and rounds common ones", () => {
    expect(formatRarity(0.04)).toBe("<0.1%");
    expect(formatRarity(0.4)).toBe("0.4%");
    expect(formatRarity(3.25)).toBe("3.3%");
    expect(formatRarity(57.4)).toBe("57%");
    expect(formatRarity(100)).toBe("100%");
  });
});

describe("yearSummary", () => {
  it("summarizes a year's unlocks by day and month", () => {
    const summary = yearSummary(
      [
        { unlockDate: "2025-01-31", count: 2 },
        { unlockDate: "2025-02-01", count: 5 },
        { unlockDate: "2025-02-02", count: 1 },
        { unlockDate: "2025-12-31", count: 5 },
      ],
      2025,
    );

    expect(summary.total).toBe(13);
    expect(summary.activeDays).toBe(4);
    expect(summary.longestStreak).toBe(3);
    // A tie keeps the earlier day
    expect(summary.bestDay).toEqual({ unlockDate: "2025-02-01", count: 5 });
    expect(summary.months).toEqual([2, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5]);
    expect(summary.busiestMonth).toBe(1);
  });

  it("handles a year without unlocks", () => {
    const summary = yearSummary([], 2025);

    expect(summary.total).toBe(0);
    expect(summary.bestDay).toBeNull();
    expect(summary.busiestMonth).toBeNull();
  });
});
