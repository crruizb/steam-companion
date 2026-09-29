import { useEffect, type ReactNode } from "react";
import { X } from "lucide-react";
import { secondaryButton } from "./buttonStyles";

type ModalProps = {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  /** Footer buttons. Defaults to a single Close button. */
  footer?: ReactNode;
};

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
}: ModalProps) {
  useEffect(() => {
    if (!isOpen) return;

    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };

    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      {/* Modal */}
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative z-10 w-full max-w-md rounded-2xl border border-line/60 bg-card p-6 shadow-2xl shadow-black/40"
      >
        <div className="mb-4 flex items-start justify-between gap-4">
          {title && <h2 className="text-lg font-semibold text-heading">{title}</h2>}
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="-m-1 rounded-lg p-1 text-muted hover:bg-card-hover hover:text-heading focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div>{children}</div>

        <div className="mt-6 flex flex-wrap justify-end gap-2">
          {footer ?? (
            <button type="button" onClick={onClose} className={secondaryButton}>
              Close
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
