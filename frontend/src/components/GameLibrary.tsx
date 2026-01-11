import { useUserGames } from "../hooks/useGames";
import { SteamImage } from "./SteamImage";

const formatPlayTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours === 0) return `${mins}m`;
  if (mins === 0) return `${hours}h`;
  return `${hours}h ${mins}m`;
};

export default function GameLibrary() {
  const { data: user } = useUserGames();

  const handleSteamGameClick = (appId: number) => {
    window.open(`https://store.steampowered.com/app/${appId}`, "_blank");
  };

  return (
    <div className="mt-10">
      <h1 className="text-2xl mb-2">Your game library</h1>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-5 gap-6">
        {user?.ownedGames
          ?.sort(() => Math.random() - 0.5)
          .map(
            (game) =>
              game.imgUrl && (
                <div
                  key={game.appId}
                  className="group relative bg-gray-800 rounded-lg overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-300 hover:scale-105 cursor-pointer"
                  onClick={() => handleSteamGameClick(game.appId)}
                >
                  <div className="aspect-3/4 relative">
                    <SteamImage
                      appId={game.appId}
                      alt={game.name}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-linear-to-t from-black/80 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  </div>
                  <div className="absolute bottom-0 left-0 right-0 p-3 translate-y-full group-hover:translate-y-0 transition-transform duration-300">
                    <h3 className="text-white text-sm font-semibold line-clamp-2">
                      {game.name}
                    </h3>
                    <h4 className="text-white text-xs">
                      {formatPlayTime(game.playTimeForeverMinutes)} played
                    </h4>
                  </div>
                </div>
              )
          )}
      </div>
    </div>
  );
}
