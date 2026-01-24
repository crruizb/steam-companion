import type { AchievementsPerDate } from "./types";

export function getColor(count: number): string {
  if (count === 0) return "bg-neutral-200";
  if (count < 2) return "bg-green-200";
  if (count < 4) return "bg-green-300";
  if (count < 6) return "bg-green-500";
  return "bg-green-700";
}

export function fillMissingDays(
  days: AchievementsPerDate[],
  year?: number
): AchievementsPerDate[] {
  if (days.length === 0 && !year) return [];

  const result: AchievementsPerDate[] = [];

  // If year is provided, use the full year range
  let start: Date;
  let end: Date;

  if (year) {
    start = new Date(year, 0, 1); // January 1 of the year
    end = new Date(year, 11, 31); // December 31 of the year
  } else {
    start = new Date(days[0].unlockDate);
    end = new Date(days[days.length - 1].unlockDate);
  }

  const map = new Map(days.map((d) => [d.unlockDate, d.count]));

  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const date = d.toISOString().slice(0, 10);
    result.push({ unlockDate: date, count: map.get(date) ?? 0 });
  }

  return result;
}

export function groupByWeeks(
  days: AchievementsPerDate[]
): AchievementsPerDate[][] {
  if (days.length === 0) return [];

  const weeks: AchievementsPerDate[][] = [];
  const emptyDay = { unlockDate: "", count: 0 };

  // Pad beginning based on first day's day of week
  const firstDayOfWeek = new Date(days[0].unlockDate).getDay();
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

export type MonthLabel = {
  label: string;
  weekIndex: number;
};

export function generateMonthLabels(
  weeks: AchievementsPerDate[][]
): MonthLabel[] {
  const labels: MonthLabel[] = [];
  let lastMonth = -1;

  weeks.forEach((week, i) => {
    const firstValidDay = week.find((d) => d.unlockDate);
    if (!firstValidDay) return;

    const month = new Date(firstValidDay.unlockDate).getMonth();

    if (month !== lastMonth) {
      labels.push({
        label: new Date(firstValidDay.unlockDate).toLocaleString("en-US", {
          month: "short",
        }),
        weekIndex: i,
      });
      lastMonth = month;
    }
  });

  return labels;
}
