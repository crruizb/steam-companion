import { useEffect, type ReactNode } from "react";

type ModalProps = {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
};

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
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
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* Modal */}
      <div className="relative z-10 w-full max-w-md rounded-xl bg-white p-6 shadow-lg">
        {title && <h2 className="mb-4 text-lg font-semibold">{title}</h2>}

        <div>{children}</div>

        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="rounded-md bg-gray-100 px-4 py-2 text-sm hover:bg-gray-200"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
