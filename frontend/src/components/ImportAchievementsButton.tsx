import { Trophy } from "lucide-react";
import { useAchievementsImport } from "../hooks/useAchievements";

/** Starts an achievements import, then shows its progress until it finishes. */
export default function ImportAchievementsButton({ className }: { className: string }) {
  const { startImport, isImporting, progress } = useAchievementsImport();

  return (
    <button type="button" className={className} onClick={startImport} disabled={isImporting}>
      {isImporting ? (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
      ) : (
        <Trophy className="h-4 w-4" />
      )}
      {isImporting ? `Importing achievements${progress ? ` (${progress})` : "..."}` : "Import achievements"}
    </button>
  );
}
