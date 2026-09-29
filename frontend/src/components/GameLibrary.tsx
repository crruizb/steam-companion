import { useMemo } from "react";
import { Clock, Download, Gamepad2 } from "lucide-react";
import { useImportGames, useUserGames } from "../hooks/useGames";
import { formatPlayTime, shuffle } from "../util";
import { SteamImage } from "./SteamImage";
import { primaryButton } from "./buttonStyles";

function EmptyLibrary() {
  const { mutate: importGames, isPending } = useImportGames();

  return (
    <div className="flex flex-col items-center rounded-2xl border border-dashed border-line py-12 text-center">
      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-accent/15 text-accent">
        <Gamepad2 className="h-6 w-6" />
      </div>
      <p className="mt-4 font-semibold text-heading">No games imported yet</p>
      <p className="mt-1 text-sm text-muted">Import your Steam library to see it here.</p>
      <button type="button" className={`${primaryButton} mt-5`} onClick={() => importGames()} disabled={isPending}>
        <Download className="h-4 w-4" />
        Import games
      </button>
    </div>
  );
}

export default function GameLibrary() {
  const { data: user, isLoading } = useUserGames();
  // Shuffle a copy once per fetched library, so re-renders keep the same order
  const games = useMemo(() => shuffle(user?.ownedGames ?? []), [user?.ownedGames]);

  return (
    <section>
      <div className="mb-4 flex items-baseline justify-between">
        <h2 className="text-xl font-semibold text-heading">Your library</h2>
        {games.length > 0 && <span className="text-sm text-muted">{games.length} games</span>}
      </div>

      {!isLoading && games.length === 0 ? (
        <EmptyLibrary />
      ) : (
        <div className="grid grid-cols-2 gap-x-4 gap-y-6 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {games.map((game) => (
            <a
              key={game.appId}
              href={`https://store.steampowered.com/app/${game.appId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="group rounded-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
            >
              <div className="aspect-3/4 overflow-hidden rounded-xl border border-line/60 bg-card shadow-md shadow-black/10 transition duration-200 group-hover:-translate-y-1 group-hover:border-accent/70 group-hover:shadow-xl">
                <SteamImage
                  appId={game.appId}
                  alt={game.name}
                  className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                />
              </div>
              <p className="mt-2 line-clamp-1 text-sm font-medium text-heading" title={game.name}>
                {game.name}
              </p>
              <p className="flex items-center gap-1 text-xs text-muted">
                <Clock className="h-3 w-3" />
                {game.playTimeForeverMinutes > 0 ? formatPlayTime(game.playTimeForeverMinutes) : "Never played"}
              </p>
            </a>
          ))}
        </div>
      )}
    </section>
  );
}
