import { LogOut } from "lucide-react";
import { useLogout, useUser } from "../hooks/useAuth.ts";
import { initiateSteamLogin } from "../services/authService.ts";
import SteamIcon from "./SteamIcon.tsx";
import ThemeToggle from "./ThemeToggle.tsx";
import Avatar from "./Avatar.tsx";

export default function Header() {
  const { data: user } = useUser();
  const { mutate: logout, isPending: isLoggingOut } = useLogout();

  return (
    <header className="sticky top-0 z-40 border-b border-line/60 bg-header/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <a href="/" className="flex items-center gap-2.5 text-heading">
          <SteamIcon className="h-7 w-7 text-accent" />
          <span className="text-lg font-bold tracking-tight">Steam Companion</span>
        </a>

        <div className="flex items-center gap-2">
          <ThemeToggle />

          {user ? (
            <>
              <div className="ml-2 hidden items-center gap-2.5 sm:flex">
                <Avatar user={user} className="h-8 w-8 text-xs" />
                <span className="text-sm font-medium text-heading">
                  {user.displayName || user.username}
                </span>
              </div>
              <button
                type="button"
                onClick={() => logout()}
                disabled={isLoggingOut}
                aria-label="Log out"
                title="Log out"
                className="ml-1 inline-flex h-9 items-center gap-2 rounded-lg px-2.5 text-sm font-medium text-muted hover:bg-card-hover hover:text-heading disabled:cursor-not-allowed disabled:opacity-50 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
              >
                {isLoggingOut ? (
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                ) : (
                  <LogOut className="h-4 w-4" />
                )}
                <span className="hidden sm:inline">Log out</span>
              </button>
            </>
          ) : (
            <button
              type="button"
              onClick={initiateSteamLogin}
              className="ml-1 inline-flex h-9 items-center gap-2 rounded-lg bg-primary px-3.5 text-sm font-semibold text-white hover:bg-primary-hover transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
            >
              <SteamIcon className="h-4 w-4" />
              Log in with Steam
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
