import { useMemo, useState } from "react";
import { ArrowUpDown, Clock, Download, Gamepad2, Search, X } from "lucide-react";
import { useImportGames, useUserGames } from "../hooks/useGames";
import {
  filterAndSortGames,
  formatPlayTime,
  shuffle,
  type LibraryFilter,
  type LibrarySort,
} from "../util";
import { SteamImage } from "./SteamImage";
import { primaryButton, secondaryButton } from "./buttonStyles";

const FILTERS: { value: LibraryFilter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "played", label: "Played" },
  { value: "never-played", label: "Never played" },
];

const SORTS: { value: LibrarySort; label: string }[] = [
  { value: "most-played", label: "Most played" },
  { value: "recent", label: "Recently played" },
  { value: "name", label: "Name (A–Z)" },
  { value: "shuffle", label: "Shuffle" },
];

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
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<LibraryFilter>("all");
  const [sort, setSort] = useState<LibrarySort>("most-played");

  // Shuffle once per fetched library, so the "Shuffle" order stays put while searching
  const shuffled = useMemo(() => shuffle(user?.ownedGames ?? []), [user?.ownedGames]);
  const games = useMemo(
    () => filterAndSortGames(shuffled, { query, filter, sort }),
    [shuffled, query, filter, sort],
  );

  const total = shuffled.length;
  const isFiltered = query.trim() !== "" || filter !== "all";
  const hasLastPlayed = shuffled.some((game) => game.lastPlayedAt);

  const clearFilters = () => {
    setQuery("");
    setFilter("all");
  };

  if (!isLoading && total === 0) {
    return (
      <section>
        <h2 className="mb-4 text-xl font-semibold text-heading">Your library</h2>
        <EmptyLibrary />
      </section>
    );
  }

  return (
    <section>
      <div className="mb-4 flex items-baseline justify-between gap-4">
        <h2 className="text-xl font-semibold text-heading">Your library</h2>
        {total > 0 && (
          <span className="text-sm text-muted">
            {isFiltered ? `${games.length} of ${total} games` : `${total} games`}
          </span>
        )}
      </div>

      <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center">
        <div className="relative sm:w-72">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search your library"
            aria-label="Search your library"
            className="w-full rounded-lg border border-line bg-card py-2 pl-9 pr-9 text-sm text-fg placeholder:text-muted focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
          />
          {query && (
            <button
              type="button"
              onClick={() => setQuery("")}
              aria-label="Clear search"
              className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-muted hover:text-heading"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>

        <div role="group" aria-label="Filter games" className="flex rounded-lg border border-line bg-card p-1">
          {FILTERS.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => setFilter(option.value)}
              aria-pressed={filter === option.value}
              className={`flex-1 whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-accent ${
                filter === option.value ? "bg-primary text-white" : "text-muted hover:text-heading"
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>

        <label className="relative flex items-center sm:ml-auto">
          <ArrowUpDown className="pointer-events-none absolute left-3 h-4 w-4 text-muted" />
          <span className="sr-only">Sort by</span>
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value as LibrarySort)}
            className="w-full rounded-lg border border-line bg-card py-2 pl-9 pr-3 text-sm text-fg focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
          >
            {SORTS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {sort === "recent" && total > 0 && !hasLastPlayed && (
        <p className="mb-4 rounded-lg border border-line/60 bg-card px-4 py-2 text-sm text-muted">
          Import your games again to sort by when you last played them.
        </p>
      )}

      {games.length === 0 && !isLoading ? (
        <div className="flex flex-col items-center rounded-2xl border border-dashed border-line py-12 text-center">
          <p className="font-semibold text-heading">No games match your filters</p>
          <p className="mt-1 text-sm text-muted">
            {query.trim() ? `Nothing called "${query.trim()}"` : "Try a different filter"}
            {filter !== "all" && ` in ${FILTERS.find((f) => f.value === filter)?.label.toLowerCase()} games`}.
          </p>
          <button type="button" className={`${secondaryButton} mt-5`} onClick={clearFilters}>
            Clear filters
          </button>
        </div>
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
                {sort === "recent" && game.lastPlayedAt
                  ? `Played ${new Date(game.lastPlayedAt).toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" })}`
                  : game.playTimeForeverMinutes > 0
                    ? formatPlayTime(game.playTimeForeverMinutes)
                    : "Never played"}
              </p>
            </a>
          ))}
        </div>
      )}
    </section>
  );
}
