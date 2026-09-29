import type { ReactNode } from "react";
import { Clock, Gamepad2, Ghost, Trophy } from "lucide-react";
import { useUserGames } from "../hooks/useGames";
import { useAchievements } from "../hooks/useAchievements";
import { formatNumber } from "../util";

function StatTile({ icon, label, value }: { icon: ReactNode; label: string; value: number | undefined }) {
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-line/60 bg-card p-4 sm:flex-row sm:items-center sm:gap-4 sm:p-5">
      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-accent/15 text-accent">
        {icon}
      </div>
      <div className="min-w-0">
        <p className="text-sm text-muted">{label}</p>
        <p className="text-2xl font-semibold text-heading">
          {value === undefined ? "–" : formatNumber(value)}
        </p>
      </div>
    </div>
  );
}

export default function StatsOverview() {
  const { data: user } = useUserGames();
  const { data: achievements } = useAchievements();

  const games = user?.ownedGames ?? undefined;
  const hoursPlayed = games && Math.round(games.reduce((total, g) => total + g.playTimeForeverMinutes, 0) / 60);
  const neverPlayed = games?.filter((g) => g.playTimeForeverMinutes === 0).length;
  const totalAchievements =
    achievements &&
    Object.values(achievements.achievementsPerDate)
      .flat()
      .reduce((total, day) => total + day.count, 0);

  return (
    <section className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
      <StatTile icon={<Gamepad2 className="h-5 w-5" />} label="Games owned" value={games?.length} />
      <StatTile icon={<Clock className="h-5 w-5" />} label="Hours played" value={hoursPlayed} />
      <StatTile icon={<Trophy className="h-5 w-5" />} label="Achievements" value={totalAchievements} />
      <StatTile icon={<Ghost className="h-5 w-5" />} label="Never played" value={neverPlayed} />
    </section>
  );
}
