import type { ReactNode } from "react";
import { useUserGames } from "../hooks/useGames";
import { playtimeInsights } from "../insights";
import { formatNumber, formatPlayTime } from "../util";
import ChartTooltip from "./ChartTooltip";
import Meter from "./Meter";

const cardClass = "rounded-2xl border border-line/60 bg-card p-5 sm:p-6";

const percent = (share: number) => `${Math.round(share * 100)}%`;
const hours = (minutes: number) => `${formatNumber(Math.round(minutes / 60))}h`;

function Figure({ label, value, children }: { label: string; value: string; children?: ReactNode }) {
  return (
    <div className="min-w-0">
      <p className="text-sm text-muted">{label}</p>
      <p className="mt-0.5 text-2xl font-semibold text-heading">{value}</p>
      {children}
    </div>
  );
}

export default function PlaytimeInsights() {
  const { data: user } = useUserGames();
  const games = user?.ownedGames;
  if (!games?.length) return null;

  const insights = playtimeInsights(games);
  const playedShare = insights.playedCount / games.length;
  const topMinutes = insights.topPlayed[0]?.playTimeForeverMinutes ?? 0;
  const maxBucket = Math.max(...insights.distribution.map((b) => b.count));

  return (
    <section className={cardClass}>
      <h2 className="text-lg font-semibold text-heading">Playtime insights</h2>
      <p className="text-sm text-muted">{hours(insights.totalMinutes)} across your whole library</p>

      <div className="mt-5 grid gap-5 sm:grid-cols-3">
        <Figure label="Library played" value={percent(playedShare)}>
          <Meter value={playedShare} label="Share of games played" className="mt-2 h-2" />
          <p className="mt-1.5 text-xs text-muted">
            {formatNumber(insights.playedCount)} of {formatNumber(games.length)} games started
          </p>
        </Figure>
        <Figure label="Typical game" value={formatPlayTime(Math.round(insights.medianPlayedMinutes))}>
          <p className="mt-1 text-xs text-muted">Median playtime of the games you started</p>
        </Figure>
        <Figure label="Top 10 share" value={percent(insights.topPlayedShare)}>
          <p className="mt-1 text-xs text-muted">of your hours went into your 10 most played games</p>
        </Figure>
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-2">
        <div>
          <h3 className="text-sm font-semibold text-heading">Most played</h3>
          {insights.topPlayed.length === 0 ? (
            <p className="mt-3 text-sm text-muted">You haven't played any of your games yet.</p>
          ) : (
            <ol className="mt-3 space-y-2.5">
              {insights.topPlayed.map((game) => {
                const share = game.playTimeForeverMinutes / insights.totalMinutes;
                return (
                  <li
                    key={game.appId}
                    tabIndex={0}
                    className="group relative grid grid-cols-[minmax(0,9rem)_1fr] items-center gap-3 rounded focus:outline-none focus-visible:ring-2 focus-visible:ring-accent sm:grid-cols-[minmax(0,12rem)_1fr]"
                  >
                    <span className="truncate text-sm text-fg" title={game.name}>
                      {game.name}
                    </span>
                    <span className="flex items-center gap-2">
                      {/* Length relative to the most played game; the value label rides the tip */}
                      <span
                        className="h-3 rounded-r bg-accent transition-opacity group-hover:opacity-80"
                        style={{ width: `calc((100% - 3.5rem) * ${game.playTimeForeverMinutes / topMinutes})` }}
                      />
                      <span className="shrink-0 text-xs tabular-nums text-muted">
                        {hours(game.playTimeForeverMinutes)}
                      </span>
                    </span>
                    <ChartTooltip className="-top-7 right-0">
                      <span className="font-semibold">{formatPlayTime(game.playTimeForeverMinutes)}</span>
                      <span className="opacity-80"> · {percent(share)} of all playtime</span>
                    </ChartTooltip>
                  </li>
                );
              })}
            </ol>
          )}
        </div>

        <div>
          <h3 className="text-sm font-semibold text-heading">Hours per game</h3>
          <p className="text-xs text-muted">How many games fall in each playtime range</p>
          {/* Plot area has a fixed height; the labels below sit outside it so nothing gets clipped */}
          <div className="mt-6 grid grid-cols-7 gap-1">
            {insights.distribution.map((bucket, i) => (
              <div
                key={bucket.label}
                tabIndex={0}
                className="group relative flex flex-col items-center rounded focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
              >
                <div className="flex h-36 w-full flex-col items-center justify-end border-b border-line">
                  <span className="mb-1 text-xs tabular-nums text-muted">{formatNumber(bucket.count)}</span>
                  <div
                    className="w-full max-w-6 rounded-t bg-accent transition-opacity group-hover:opacity-80"
                    style={{ height: maxBucket === 0 ? 0 : `calc((100% - 1.25rem) * ${bucket.count / maxBucket})` }}
                  />
                </div>
                <span className="mt-1.5 text-center text-[11px] leading-tight text-muted">{bucket.label}</span>
                {/* Edge columns align inward so the tooltip stays inside the card */}
                <ChartTooltip
                  className={`-top-7 ${
                    i === 0 ? "left-0" : i === insights.distribution.length - 1 ? "right-0" : "left-1/2 -translate-x-1/2"
                  }`}
                >
                  <span className="font-semibold">{formatNumber(bucket.count)}</span>
                  <span className="opacity-80">
                    {" "}
                    game{bucket.count !== 1 ? "s" : ""} {bucket.label === "Never" ? "never played" : `played ${bucket.label}`}
                  </span>
                </ChartTooltip>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
