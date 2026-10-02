import { useState } from "react";
import { ArrowLeft, Check, ExternalLink, Lock } from "lucide-react";
import { useAchievementComparison, useLibraryComparison } from "../hooks/useFriends";
import { formatRarity } from "../insights";
import { ApiError } from "../services/api";
import type { AchievementComparisonRow, Friend, SharedGame } from "../types";
import { formatNumber } from "../util";
import AchievementIcon from "./AchievementIcon";
import Meter from "./Meter";
import Modal from "./Modal";

const SHARED_SHOWN = 10;

const hours = (minutes: number) =>
  minutes < 60 ? `${minutes}m` : `${formatNumber(Math.round(minutes / 60))}h`;

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });

const errorMessage = (error: Error) =>
  // 400 and 404 carry the backend's reason, e.g. no games imported yet
  error instanceof ApiError && (error.status === 400 || error.status === 404)
    ? error.message
    : "Could not load this from Steam. Please try again later.";

function Spinner() {
  return (
    <div className="flex justify-center py-10">
      <span className="h-6 w-6 animate-spin rounded-full border-2 border-accent border-t-transparent" />
    </div>
  );
}

/** Legend for the two-series bars and meters: always shown, so color is never the only cue. */
function Legend({ friendName }: { friendName: string }) {
  return (
    <div className="flex items-center gap-4 text-xs text-muted">
      <span className="inline-flex items-center gap-1.5">
        <span className="h-2.5 w-2.5 rounded-sm bg-series-you" />
        You
      </span>
      <span className="inline-flex min-w-0 items-center gap-1.5">
        <span className="h-2.5 w-2.5 shrink-0 rounded-sm bg-series-friend" />
        <span className="truncate">{friendName}</span>
      </span>
    </div>
  );
}

function Figure({ label, value }: { label: string; value: string }) {
  return (
    <div className="min-w-0">
      <p className="truncate text-sm text-muted">{label}</p>
      <p className="text-2xl font-semibold text-heading">{value}</p>
    </div>
  );
}

