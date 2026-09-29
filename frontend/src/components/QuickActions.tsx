import { useState } from "react";
import { useImportGames, useRandomGame } from "../hooks/useGames";
import Modal from "./Modal";
import { useImportAchievements } from "../hooks/useAchievements";

export default function QuickActions() {
  const { mutate: importGames } = useImportGames();
  const { mutate: importAchievements } = useImportAchievements();
  const { mutate: pickRandomGame } = useRandomGame();
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [gameInfo, setGameInfo] = useState({
    name: "",
    imgUrl: "",
  });

  const handleImportGames = () => {
    importGames();
  };

  const handleImportAchievements = () => {
    importAchievements();
  };

  const handleRandomGame = () => {
    // The backend picks the game; with no games imported it returns 404 and a toast explains why
    pickRandomGame(undefined, {
      onSuccess: (game) => {
        setGameInfo({
          name: game.name,
          imgUrl: `https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/header.jpg`,
        });
        setOpenModal(true);
      },
    });
  };

  return (
    <section className="bg-white rounded-lg shadow p-6">
      <Modal
        isOpen={openModal}
        onClose={() => setOpenModal(false)}
        title="Random Game Selected"
      >
        <p className="text-sm text-gray-600">{gameInfo.name}</p>
        <img
          src={gameInfo.imgUrl || ""}
          alt={gameInfo.name}
          className="mt-4 w-full rounded-2xl"
        />
      </Modal>

      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Quick Actions
      </h3>
      <div className="space-y-3">
        <button
          className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded cursor-pointer"
          onClick={handleImportGames}
        >
          Import games
        </button>
        <button
          className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded cursor-pointer"
          onClick={handleImportAchievements}
        >
          Import achievements
        </button>
        <button
          className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded cursor-pointer"
          onClick={handleRandomGame}
        >
          Choose random game
        </button>
      </div>
    </section>
  );
}
