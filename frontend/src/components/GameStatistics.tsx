import { useUserGames } from "../hooks/useGames.ts";

export default function GameStatistics() {
  const { data: user } = useUserGames();

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Game Library</h3>
      <div className="text-center">
        <div className="text-3xl font-bold text-blue-600">
          {user?.ownedGames ? user.ownedGames.length : 0}
        </div>
        <div className="text-sm text-gray-500">Games Owned</div>
      </div>
    </div>
  );
}