function LibraryView({ friend, onSelectGame }: { friend: Friend; onSelectGame: (game: SharedGame) => void }) {
  const { data, isLoading, error } = useLibraryComparison(friend.steamId);
  const [showAll, setShowAll] = useState(false);

  if (isLoading) return <Spinner />;
  if (error || !data) return <p className="py-6 text-sm text-muted">{errorMessage(error ?? new Error())}</p>;
  if (!data.friendLibraryPublic) {
    return (
      <p className="py-6 text-sm text-muted">
        {friend.displayName}'s game details are private on Steam, so there's nothing to compare yet.
      </p>
    );
  }

  const shown = showAll ? data.sharedGames : data.sharedGames.slice(0, SHARED_SHOWN);
  const maxMinutes = Math.max(1, ...shown.flatMap((g) => [g.myMinutes, g.friendMinutes]));

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-3 gap-4">
        <Figure label="In common" value={formatNumber(data.sharedGames.length)} />
        <Figure label="Your games" value={formatNumber(data.myGameCount)} />
        <Figure label={`${friend.displayName}'s games`} value={formatNumber(data.friendGameCount)} />
      </div>

      <div>
        <div className="flex flex-wrap items-baseline justify-between gap-2">
          <h3 className="text-sm font-semibold text-heading">Hours in the games you share</h3>
          <Legend friendName={friend.displayName} />
        </div>
        {data.sharedGames.length === 0 ? (
          <p className="mt-3 text-sm text-muted">You don't own any of the same games yet.</p>
        ) : (
          <>
            <p className="text-xs text-muted">Pick a game to compare achievements</p>
            <ul className="mt-3 space-y-1">
              {shown.map((game) => (
                <li key={game.appId}>
                  <button
                    type="button"
                    onClick={() => onSelectGame(game)}
                    className="-mx-2 grid w-[calc(100%+1rem)] grid-cols-[minmax(0,8rem)_1fr] items-center gap-3 rounded-lg px-2 py-1.5 text-left transition-colors hover:bg-card-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-accent sm:grid-cols-[minmax(0,13rem)_1fr]"
                  >
                    <span className="truncate text-sm text-fg" title={game.name}>
                      {game.name}
                    </span>
                    {/* Two thin bars on one scale, 2px apart, each labeled at its tip */}
                    <span className="flex flex-col gap-0.5">
                      {[
                        { minutes: game.myMinutes, color: "bg-series-you", who: "You" },
                        { minutes: game.friendMinutes, color: "bg-series-friend", who: friend.displayName },
                      ].map(({ minutes, color, who }) => (
                        <span key={who} className="flex items-center gap-2" aria-label={`${who}: ${hours(minutes)}`}>
                          <span
                            className={`h-2 rounded-r ${color}`}
                            style={{ width: `calc((100% - 3rem) * ${minutes / maxMinutes})` }}
                          />
                          <span className="shrink-0 text-[11px] leading-none tabular-nums text-muted">{hours(minutes)}</span>
                        </span>
                      ))}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
            {data.sharedGames.length > SHARED_SHOWN && (
              <button
                type="button"
                onClick={() => setShowAll(!showAll)}
                className="mt-2 text-sm font-medium text-accent hover:underline"
              >
                {showAll ? "Show fewer" : `Show all ${data.sharedGames.length} games`}
              </button>
            )}
          </>
        )}
      </div>

      {data.friendOnlyTopGames.length > 0 && (
        <div>
          <h3 className="text-sm font-semibold text-heading">{friend.displayName} plays these, you don't own them</h3>
          <ul className="mt-2 divide-y divide-line/60">
            {data.friendOnlyTopGames.map((game) => (
              <li key={game.appId}>
                <a
                  href={`https://store.steampowered.com/app/${game.appId}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-between gap-3 py-2 text-sm text-fg hover:text-heading"
                >
                  <span className="truncate">{game.name}</span>
                  <span className="flex shrink-0 items-center gap-2 text-xs tabular-nums text-muted">
                    {hours(game.friendMinutes)}
                    <ExternalLink className="h-3.5 w-3.5" />
                  </span>
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

type AchievementFilter = "all" | "you-missing" | "they-missing";

function UnlockStatus({ who, color, time }: { who: string; color: string; time: string | null }) {
  return (
    <span className="inline-flex min-w-0 items-center gap-1.5">
      <span className={`h-2 w-2 shrink-0 rounded-full ${color}`} />
      <span className="truncate">{who}</span>
      {time ? (
        <span className="inline-flex shrink-0 items-center gap-0.5 text-fg">
          <Check className="h-3 w-3" />
          {formatDate(time)}
        </span>
      ) : (
        <span className="inline-flex shrink-0 items-center gap-0.5">
          <Lock className="h-3 w-3" />
          Locked
        </span>
      )}
    </span>
  );
}

function AchievementsView({ friend, game, onBack }: { friend: Friend; game: SharedGame; onBack: () => void }) {
  const { data, isLoading, error } = useAchievementComparison(friend.steamId, game.appId);
  const [filter, setFilter] = useState<AchievementFilter>("all");

  const rows = data?.achievements ?? [];
  const mine = rows.filter((a) => a.myUnlockTime).length;
  const theirs = rows.filter((a) => a.friendUnlockTime).length;
  const matches = (a: AchievementComparisonRow) =>
    filter === "all" ||
    (filter === "you-missing" ? !a.myUnlockTime && !!a.friendUnlockTime : !!a.myUnlockTime && !a.friendUnlockTime);
  const filters: { value: AchievementFilter; label: string }[] = [
    { value: "all", label: "All" },
    { value: "you-missing", label: "Only they have" },
    { value: "they-missing", label: "Only you have" },
  ];

  return (
    <div>
      <button
        type="button"
        onClick={onBack}
        className="mb-3 inline-flex items-center gap-1 text-sm text-accent hover:underline"
      >
        <ArrowLeft className="h-4 w-4" />
        All shared games
      </button>
      <h3 className="font-semibold text-heading">{game.name}</h3>

      {isLoading ? (
        <Spinner />
      ) : error || !data ? (
        <p className="py-6 text-sm text-muted">{errorMessage(error ?? new Error())}</p>
      ) : (
        <>
          <div className="mt-3 grid grid-cols-2 gap-4">
            {[
              { who: "You", count: mine, color: "bg-series-you" },
              { who: friend.displayName, count: theirs, color: "bg-series-friend" },
            ].map(({ who, count, color }) => (
              <div key={who} className="min-w-0">
                <p className="flex items-baseline justify-between gap-2 text-sm text-muted">
                  <span className="truncate">{who}</span>
                  <span className="shrink-0 tabular-nums">
                    <span className="font-semibold text-heading">{count}</span>/{rows.length}
                  </span>
                </p>
                <Meter value={rows.length ? count / rows.length : 0} label={`${who}: achievements unlocked`} className="mt-1.5 h-1.5" fillClassName={color} />
              </div>
            ))}
          </div>

          {!data.friendAchievementsPublic && (
            <p className="mt-4 rounded-lg border border-line/60 px-3 py-2 text-sm text-muted">
              {friend.displayName}'s achievements are private on Steam, so only yours are shown.
            </p>
          )}

          <div role="group" aria-label="Filter achievements" className="mt-4 flex flex-wrap gap-1">
            {filters.map(({ value, label }) => (
              <button
                key={value}
                type="button"
                onClick={() => setFilter(value)}
                aria-pressed={filter === value}
                className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-accent ${
                  filter === value ? "bg-primary text-white" : "text-muted hover:bg-card-hover hover:text-heading"
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          <ul className="mt-2 divide-y divide-line/60">
            {rows.filter(matches).map((a) => (
              <li key={a.apiName} className="flex items-start gap-3 py-3">
                <AchievementIcon src={a.iconUrl} locked={!a.myUnlockTime && !a.friendUnlockTime} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-baseline justify-between gap-3">
                    <p className="text-sm font-medium text-heading">{a.displayName}</p>
                    {a.globalPercent !== null && (
                      <p className="shrink-0 text-xs text-muted">
                        <span className="font-semibold text-fg">{formatRarity(a.globalPercent)}</span> of players
                      </p>
                    )}
                  </div>
                  <p className="text-xs text-muted">{a.description ?? "Hidden achievement"}</p>
                  <div className="mt-1.5 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted">
                    <UnlockStatus who="You" color="bg-series-you" time={a.myUnlockTime} />
                    {data.friendAchievementsPublic && (
                      <UnlockStatus who={friend.displayName} color="bg-series-friend" time={a.friendUnlockTime} />
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
          {rows.filter(matches).length === 0 && (
            <p className="py-6 text-center text-sm text-muted">Nothing here.</p>
          )}
        </>
      )}
    </div>
  );
}

/** You and a friend side by side: shared games and hours, then achievements per game. */
export default function FriendCompareModal({ friend, onClose }: { friend: Friend | null; onClose: () => void }) {
  const [game, setGame] = useState<SharedGame | null>(null);

  const close = () => {
    setGame(null);
    onClose();
  };

  return (
    <Modal isOpen={friend !== null} onClose={close} title={friend ? `You & ${friend.displayName}` : undefined} size="xl">
      {friend &&
        (game ? (
          <AchievementsView friend={friend} game={game} onBack={() => setGame(null)} />
        ) : (
          <LibraryView friend={friend} onSelectGame={setGame} />
        ))}
    </Modal>
  );
}
