import { useUser } from "../hooks/useAuth.ts";
import Header from "../components/Header.tsx";
import Loading from "../components/Loading.tsx";
import Login from "../components/Login.tsx";
import ProfileHero from "../components/ProfileHero.tsx";
import StatsOverview from "../components/StatsOverview.tsx";
import AchievementsHeatmap from "../components/AchievementsHeatmap.tsx";
import GameLibrary from "../components/GameLibrary.tsx";
import { primaryButton } from "../components/buttonStyles.ts";

function HomePage() {
  const { data: user, isLoading, error } = useUser();

  // Show full-page loading only when initially loading user data
  if (isLoading && !user) {
    return <Loading />;
  }

  if (error && !user) {
    return (
      <div className="flex min-h-screen items-center justify-center p-4">
        <div className="max-w-md rounded-2xl border border-line/60 bg-card p-6 text-center">
          <h2 className="mb-2 text-lg font-semibold text-heading">Connection error</h2>
          <p className="mb-4 text-muted">
            Unable to load user data. Please check your connection and try again.
          </p>
          <button type="button" onClick={() => window.location.reload()} className={primaryButton}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
        {user ? (
          <div className="space-y-6">
            <ProfileHero user={user} />
            <StatsOverview />
            <AchievementsHeatmap />
            <div className="pt-4">
              <GameLibrary />
            </div>
          </div>
        ) : (
          <Login />
        )}
      </main>
    </div>
  );
}

export default HomePage;
