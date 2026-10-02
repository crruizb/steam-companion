import { Clock, Dices, ExternalLink, History, Play, Trophy } from "lucide-react";
import { RANDOM_GAME_FILTERS, type useRandomGamePicker } from "../hooks/useGames";
import { formatPlayTime, timeAgo } from "../util";
import { primaryButton, secondaryButton } from "./buttonStyles";
import Modal from "./Modal";

/** Picks a game to play from the library, optionally narrowed by a filter. */
export default function RandomGameModal({ picker }: { picker: ReturnType<typeof useRandomGamePicker> }) {
  const { isOpen, filter, game, isPicking, error } = picker;

  return (
    <Modal
      isOpen={isOpen}
      onClose={picker.close}
      title="Your next game"
      size="lg"
      footer={
        game && (
          <>
            <button type="button" className={secondaryButton} onClick={picker.reroll} disabled={isPicking}>
              <Dices className="h-4 w-4" />
              Pick another
            </button>
            <a
              className={secondaryButton}
              href={`https://store.steampowered.com/app/${game.appId}`}
              target="_blank"
              rel="noopener noreferrer"
            >
              Store
              <ExternalLink className="h-4 w-4" />
            </a>
            {/* Opens the Steam client, which installs the game first if needed */}
            <a className={primaryButton} href={`steam://run/${game.appId}`}>
              <Play className="h-4 w-4" />
              Play on Steam
            </a>
          </>
        )
      }
    >
      <div role="group" aria-label="Pick from" className="flex flex-wrap gap-1.5">
        {RANDOM_GAME_FILTERS.map(({ value, label }) => (
          <button
            key={value}
            type="button"
            onClick={() => picker.changeFilter(value)}
            aria-pressed={filter === value}
            disabled={isPicking}
            className={`rounded-full border px-3 py-1 text-xs font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-accent disabled:cursor-wait ${
              filter === value
                ? "border-primary bg-primary text-white"
                : "border-line text-muted hover:bg-card-hover hover:text-heading"
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="mt-4">
        {game ? (
          // Holds the previous game while the next one loads, so the dialog doesn't jump
          <div className={isPicking ? "opacity-60 transition-opacity" : "transition-opacity"}>
            <img
              key={game.appId}
              src={`https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/header.jpg`}
              alt=""
              className="aspect-[460/215] w-full rounded-lg border border-line/60 bg-heat-0 object-cover"
            />
            <p className="mt-4 text-xl font-semibold text-heading">{game.name}</p>
            <div className="mt-1.5 flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted">
              <span className="inline-flex items-center gap-1.5">
                <Clock className="h-4 w-4" />
                {game.playTimeForeverMinutes > 0 ? `${formatPlayTime(game.playTimeForeverMinutes)} played` : "Never played"}
              </span>
              {game.lastPlayedAt && (
                <span className="inline-flex items-center gap-1.5">
                  <History className="h-4 w-4" />
                  Last played {timeAgo(new Date(game.lastPlayedAt))}
                </span>
              )}
              {!!game.achievementsTotal && (
                <span className="inline-flex items-center gap-1.5">
                  <Trophy className="h-4 w-4" />
                  {game.achievementsUnlocked ?? 0}/{game.achievementsTotal} achievements
                </span>
              )}
            </div>
          </div>
        ) : isPicking ? (
          <div className="flex justify-center py-16">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-accent border-t-transparent" />
          </div>
        ) : (
          error && <p className="py-10 text-center text-sm text-muted">{error} Try another filter.</p>
        )}
      </div>
    </Modal>
  );
}
