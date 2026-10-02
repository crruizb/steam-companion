import { useState } from "react";
import { ExternalLink, Search, Users } from "lucide-react";
import { useFriends } from "../hooks/useFriends";
import { ApiError } from "../services/api";
import type { Friend } from "../types";
import Avatar from "./Avatar";
import FriendCompareModal from "./FriendCompareModal";

const cardClass = "rounded-2xl border border-line/60 bg-card p-5 sm:p-6";
const SHOWN = 12;

function FriendButton({ friend, onSelect }: { friend: Friend; onSelect: (friend: Friend) => void }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(friend)}
      className="flex min-w-0 items-center gap-3 rounded-xl border border-line/60 p-2.5 text-left transition-colors hover:bg-card-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
    >
      <Avatar user={friend} className="h-10 w-10 shrink-0 text-sm" />
      <div className="min-w-0">
        <p className="truncate text-sm font-medium text-heading">{friend.displayName}</p>
        <p className="truncate text-xs text-muted">
          {friend.friendSince ? `Friends since ${new Date(friend.friendSince).getFullYear()}` : "Steam friend"}
        </p>
      </div>
    </button>
  );
}

export default function Friends() {
  const { data: friends, isLoading, error } = useFriends();
  const [query, setQuery] = useState("");
  const [showAll, setShowAll] = useState(false);
  const [selected, setSelected] = useState<Friend | null>(null);

  const search = query.trim().toLowerCase();
  const matching = (friends ?? []).filter((f) => f.displayName.toLowerCase().includes(search));
  const shown = showAll || search ? matching : matching.slice(0, SHOWN);

  return (
    <section className={cardClass}>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-heading">Friends</h2>
          <p className="text-sm text-muted">Pick a friend to compare libraries and achievements</p>
        </div>
        {(friends?.length ?? 0) > SHOWN && (
          <div className="relative w-full sm:w-60">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
            <input
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search friends"
              aria-label="Search friends"
              className="w-full rounded-lg border border-line bg-card py-2 pl-9 pr-3 text-sm text-fg placeholder:text-muted focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
            />
          </div>
        )}
      </div>

      <div className="mt-5">
        {isLoading ? (
          <div className="flex justify-center py-6">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-accent border-t-transparent" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center py-6 text-center">
            <Users className="h-6 w-6 text-muted" />
            <p className="mt-3 max-w-md text-sm text-muted">
              {/* 400 carries the reason, e.g. a private friends list */}
              {error instanceof ApiError && error.status === 400
                ? error.message
                : "Could not load your Steam friends. Please try again later."}
            </p>
            {error instanceof ApiError && error.status === 400 && (
              <a
                href="https://steamcommunity.com/my/edit/settings"
                target="_blank"
                rel="noopener noreferrer"
                className="mt-2 inline-flex items-center gap-1 text-sm text-accent hover:underline"
              >
                Steam privacy settings
                <ExternalLink className="h-3.5 w-3.5" />
              </a>
            )}
          </div>
        ) : !friends?.length ? (
          <p className="py-6 text-center text-sm text-muted">You have no friends on Steam yet.</p>
        ) : shown.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted">No friends match "{query}".</p>
        ) : (
          <>
            <div className="grid grid-cols-1 gap-2 min-[420px]:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
              {shown.map((friend) => (
                <FriendButton key={friend.steamId} friend={friend} onSelect={setSelected} />
              ))}
            </div>
            {!search && matching.length > SHOWN && (
              <button
                type="button"
                onClick={() => setShowAll(!showAll)}
                className="mt-3 text-sm font-medium text-accent hover:underline"
              >
                {showAll ? "Show fewer" : `Show all ${matching.length} friends`}
              </button>
            )}
          </>
        )}
      </div>

      <FriendCompareModal friend={selected} onClose={() => setSelected(null)} />
    </section>
  );
}
