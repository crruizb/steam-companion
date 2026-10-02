import { useState } from "react";
import { Clock, Dices, Download, ExternalLink } from "lucide-react";
import { useImportGames, useRandomGame } from "../hooks/useGames";
import type { Game } from "../types";
import { formatPlayTime } from "../util";
import ImportAchievementsButton from "./ImportAchievementsButton";
import Modal from "./Modal";
import { primaryButton, secondaryButton } from "./buttonStyles";

export default function QuickActions() {
  const { mutate: importGames, isPending: isImportingGames } = useImportGames();
  const { mutate: pickRandomGame, isPending: isPicking } = useRandomGame();
  const [randomGame, setRandomGame] = useState<Game | null>(null);

  // The backend picks the game; with no games imported it returns 404 and a toast explains why
  const handleRandomGame = () => {
    pickRandomGame(undefined, { onSuccess: setRandomGame });
  };

  return (
    <>
      <div className="grid gap-2 sm:flex sm:flex-wrap">
        <button
          type="button"
          className={secondaryButton}
          onClick={() => importGames()}
          disabled={isImportingGames}
        >
          <Download className="h-4 w-4" />
          Import games
        </button>
        <ImportAchievementsButton className={secondaryButton} />
        <button type="button" className={primaryButton} onClick={handleRandomGame} disabled={isPicking}>
          <Dices className="h-4 w-4" />
          Pick a random game
        </button>
      </div>

      <Modal
        isOpen={randomGame !== null}
        onClose={() => setRandomGame(null)}
        title="Your next game"
        footer={
          randomGame && (
            <>
              <button type="button" className={secondaryButton} onClick={handleRandomGame} disabled={isPicking}>
                <Dices className="h-4 w-4" />
                Pick another
              </button>
              <a
                className={primaryButton}
                href={`https://store.steampowered.com/app/${randomGame.appId}`}
                target="_blank"
                rel="noopener noreferrer"
              >
                View in store
                <ExternalLink className="h-4 w-4" />
              </a>
            </>
          )
        }
      >
        {randomGame && (
          <>
            <img
              src={`https://cdn.cloudflare.steamstatic.com/steam/apps/${randomGame.appId}/header.jpg`}
              alt=""
              className="w-full rounded-lg border border-line/60"
            />
            <p className="mt-4 text-xl font-semibold text-heading">{randomGame.name}</p>
            <p className="mt-1 flex items-center gap-1.5 text-sm text-muted">
              <Clock className="h-4 w-4" />
              {randomGame.playTimeForeverMinutes > 0
                ? `${formatPlayTime(randomGame.playTimeForeverMinutes)} played`
                : "Never played"}
            </p>
          </>
        )}
      </Modal>
    </>
  );
}
