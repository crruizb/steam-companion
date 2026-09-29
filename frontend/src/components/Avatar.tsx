import type { User } from "../types";

/** The Steam avatar, or the user's initial when there isn't one. */
export default function Avatar({ user, className = "" }: { user: User; className?: string }) {
  const name = user.displayName || user.username;

  if (user.avatarUrl) {
    return <img src={user.avatarUrl} alt="" className={`rounded-full object-cover ${className}`} />;
  }
  return (
    <span
      aria-hidden="true"
      className={`inline-flex items-center justify-center rounded-full bg-primary font-semibold text-white ${className}`}
    >
      {name.charAt(0).toUpperCase()}
    </span>
  );
}
