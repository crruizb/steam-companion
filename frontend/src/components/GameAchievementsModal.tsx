import { Lock } from "lucide-react";
import { useGameAchievements } from "../hooks/useAchievements";
import { formatRarity } from "../insights";
import type { GameAchievement } from "../types";
import AchievementIcon from "./AchievementIcon";
import Meter from "./Meter";
import Modal from "./Modal";

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });

function AchievementRow({ achievement }: { achievement: GameAchievement }) {
  const { displayName, description, iconUrl, unlocked, unlockTime, globalPercent } = achievement;
  return (
    <li className="flex items-start gap-3 py-3">
      <AchievementIcon src={iconUrl} locked={!unlocked} />
      <div className="min-w-0 flex-1">
        <p className={`text-sm font-medium ${unlocked ? "text-heading" : "text-fg"}`}>{displayName}</p>
        <p className="text-xs text-muted">{description ?? (unlocked ? "" : "Hidden achievement")}</p>
      </div>
      <div className="shrink-0 text-right text-xs text-muted">
        {globalPercent !== null && (
          <p>
            <span className="font-semibold text-fg">{formatRarity(globalPercent)}</span> of players
          </p>
        )}
        <p className="mt-0.5 inline-flex items-center gap-1">
          {unlocked ? (
            unlockTime && formatDate(unlockTime)
          ) : (
            <>
              <Lock className="h-3 w-3" />
              Locked
            </>
          )}
        </p>
      </div>
    </li>
  );
}

/** A game's achievements: what's unlocked and when, what's left, and how rare each one is. */
export default function GameAchievementsModal({
  game,
  onClose,
}: {
  game: { appId: number; name: string } | null;
  onClose: () => void;
}) {
  const { data, isLoading, isError } = useGameAchievements(game?.appId ?? null);
  const achievements = data?.achievements ?? [];
  const unlockedCount = achievements.filter((a) => a.unlocked).length;

  return (
    <Modal isOpen={game !== null} onClose={onClose} title={game?.name} size="lg">
      {isLoading ? (
        <div className="flex justify-center py-10">
          <span className="h-6 w-6 animate-spin rounded-full border-2 border-accent border-t-transparent" />
        </div>
      ) : isError ? (
        <p className="py-6 text-sm text-muted">Could not load this game's achievements. Please try again.</p>
      ) : (
        <>
          {data?.hasDetails ? (
            <div className="mb-1">
              <p className="text-sm text-muted">
                <span className="font-semibold text-heading">{unlockedCount}</span> of {achievements.length} unlocked
              </p>
              <Meter
                value={achievements.length ? unlockedCount / achievements.length : 0}
                label="Achievements unlocked"
                className="mt-2 h-1.5"
              />
            </div>
          ) : (
            <p className="mb-1 rounded-lg border border-line/60 px-3 py-2 text-sm text-muted">
              Import achievements again to see names, icons and rarity for this game.
            </p>
          )}
          {achievements.length === 0 ? (
            <p className="py-6 text-sm text-muted">No achievements unlocked yet.</p>
          ) : (
            <ul className="divide-y divide-line/60">
              {achievements.map((a) => (
                <AchievementRow key={a.apiName} achievement={a} />
              ))}
            </ul>
          )}
        </>
      )}
    </Modal>
  );
}
