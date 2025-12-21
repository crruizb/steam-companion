import { useUser } from "../hooks/useAuth.ts";
import { useState } from "react";
import Header from "../components/Header.tsx";
import Loading from "../components/Loading.tsx";
import ProfileCard from "../components/ProfileCard.tsx";
import GameStatistics from "../components/GameStatistics.tsx";
import QuickActions from "../components/QuickActions.tsx";
import Login from "../components/Login.tsx";
import GameLibrary from "../components/GameLibrary.tsx";
import AchievementsHeatmap from "../components/AchievementsHeatmap.tsx";

type ViewType = "library" | "achievements" | null;

function HomePage() {
  const { data: user, isLoading, error } = useUser();
  const [activeView, setActiveView] = useState<ViewType>("library");

  // Show full-page loading only when initially loading user data
  if (isLoading && !user) {
    return <Loading />;
  }

  if (error && !user) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-white rounded-lg shadow p-6 max-w-md">
            <h2 className="text-lg font-semibold text-gray-900 mb-2">
              Connection Error
            </h2>
            <p className="text-gray-600 mb-4">
              Unable to load user data. Please check your connection and try
              again.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Header />
      <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        {user ? (
          <div>
            <div className="text-center mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-4">
                Welcome back, {user.displayName || user.username}!
              </h2>
              <p className="text-lg text-gray-600">
                Here's your Steam companion dashboard
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <ProfileCard user={user} />
              <GameStatistics />
              <QuickActions onViewChange={setActiveView} />
            </div>
            {activeView === "library" && <GameLibrary />}
            {activeView === "achievements" && <AchievementsHeatmap />}
          </div>
        ) : (
          <Login />
        )}
      </main>
    </div>
  );
}

export default HomePage;
