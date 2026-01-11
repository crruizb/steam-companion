import { useAchievements } from "../hooks/useAchievements.ts";
import { useUserGames } from "../hooks/useGames.ts";
import type { AchievementsHeatmap } from "../types/index.ts";

export default function GameStatistics() {
  const { data: user } = useUserGames();
  const { data: achievements } = useAchievements();

  const getTotalAchievements = (heatmap: AchievementsHeatmap): number => {
    return Object.values(heatmap.achievementsPerDate)
      .flat()
      .reduce((total, day) => total + day.count, 0);
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Game Library</h3>
      <div className="text-center">
        <div className="text-3xl font-bold text-blue-600">
          {user?.ownedGames ? user.ownedGames.length : 0}
        </div>
        <div className="text-sm text-gray-500">Games Owned</div>
        <div className="text-3xl font-bold text-blue-600 mt-2">
          {achievements ? getTotalAchievements(achievements) : 0}
        </div>
        <div className="text-sm text-gray-500">Achievements</div>
      </div>
    </div>
  );
}
