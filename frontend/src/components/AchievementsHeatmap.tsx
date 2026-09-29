import { useLayoutEffect, useRef, useState } from "react";
import { Flame, Trophy } from "lucide-react";
import { useAchievements, useImportAchievements } from "../hooks/useAchievements";
import {
  fillMissingDays,
  generateMonthLabels,
  getColor,
  groupByWeeks,
  longestStreak,
  parseLocalDate,
} from "../util";
import { primaryButton } from "./buttonStyles";

const WEEKDAY_LABELS = ["", "Mon", "", "Wed", "", "Fri", ""]; // Rows start on Sunday

const cardClass = "rounded-2xl border border-line/60 bg-card p-5 sm:p-6";

function EmptyState() {
  const { mutate: importAchievements, isPending } = useImportAchievements();

  return (
    <section className={`${cardClass} flex flex-col items-center py-10 text-center`}>
      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-accent/15 text-accent">
        <Trophy className="h-6 w-6" />
      </div>
      <h2 className="mt-4 text-lg font-semibold text-heading">No achievement activity yet</h2>
      <p className="mt-1 max-w-sm text-sm text-muted">
        Import your achievements from Steam to see which days you unlocked them.
      </p>
      <button
        type="button"
        className={`${primaryButton} mt-5`}
        onClick={() => importAchievements()}
        disabled={isPending}
      >
        <Trophy className="h-4 w-4" />
        Import achievements
      </button>
    </section>
  );
}

export default function AchievementsHeatmap() {
  const { data: achievementsHeatmap, isLoading } = useAchievements();
  const years = Object.keys(achievementsHeatmap?.achievementsPerDate || {});
  const [selectedYear, setSelectedYear] = useState<number | null>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  // Default to the most recent year with data
  const lastYear = years[years.length - 1];
  const activeYear = selectedYear ?? (lastYear ? Number(lastYear) : null);
  const days = activeYear ? achievementsHeatmap?.achievementsPerDate[activeYear] : undefined;

  // On narrow screens the heatmap scrolls sideways: start at the most recent months
  useLayoutEffect(() => {
    const el = scrollRef.current;
    if (el) el.scrollLeft = el.scrollWidth;
  }, [activeYear, days]);

  if (isLoading) {
    return <section className={`${cardClass} h-64 animate-pulse`} aria-label="Loading achievement activity" />;
  }
  if (!activeYear || !days || days.length === 0) {
    return <EmptyState />;
  }

  const normalized = fillMissingDays(days, activeYear);
  const weeks = groupByWeeks(normalized);
  const monthLabels = generateMonthLabels(weeks);
  const total = normalized.reduce((sum, day) => sum + day.count, 0);
  const streak = longestStreak(normalized);
  const bestDay = normalized.reduce((best, day) => (day.count > best.count ? day : best));

  return (
    <section className={cardClass}>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-heading">Achievement activity</h2>
          <p className="text-sm text-muted">
            {total} achievement{total !== 1 ? "s" : ""} unlocked in {activeYear}
          </p>
        </div>
        <select
          value={activeYear}
          onChange={(e) => setSelectedYear(Number(e.target.value))}
          aria-label="Year"
          className="rounded-lg border border-line bg-card px-3 py-1.5 text-sm text-fg focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
        >
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
      </div>

      {/* Cells grow with the card; below min-w it scrolls sideways. Top padding leaves room for the first row's tooltips */}
      <div ref={scrollRef} className="mt-2 overflow-x-auto pb-1 pt-3">
        <div
          className="grid min-w-[680px] gap-[3px]"
          style={{ gridTemplateColumns: `auto repeat(${weeks.length}, minmax(0, 1fr))` }}
        >
          {monthLabels.map((m) => (
            <span
              key={m.weekIndex}
              className="whitespace-nowrap pb-1 text-xs text-muted"
              style={{ gridRow: 1, gridColumn: `${m.weekIndex + 2} / span 4` }}
            >
              {m.label}
            </span>
          ))}

          {WEEKDAY_LABELS.map((label, i) => (
            <span
              key={label || i}
              className="self-center pr-1 text-[10px] leading-none text-muted"
              style={{ gridRow: i + 2, gridColumn: 1 }}
            >
              {label}
            </span>
          ))}

          {weeks.map((week, w) =>
            week.map((day, d) =>
              day.unlockDate ? (
                <div
                  key={day.unlockDate}
                  className={`group relative aspect-square rounded-[3px] ${getColor(day.count)}`}
                  style={{ gridRow: d + 2, gridColumn: w + 2 }}
                >
                  {/* Not rendered until hover, so hidden tooltips don't widen the scroll area.
                      Edge columns align inward so the tooltip stays inside the card */}
                  <div
                    className={`pointer-events-none absolute -top-8 z-20 hidden whitespace-nowrap rounded-md bg-heading px-2 py-1 text-xs font-medium text-page shadow-lg group-hover:block ${
                      w < 6 ? "left-0" : w >= weeks.length - 6 ? "right-0" : "left-1/2 -translate-x-1/2"
                    }`}
                  >
                    {day.count} achievement{day.count !== 1 ? "s" : ""} on{" "}
                    {parseLocalDate(day.unlockDate).toLocaleDateString()}
                  </div>
                </div>
              ) : null
            )
          )}
        </div>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm">
        <p className="flex flex-wrap items-center gap-x-4 gap-y-1 text-muted">
          <span className="inline-flex items-center gap-1.5">
            <Flame className="h-4 w-4 text-accent" />
            Longest streak <span className="font-semibold text-heading">{streak} day{streak !== 1 ? "s" : ""}</span>
          </span>
          {bestDay.count > 0 && (
            <span>
              Best day <span className="font-semibold text-heading">{bestDay.count}</span> on{" "}
              {parseLocalDate(bestDay.unlockDate).toLocaleDateString(undefined, { month: "short", day: "numeric" })}
            </span>
          )}
        </p>

        {/* Scale legend: the colors are a single-hue scale, so this explains them */}
        <div className="flex items-center gap-1.5 text-xs text-muted" aria-hidden="true">
          Less
          {[0, 1, 3, 5, 6].map((count) => (
            <span key={count} className={`h-3 w-3 rounded-sm ${getColor(count)}`} />
          ))}
          More
        </div>
      </div>
    </section>
  );
}
