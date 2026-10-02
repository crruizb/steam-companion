/** A single ratio against its limit: accent fill (or a series color) on a light track. */
export default function Meter({
  value,
  label,
  className = "h-2",
  fillClassName = "bg-accent",
}: {
  value: number;
  label: string;
  className?: string;
  fillClassName?: string;
}) {
  const percent = Math.round(Math.min(Math.max(value, 0), 1) * 100);
  return (
    <div
      role="meter"
      aria-label={label}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={percent}
      className={`overflow-hidden rounded-full bg-heat-0 ${className}`}
    >
      <div className={`h-full rounded-full ${fillClassName}`} style={{ width: `${percent}%` }} />
    </div>
  );
}
