import { forwardRef } from "react";
import { formatRarity, type YearSummary } from "../insights";
import type { YearReview } from "../types";
import { formatNumber, parseLocalDate } from "../util";
import { CARD_HEIGHT, CARD_WIDTH } from "./yearReviewImage";

// The card is an image people share, so it keeps one fixed (dark) look whatever the app theme
const C = {
  bgFrom: "#171a21",
  bgTo: "#1b2838",
  glow: "#66c0f4",
  heading: "#ffffff",
  fg: "#c7d5e0",
  muted: "#8f98a0",
  line: "#2a475e",
  bar: "#4a9ad0",
  kicker: "#66c0f4",
};
const FONT = "ui-sans-serif, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif";
const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

// SVG text doesn't wrap or clip, so long names are shortened to fit their column
const truncate = (text: string, max: number) => (text.length > max ? `${text.slice(0, max - 1).trimEnd()}…` : text);

function Kicker({ x, y, children }: { x: number; y: number; children: string }) {
  return (
    <text x={x} y={y} fill={C.muted} fontSize={15} fontWeight={600} letterSpacing={1.5}>
      {children.toUpperCase()}
    </text>
  );
}

type Props = { name: string; year: number; summary: YearSummary; review: YearReview | undefined };

/** Steam Replay-style summary of a year, as a self-contained SVG so it can be saved as an image. */
const YearReviewCard = forwardRef<SVGSVGElement, Props>(function YearReviewCard({ name, year, summary, review }, ref) {
  const stats = [
    { label: "Active days", value: formatNumber(summary.activeDays) },
    { label: "Longest streak", value: `${summary.longestStreak} day${summary.longestStreak !== 1 ? "s" : ""}` },
    {
      label: summary.bestDay
        ? `Best day, ${parseLocalDate(summary.bestDay.unlockDate).toLocaleDateString("en-US", { month: "short", day: "numeric" })}`
        : "Best day",
      value: summary.bestDay ? formatNumber(summary.bestDay.count) : "–",
    },
    { label: "Busiest month", value: summary.busiestMonth === null ? "–" : MONTHS[summary.busiestMonth] },
  ];

  // Month columns: 12 slots across the left half, growing up from one baseline
  const chart = { x: 64, width: 540, baseline: 560, height: 96 };
  const slot = chart.width / 12;
  const maxMonth = Math.max(...summary.months, 1);

  const topGames = review?.topGames.slice(0, 3) ?? [];
  const rarest = review?.rarest[0];
  const perfected = review?.perfected ?? [];

  return (
    <svg
      ref={ref}
      xmlns="http://www.w3.org/2000/svg"
      width={CARD_WIDTH}
      height={CARD_HEIGHT}
      viewBox={`0 0 ${CARD_WIDTH} ${CARD_HEIGHT}`}
      fontFamily={FONT}
      role="img"
      aria-label={`${name}'s ${year} on Steam: ${summary.total} achievements unlocked`}
      className="h-auto w-full rounded-xl"
    >
      <defs>
        <linearGradient id="yr-bg" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor={C.bgFrom} />
          <stop offset="1" stopColor={C.bgTo} />
        </linearGradient>
        <radialGradient id="yr-glow" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0" stopColor={C.glow} stopOpacity={0.18} />
          <stop offset="1" stopColor={C.glow} stopOpacity={0} />
        </radialGradient>
      </defs>
      <rect width={CARD_WIDTH} height={CARD_HEIGHT} fill="url(#yr-bg)" />
      <circle cx={140} cy={120} r={320} fill="url(#yr-glow)" />

      <text x={64} y={78} fill={C.kicker} fontSize={16} fontWeight={700} letterSpacing={3}>
        YEAR IN REVIEW
      </text>
      <text x={64} y={126} fill={C.heading} fontSize={40} fontWeight={700}>
        {truncate(name, 22)}'s {year} on Steam
      </text>

      <text x={64} y={262} fill={C.heading} fontSize={112} fontWeight={700}>
        {formatNumber(summary.total)}
      </text>
      <text x={64} y={302} fill={C.fg} fontSize={24}>
        achievement{summary.total !== 1 ? "s" : ""} unlocked
      </text>

      {stats.map((stat, i) => (
        <g key={stat.label} transform={`translate(${64 + i * 150}, 0)`}>
          <text y={380} fill={C.heading} fontSize={32} fontWeight={700}>
            {stat.value}
          </text>
          <text y={406} fill={C.muted} fontSize={14}>
            {stat.label}
          </text>
        </g>
      ))}

      <line x1={chart.x} x2={chart.x + chart.width} y1={chart.baseline} y2={chart.baseline} stroke={C.line} strokeWidth={1} />
      {summary.months.map((count, i) => {
        const height = (count / maxMonth) * chart.height;
        const cx = chart.x + slot * i + slot / 2;
        return (
          <g key={i}>
            {count > 0 && (
              // 4px rounded data end, square at the baseline
              <path
                d={`M${cx - 12},${chart.baseline} v${-Math.max(height - 4, 0)} q0,-4 4,-4 h16 q4,0 4,4 v${Math.max(height - 4, 0)} z`}
                fill={C.bar}
              />
            )}
            {i === summary.busiestMonth && (
              <text x={cx} y={chart.baseline - height - 8} fill={C.fg} fontSize={14} fontWeight={600} textAnchor="middle">
                {formatNumber(count)}
              </text>
            )}
            <text x={cx} y={chart.baseline + 22} fill={C.muted} fontSize={13} textAnchor="middle">
              {MONTHS[i].charAt(0)}
            </text>
          </g>
        );
      })}

      <line x1={664} x2={664} y1={176} y2={582} stroke={C.line} strokeWidth={1} />

      <Kicker x={704} y={196}>Most achievements</Kicker>
      {topGames.length === 0 ? (
        <text x={704} y={234} fill={C.muted} fontSize={20}>
          –
        </text>
      ) : (
        topGames.map((game, i) => (
          <g key={game.appId}>
            <text x={704} y={234 + i * 38} fill={C.heading} fontSize={21}>
              {truncate(game.name, 30)}
            </text>
            <text x={1136} y={234 + i * 38} fill={C.fg} fontSize={21} fontWeight={600} textAnchor="end">
              {formatNumber(game.achievements)}
            </text>
          </g>
        ))
      )}

      <Kicker x={704} y={374}>Rarest unlock</Kicker>
      {rarest ? (
        <>
          <text x={704} y={410} fill={C.heading} fontSize={21}>
            {truncate(rarest.displayName, 36)}
          </text>
          <text x={704} y={438} fill={C.muted} fontSize={16}>
            {truncate(rarest.gameName, 30)} · {formatRarity(rarest.globalPercent)} of players
          </text>
        </>
      ) : (
        <text x={704} y={410} fill={C.muted} fontSize={20}>
          –
        </text>
      )}

      <Kicker x={704} y={502}>Games perfected</Kicker>
      <text x={704} y={546} fill={C.heading} fontSize={32} fontWeight={700}>
        {perfected.length}
      </text>
      {perfected.length > 0 && (
        <text x={750} y={544} fill={C.muted} fontSize={16}>
          {truncate(perfected.map((g) => g.name).join(", "), 44)}
        </text>
      )}

      <text x={1136} y={600} fill={C.muted} fontSize={14} textAnchor="end">
        Steam Companion
      </text>
    </svg>
  );
});

export default YearReviewCard;
