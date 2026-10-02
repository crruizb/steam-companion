import { useRef, useState } from "react";
import { Download, Share2 } from "lucide-react";
import toast from "react-hot-toast";
import { useYearReview } from "../hooks/useAchievements";
import { useUser } from "../hooks/useAuth";
import { yearSummary } from "../insights";
import type { AchievementsPerDate } from "../types";
import { primaryButton, secondaryButton } from "./buttonStyles";
import Modal from "./Modal";
import YearReviewCard from "./YearReviewCard";
import { cardToPng } from "./yearReviewImage";

// Sharing files works on most phones and some desktop browsers; elsewhere the button is hidden
const canShareFiles = () =>
  typeof navigator.canShare === "function" &&
  navigator.canShare({ files: [new File([""], "test.png", { type: "image/png" })] });

/** The year's summary card, ready to download or share as an image. */
export default function YearInReviewModal({
  year,
  days,
  onClose,
}: {
  year: number | null;
  days: AchievementsPerDate[];
  onClose: () => void;
}) {
  const { data: user } = useUser();
  const { data: review, isLoading } = useYearReview(year);
  const cardRef = useRef<SVGSVGElement>(null);
  const [isExporting, setIsExporting] = useState(false);

  if (year === null) return null;
  const name = user?.displayName || user?.username || "Your";
  const fileName = `steam-${year}-in-review.png`;

  const exportImage = async (share: boolean) => {
    if (!cardRef.current) return;
    setIsExporting(true);
    try {
      const blob = await cardToPng(cardRef.current);
      const file = new File([blob], fileName, { type: "image/png" });
      if (share) {
        await navigator.share({ files: [file], title: `My ${year} on Steam` });
      } else {
        const url = URL.createObjectURL(blob);
        const link = Object.assign(document.createElement("a"), { href: url, download: fileName });
        link.click();
        URL.revokeObjectURL(url);
      }
    } catch (error) {
      // Closing the share sheet rejects with AbortError; that's not a failure
      if (!(error instanceof DOMException && error.name === "AbortError")) {
        toast.error("Could not create the image. Please try again.");
        console.error(error);
      }
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <Modal
      isOpen
      onClose={onClose}
      title={`Your ${year} in review`}
      size="xl"
      footer={
        <>
          {canShareFiles() && (
            <button type="button" className={secondaryButton} onClick={() => exportImage(true)} disabled={isLoading || isExporting}>
              <Share2 className="h-4 w-4" />
              Share
            </button>
          )}
          <button type="button" className={primaryButton} onClick={() => exportImage(false)} disabled={isLoading || isExporting}>
            <Download className="h-4 w-4" />
            Download image
          </button>
        </>
      }
    >
      {/* Holds the previous frame while the per-game highlights load */}
      <div className={isLoading ? "opacity-60" : undefined}>
        <YearReviewCard ref={cardRef} name={name} year={year} summary={yearSummary(days, year)} review={review} />
      </div>
      <p className="mt-3 text-xs text-muted">Years run in UTC, so unlocks near midnight on New Year's Eve may count in the next year.</p>
    </Modal>
  );
}
