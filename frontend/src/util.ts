import type { AchievementsPerDate, Game } from "./types";

export function getColor(count: number): string {
  // Brighter means more achievements (heat-* tokens in index.css)
  if (count === 0) return "bg-heat-0";
  if (count < 2) return "bg-heat-1";
  if (count < 4) return "bg-heat-2";
  if (count < 6) return "bg-heat-3";
  return "bg-heat-4";
}

export function parseLocalDate(isoDate: string): Date {
  const [year, month, day] = isoDate.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function toLocalISODate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function fillMissingDays(
  days: AchievementsPerDate[],
  year?: number,
): AchievementsPerDate[] {
  if (days.length === 0 && !year) return [];

  const result: AchievementsPerDate[] = [];

  // If year is provided, use the full year range
  let start: Date;
  let end: Date;

  if (year) {
    start = new Date(year, 0, 1);
    end = new Date(year, 11, 31);
  } else {
    start = parseLocalDate(days[0].unlockDate);
    end = parseLocalDate(days[days.length - 1].unlockDate);
  }

  const map = new Map(days.map((d) => [d.unlockDate, d.count]));

  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const date = toLocalISODate(d);
    result.push({ unlockDate: date, count: map.get(date) ?? 0 });
  }

  return result;
}

export function groupByWeeks(
  days: AchievementsPerDate[],
): AchievementsPerDate[][] {
  if (days.length === 0) return [];

  const weeks: AchievementsPerDate[][] = [];
  const emptyDay = { unlockDate: "", count: 0 };

  // Pad beginning based on first day's day of week
  const firstDayOfWeek = parseLocalDate(days[0].unlockDate).getDay();
  let currentWeek: AchievementsPerDate[] = Array(firstDayOfWeek).fill(emptyDay);

  days.forEach((day) => {
    currentWeek.push(day);

    if (currentWeek.length === 7) {
      weeks.push(currentWeek);
      currentWeek = [];
    }
  });

  // Pad and add last week if needed
  if (currentWeek.length > 0) {
    weeks.push([
      ...currentWeek,
      ...Array(7 - currentWeek.length).fill(emptyDay),
    ]);
  }

  return weeks;
}

// Fisher-Yates shuffle. Returns a new array and leaves the input untouched.
export function shuffle<T>(items: readonly T[]): T[] {
  const result = [...items];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}

export type MonthLabel = {
  label: string;
  weekIndex: number;
};

export function generateMonthLabels(
  weeks: AchievementsPerDate[][],
): MonthLabel[] {
  const labels: MonthLabel[] = [];
  let lastMonth = -1;

  weeks.forEach((week, i) => {
    const firstValidDay = week.find((d) => d.unlockDate);
    if (!firstValidDay) return;

    const date = parseLocalDate(firstValidDay.unlockDate);
    const month = date.getMonth();

    if (month !== lastMonth) {
      labels.push({
        label: date.toLocaleString("en-US", {
          month: "short",
        }),
        weekIndex: i,
      });
      lastMonth = month;
    }
  });

  return labels;
}

export function formatPlayTime(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours === 0) return `${mins}m`;
  if (mins === 0) return `${hours}h`;
  return `${hours}h ${mins}m`;
}

const compactFormat = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});
const fullFormat = new Intl.NumberFormat("en");

/** 1,284 up to 9,999, then compact: 12.9K, 4.2M. */
export function formatNumber(value: number): string {
  return value < 10_000 ? fullFormat.format(value) : compactFormat.format(value);
}

/** Longest run of consecutive days with at least one achievement. Expects every day present (see fillMissingDays). */
export function longestStreak(days: AchievementsPerDate[]): number {
  let longest = 0;
  let current = 0;
  for (const day of days) {
    current = day.count > 0 ? current + 1 : 0;
    longest = Math.max(longest, current);
  }
  return longest;
}

export type LibrarySort = "most-played" | "recent" | "name" | "shuffle";
export type LibraryFilter = "all" | "played" | "never-played";

// Lowercase and strip accents, so "pokemon" finds "Pokémon"
const normalizeText = (text: string) =>
  text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();

const byName = (a: Game, b: Game) =>
  a.name.localeCompare(b.name, undefined, { numeric: true, sensitivity: "base" });

const lastPlayedTime = (game: Game) => (game.lastPlayedAt ? Date.parse(game.lastPlayedAt) : 0);

/**
 * Searches, filters and sorts a library into a new array.
 * "shuffle" keeps the order of the given games, so pass them already shuffled.
 */
export function filterAndSortGames(
  games: readonly Game[],
  { query, filter, sort }: { query: string; filter: LibraryFilter; sort: LibrarySort },
): Game[] {
  const search = normalizeText(query.trim());
  const result = games.filter(
    (game) =>
      (filter === "all" || (filter === "played") === game.playTimeForeverMinutes > 0) &&
      (search === "" || normalizeText(game.name).includes(search)),
  );

  switch (sort) {
    case "most-played":
      return result.sort((a, b) => b.playTimeForeverMinutes - a.playTimeForeverMinutes || byName(a, b));
    case "recent":
      // Never played (no date) goes last
      return result.sort((a, b) => lastPlayedTime(b) - lastPlayedTime(a) || byName(a, b));
    case "name":
      return result.sort(byName);
    case "shuffle":
      return result;
  }
}
