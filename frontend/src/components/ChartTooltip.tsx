import type { ReactNode } from "react";

/**
 * Hover/focus readout for a chart mark. Put it inside a `group relative` element; it is only
 * rendered while that element is hovered or focused, so hidden tooltips never widen the layout.
 */
export default function ChartTooltip({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <div
      role="tooltip"
      className={`pointer-events-none absolute z-20 hidden whitespace-nowrap rounded-md bg-heading px-2 py-1 text-xs text-page shadow-lg group-hover:block group-focus-visible:block ${className}`}
    >
      {children}
    </div>
  );
}
