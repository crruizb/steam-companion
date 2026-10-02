import { useState } from "react";
import { Trophy } from "lucide-react";

/** An achievement's Steam icon, with a trophy placeholder when there is none or it fails to load. */
export default function AchievementIcon({ src, locked = false }: { src: string | null; locked?: boolean }) {
  const [failed, setFailed] = useState(false);
  const box = `h-10 w-10 shrink-0 rounded-md ${locked ? "opacity-60" : ""}`;

  if (!src || failed) {
    return (
      <div className={`${box} flex items-center justify-center bg-heat-0 text-muted`}>
        <Trophy className="h-4 w-4" />
      </div>
    );
  }
  return <img src={src} alt="" loading="lazy" className={box} onError={() => setFailed(true)} />;
}
