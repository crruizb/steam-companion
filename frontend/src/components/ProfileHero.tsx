import { ExternalLink } from "lucide-react";
import type { User } from "../types";
import Avatar from "./Avatar";
import QuickActions from "./QuickActions";

export default function ProfileHero({ user }: { user: User }) {
  return (
    <section className="relative overflow-hidden rounded-2xl border border-line/60 bg-card p-6 sm:p-8">
      {/* Soft accent glow behind the avatar */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -left-24 -top-24 h-72 w-72 rounded-full bg-accent/15 blur-3xl"
      />

      <div className="relative flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-center gap-5">
          <Avatar user={user} className="h-16 w-16 text-2xl ring-2 ring-accent/60 sm:h-20 sm:w-20" />
          <div>
            <p className="text-sm text-muted">Welcome back</p>
            <h1 className="text-2xl font-bold tracking-tight text-heading sm:text-3xl">
              {user.displayName || user.username}
            </h1>
            {user.profileUrl && (
              <a
                href={user.profileUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-1 inline-flex items-center gap-1 text-sm text-accent hover:underline"
              >
                Steam profile
                <ExternalLink className="h-3.5 w-3.5" />
              </a>
            )}
          </div>
        </div>

        <QuickActions />
      </div>
    </section>
  );
}
