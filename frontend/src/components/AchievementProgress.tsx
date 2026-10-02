import { useState, type ReactNode } from "react";
import { Gem, Medal, Target, Trophy } from "lucide-react";
import { useRarestAchievements } from "../hooks/useAchievements";
import { useUserGames } from "../hooks/useGames";
import { achievementProgress, completion, formatRarity, type GameProgress } from "../insights";
import { formatNumber } from "../util";
import AchievementIcon from "./AchievementIcon";
import { primaryButton } from "./buttonStyles";
import GameAchievementsModal from "./GameAchievementsModal";
import ImportAchievementsButton from "./ImportAchievementsButton";
import Meter from "./Meter";
import { SteamImage } from "./SteamImage";

const cardClass = "rounded-2xl border border-line/60 bg-card p-5 sm:p-6";
const PERFECT_SHOWN = 8;

const percent = (share: number) => `${Math.floor(share * 100)}%`;

function GameThumb({ game }: { game: GameProgress }) {
  return <SteamImage appId={game.appId} alt="" className="h-12 w-8 shrink-0 rounded object-cover" />;
}

type SelectedGame = { appId: number; name: string };

/** A list row that opens the game's achievements. */
function GameButton({
  game,
  onSelect,
  children,
}: {
  game: SelectedGame;
  onSelect: (game: SelectedGame) => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={() => onSelect(game)}
      aria-label={`${game.name}: show achievements`}
      className="-mx-2 flex w-[calc(100%+1rem)] min-w-0 items-center gap-3 rounded-lg px-2 py-1 text-left transition-colors hover:bg-card-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
    >
      {children}
    </button>
  );
}

function RarestUnlocks({ onSelect }: { onSelect: (game: SelectedGame) => void }) {
  const { data: rarest, isLoading } = useRarestAchievements();
  if (isLoading) return null;
  if (!rarest?.length) {
    return <p className="mt-3 text-sm text-muted">Import achievements again to see how rare yours are.</p>;
  }
  return (
    <ul className="mt-2 space-y-1">
      {rarest.map((a) => (
        <li key={`${a.appId}-${a.displayName}`}>
          <GameButton game={{ appId: a.appId, name: a.gameName }} onSelect={onSelect}>
            <AchievementIcon src={a.iconUrl} />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm text-fg" title={a.displayName}>
                {a.displayName}
              </p>
              <p className="truncate text-xs text-muted">{a.gameName}</p>
            </div>
            <p className="shrink-0 text-right text-xs text-muted">
              <span className="block text-sm font-semibold text-heading">{formatRarity(a.globalPercent)}</span>
              of players
            </p>
          </GameButton>
        </li>
      ))}
    </ul>
  );
}

function EmptyState() {
  return (
    <section className={`${cardClass} flex flex-col items-center py-10 text-center`}>
      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-accent/15 text-accent">
        <Target className="h-6 w-6" />
      </div>
      <h2 className="mt-4 text-lg font-semibold text-heading">No achievement progress yet</h2>
      <p className="mt-1 max-w-sm text-sm text-muted">
        Import your achievements to see your perfect games and the ones you're closest to completing.
      </p>
      <ImportAchievementsButton className={`${primaryButton} mt-5`} />
    </section>
  );
}

export default function AchievementProgress() {
  const { data: user } = useUserGames();
  const [selected, setSelected] = useState<SelectedGame | null>(null);
  const games = user?.ownedGames;
  if (!games?.length) return null;

  const progress = achievementProgress(games);
  if (progress.trackedCount === 0) return <EmptyState />;

  const perfectShown = progress.perfect.slice(0, PERFECT_SHOWN);

  return (
    <section className={cardClass}>
      <div className="flex flex-wrap items-start justify-between gap-x-8 gap-y-4">
        <div>
          <h2 className="text-lg font-semibold text-heading">Achievement progress</h2>
          <p className="text-sm text-muted">
            Across {formatNumber(progress.trackedCount)} game{progress.trackedCount !== 1 ? "s" : ""} with achievements
          </p>
        </div>
        <dl className="flex gap-8">
          <div>
            <dt className="text-sm text-muted">Perfect games</dt>
            <dd className="text-2xl font-semibold text-heading">{formatNumber(progress.perfect.length)}</dd>
          </div>
          <div>
            <dt className="text-sm text-muted">Average completion</dt>
            <dd className="text-2xl font-semibold text-heading">
              {progress.averageCompletion === null ? "–" : percent(progress.averageCompletion)}
            </dd>
          </div>
        </dl>
      </div>

      <div className="mt-6 grid gap-8 md:grid-cols-2 xl:grid-cols-3">
        <div>
          <h3 className="flex items-center gap-1.5 text-sm font-semibold text-heading">
            <Target className="h-4 w-4 text-accent" />
            Closest to 100%
          </h3>
          {progress.closest.length === 0 ? (
            <p className="mt-3 text-sm text-muted">No games in progress. Unlock an achievement to start one.</p>
          ) : (
            <ul className="mt-2 space-y-1">
              {progress.closest.map((game) => {
                const left = game.achievementsTotal - game.achievementsUnlocked;
                return (
                  <li key={game.appId}>
                    <GameButton game={game} onSelect={setSelected}>
                      <GameThumb game={game} />
                      <div className="min-w-0 flex-1">
                        <div className="flex items-baseline justify-between gap-3">
                          <span className="truncate text-sm text-fg" title={game.name}>
                            {game.name}
                          </span>
                          <span className="shrink-0 text-sm font-semibold tabular-nums text-heading">
                            {percent(completion(game))}
                          </span>
                        </div>
                        <Meter value={completion(game)} label={`${game.name} completion`} className="mt-1.5 h-1.5" />
                        <p className="mt-1 text-xs tabular-nums text-muted">
                          {game.achievementsUnlocked}/{game.achievementsTotal} · {left} left
                        </p>
                      </div>
                    </GameButton>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <div>
          <h3 className="flex items-center gap-1.5 text-sm font-semibold text-heading">
            <Medal className="h-4 w-4 text-accent" />
            Perfect games
          </h3>
          {perfectShown.length === 0 ? (
            <p className="mt-3 text-sm text-muted">
              No perfect games yet. The games on the left are the quickest way to your first one.
            </p>
          ) : (
            <>
              <ul className="mt-2 space-y-1">
                {perfectShown.map((game) => (
                  <li key={game.appId}>
                    <GameButton game={game} onSelect={setSelected}>
                      <GameThumb game={game} />
                      <div className="min-w-0">
                        <p className="truncate text-sm text-fg" title={game.name}>
                          {game.name}
                        </p>
                        <p className="flex items-center gap-1 text-xs text-muted">
                          <Trophy className="h-3 w-3" />
                          {game.achievementsTotal} achievements
                        </p>
                      </div>
                    </GameButton>
                  </li>
                ))}
              </ul>
              {progress.perfect.length > PERFECT_SHOWN && (
                <p className="mt-3 text-xs text-muted">
                  and {progress.perfect.length - PERFECT_SHOWN} more
                </p>
              )}
            </>
          )}
        </div>

        <div className="md:col-span-2 xl:col-span-1">
          <h3 className="flex items-center gap-1.5 text-sm font-semibold text-heading">
            <Gem className="h-4 w-4 text-accent" />
            Rarest unlocks
          </h3>
          <RarestUnlocks onSelect={setSelected} />
        </div>
      </div>

      <GameAchievementsModal game={selected} onClose={() => setSelected(null)} />
    </section>
  );
}
