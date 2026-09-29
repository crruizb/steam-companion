import type { ReactNode } from "react";
import { BarChart3, CalendarDays, Dices } from "lucide-react";
import { initiateSteamLogin } from "../services/authService.ts";
import SteamIcon from "./SteamIcon.tsx";

const features: { icon: ReactNode; title: string; description: string }[] = [
  {
    icon: <BarChart3 className="h-5 w-5" />,
    title: "Your whole library",
    description:
      "Every game you own with its cover art and playtime, plus totals for games, hours and achievements.",
  },
  {
    icon: <CalendarDays className="h-5 w-5" />,
    title: "Achievement activity",
    description:
      "A year-by-year heatmap of the days you unlocked achievements, with your longest streak.",
  },
  {
    icon: <Dices className="h-5 w-5" />,
    title: "Can't decide what to play?",
    description: "Let us pick a random game from your library for your next session.",
  },
];

export default function Login() {
  return (
    <div className="relative">
      {/* Soft accent glow behind the hero */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute left-1/2 top-0 h-80 w-[36rem] max-w-full -translate-x-1/2 rounded-full bg-accent/20 blur-3xl"
      />

      <section className="relative mx-auto max-w-3xl py-12 text-center sm:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-line/60 bg-card/60 px-3 py-1 text-xs font-medium text-muted">
          <SteamIcon className="h-3.5 w-3.5 text-accent" />
          Your personal Steam dashboard
        </span>
        <h1 className="mt-6 text-4xl font-bold tracking-tight text-heading sm:text-6xl">
          Your Steam library, <span className="whitespace-nowrap text-accent">at a glance</span>
        </h1>
        <p className="mx-auto mt-5 max-w-xl text-lg text-muted">
          Connect with Steam to explore your games, playtime and achievement history in one place.
        </p>
        <button
          type="button"
          onClick={initiateSteamLogin}
          className="mt-8 inline-flex items-center gap-3 rounded-xl bg-linear-to-r from-[#06bfff] to-[#2d73ff] px-7 py-3.5 font-semibold text-white shadow-lg shadow-[#2d73ff]/30 transition hover:-translate-y-0.5 hover:brightness-110 focus:outline-none focus-visible:ring-2 focus-visible:ring-accent"
        >
          <SteamIcon className="h-6 w-6" />
          Log in with Steam
        </button>
        <p className="mt-4 text-sm text-muted">We only access public Steam profile information.</p>
      </section>

      <section className="relative grid gap-4 pb-12 md:grid-cols-3">
        {features.map((feature) => (
          <div
            key={feature.title}
            className="rounded-2xl border border-line/60 bg-card p-6 transition-colors hover:border-accent/60"
          >
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent/15 text-accent">
              {feature.icon}
            </div>
            <h3 className="mt-4 font-semibold text-heading">{feature.title}</h3>
            <p className="mt-1.5 text-sm text-muted">{feature.description}</p>
          </div>
        ))}
      </section>
    </div>
  );
}
