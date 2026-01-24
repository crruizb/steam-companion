import { useState } from "react";
import { useAchievements } from "../hooks/useAchievements";
import {
  fillMissingDays,
  generateMonthLabels,
  getColor,
  groupByWeeks,
} from "../util";

export default function AchievementsHeatmap() {
  const { data: achievementsHeatmap } = useAchievements();
  const keys = Object.keys(achievementsHeatmap?.achievementsPerDate || {});
  const lastKey = keys[keys.length - 1];
  const [selectedYear, setSelectedYear] = useState<number | null>(null);

  // Use lastKey if selectedYear hasn't been set yet
  const activeYear = selectedYear ?? (lastKey ? Number(lastKey) : null);

  const days = activeYear
    ? achievementsHeatmap?.achievementsPerDate[activeYear]
    : undefined;

  if (!activeYear || !days || days.length === 0) {
    return <div>No achievements data available.</div>;
  }

  const normalized = fillMissingDays(days, activeYear);
  const weeks = groupByWeeks(normalized);
  const monthLabels = generateMonthLabels(weeks);

  const nAchievements = normalized.reduce((total, day) => total + day.count, 0);

  return (
    <div className="flex overflow-x-auto md:overflow-x-visible mt-4">
      <div className="flex flex-col">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xl">
            {nAchievements} achievements in {activeYear}
          </span>
          <select
            value={activeYear}
            onChange={(e) => setSelectedYear(Number(e.target.value))}
            className="px-3 py-1 border rounded-md bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {keys.map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
        </div>
        <div className="mt-2 px-2 border p-2 rounded-lg bg-white shadow-xl">
          {/* Month labels */}
          <div className="relative mb-1 h-4">
            {monthLabels.map((m) => (
              <span
                key={m.weekIndex}
                className="absolute text-xs text-neutral-500"
                style={{
                  left: `${m.weekIndex * 16}px`, // 12px cell + 4px gap
                }}
              >
                {m.label}
              </span>
            ))}
          </div>
          {/* Heatmap */}
          <div className="grid grid-flow-col auto-cols-max gap-1">
            {weeks.map((week, i) => (
              <div key={i} className="grid grid-rows-7 gap-1">
                {week.map((day, j) => (
                  <div
                    key={j}
                    className={`group relative h-3 w-3 rounded-sm ${
                      day.unlockDate ? getColor(day.count) : "bg-transparent"
                    }`}
                  >
                    <div className="pointer-events-none absolute -top-7 left-1/2 -translate-x-1/2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white opacity-0 transition-opacity duration-75 group-hover:opacity-100 z-50">
                      {day.unlockDate
                        ? `${day.count} achievement${
                            day.count !== 1 ? "s" : ""
                          } on ${new Date(day.unlockDate).toLocaleDateString()}`
                        : "No data"}
                    </div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
