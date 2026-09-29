import { describe, expect, it } from "vitest";
import {
  fillMissingDays,
  generateMonthLabels,
  getColor,
  formatNumber,
  formatPlayTime,
  groupByWeeks,
  longestStreak,
  parseLocalDate,
  shuffle,
} from "./util";

describe("test setup", () => {
  it("runs in a timezone west of UTC", () => {
    // Guards the date tests below: they only catch UTC parsing bugs west of UTC
    expect(new Date(2025, 0, 1).getTimezoneOffset()).toBeGreaterThan(0);
  });
});

describe("parseLocalDate", () => {
  it("returns local midnight of the given day", () => {
    const date = parseLocalDate("2025-03-01");
    expect([date.getFullYear(), date.getMonth(), date.getDate()]).toEqual([2025, 2, 1]);
    expect([date.getHours(), date.getMinutes()]).toEqual([0, 0]);
  });
});

describe("fillMissingDays", () => {
  it("fills every day of the year, keeping the known counts", () => {
    const days = fillMissingDays([{ unlockDate: "2025-03-01", count: 3 }], 2025);

    expect(days).toHaveLength(365);
    expect(days[0]).toEqual({ unlockDate: "2025-01-01", count: 0 });
    expect(days.at(-1)).toEqual({ unlockDate: "2025-12-31", count: 0 });
    expect(days.find((d) => d.unlockDate === "2025-03-01")?.count).toBe(3);
  });

  it("handles leap years", () => {
    expect(fillMissingDays([], 2024)).toHaveLength(366);
  });

  it("spans from the first to the last day when no year is given", () => {
    const days = fillMissingDays([
      { unlockDate: "2025-01-30", count: 1 },
      { unlockDate: "2025-02-02", count: 2 },
    ]);

    expect(days.map((d) => d.unlockDate)).toEqual([
      "2025-01-30",
      "2025-01-31",
      "2025-02-01",
      "2025-02-02",
    ]);
  });
});

describe("groupByWeeks", () => {
  const weeks = groupByWeeks(fillMissingDays([], 2025));

  it("starts the first week on the right weekday", () => {
    // 2025-01-01 is a Wednesday: Sunday to Tuesday are padding
    expect(weeks[0].map((d) => d.unlockDate)).toEqual([
      "",
      "",
      "",
      "2025-01-01",
      "2025-01-02",
      "2025-01-03",
      "2025-01-04",
    ]);
  });

  it("puts each day in its weekday row", () => {
    const week = weeks.find((w) => w.some((d) => d.unlockDate === "2025-03-01"))!;
    // 2025-03-01 is a Saturday (row 6)
    expect(week.findIndex((d) => d.unlockDate === "2025-03-01")).toBe(6);
  });

  it("pads the last week to 7 days", () => {
    expect(weeks.every((w) => w.length === 7)).toBe(true);
  });
});

describe("generateMonthLabels", () => {
  it("labels each month once, in order", () => {
    const labels = generateMonthLabels(groupByWeeks(fillMissingDays([], 2025)));
    expect(labels.map((l) => l.label)).toEqual([
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    ]);
  });
});

describe("getColor", () => {
  it("gets brighter as the count grows", () => {
    expect([0, 1, 3, 5, 6].map(getColor)).toEqual([
      "bg-heat-0",
      "bg-heat-1",
      "bg-heat-2",
      "bg-heat-3",
      "bg-heat-4",
    ]);
  });
});

describe("shuffle", () => {
  it("returns the same items without changing the input", () => {
    const input = [1, 2, 3, 4, 5];
    const result = shuffle(input);

    expect(input).toEqual([1, 2, 3, 4, 5]);
    expect(result).not.toBe(input);
    expect([...result].sort()).toEqual(input);
  });
});

describe("formatPlayTime", () => {
  it("shows hours and minutes, dropping zero parts", () => {
    expect([0, 45, 60, 135].map(formatPlayTime)).toEqual(["0m", "45m", "1h", "2h 15m"]);
  });
});

describe("formatNumber", () => {
  it("groups small numbers and compacts large ones", () => {
    expect([7, 1284, 9999, 12_900, 4_200_000].map(formatNumber)).toEqual([
      "7",
      "1,284",
      "9,999",
      "12.9K",
      "4.2M",
    ]);
  });
});

describe("longestStreak", () => {
  it("counts the longest run of consecutive active days", () => {
    const days = [1, 2, 0, 1, 1, 1, 0, 3].map((count, i) => ({
      unlockDate: `2025-01-0${i + 1}`,
      count,
    }));
    expect(longestStreak(days)).toBe(3);
  });

  it("is 0 without activity", () => {
    expect(longestStreak(fillMissingDays([], 2025))).toBe(0);
  });
});
