import { Dices, Download } from "lucide-react";
import { useImportGames, useRandomGamePicker } from "../hooks/useGames";
import ImportAchievementsButton from "./ImportAchievementsButton";
import RandomGameModal from "./RandomGameModal";
import { primaryButton, secondaryButton } from "./buttonStyles";

export default function QuickActions() {
  const { mutate: importGames, isPending: isImportingGames } = useImportGames();
  const picker = useRandomGamePicker();

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
        <button type="button" className={primaryButton} onClick={picker.open} disabled={picker.isPicking}>
          <Dices className="h-4 w-4" />
          Pick a random game
        </button>
      </div>

      <RandomGameModal picker={picker} />
    </>
  );
}
