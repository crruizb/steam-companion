import useAuth from "../hooks/useAuth.ts";
import { authService } from "../services/authService";

function HomePage() {
  const {
    user,
    isLoading,
    error,
    logout,
    isLoggingOut
  } = useAuth();

  const handleSteamLogin = () => {
    authService.initiateSteamLogin();
  };

  const handleLogout = () => {
    logout();
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // Show error state if user fetch failed and we don't have cached data
  if (error && !user) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-white rounded-lg shadow p-6 max-w-md">
            <h2 className="text-lg font-semibold text-gray-900 mb-2">Connection Error</h2>
            <p className="text-gray-600 mb-4">
              Unable to load user data. Please check your connection and try again.
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
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo/Title */}
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">
                Steam Companion
              </h1>
            </div>

            {/* User Section */}
            {user ? (
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-3">
                  {user.avatarUrl && (
                    <img
                      src={user.avatarUrl}
                      alt={user.displayName || user.username}
                      className="h-8 w-8 rounded-full"
                    />
                  )}
                  <div className="flex flex-col">
                    <span className="text-sm font-medium text-gray-900">
                      {user.displayName || user.username}
                    </span>
                    <span className="text-xs text-gray-500">Steam ID: {user.steamId}</span>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  disabled={isLoggingOut}
                  className="inline-flex items-center px-3 py-2 bg-red-600 hover:bg-red-700 disabled:bg-red-400 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                >
                  {isLoggingOut ? (
                    <>
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                      Logging out...
                    </>
                  ) : (
                    'Logout'
                  )}
                </button>
              </div>
            ) : (
              <button
                onClick={handleSteamLogin}
                className="inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                <svg
                  className="w-5 h-5 mr-2"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M11.979 0C5.678 0 0.511 4.86 0.022 11.037l6.432 2.658c.545-.371 1.203-.59 1.912-.59.063 0 .125.004.188.006l2.861-4.142V8.91c0-2.495 2.028-4.524 4.524-4.524 2.494 0 4.524 2.029 4.524 4.524s-2.03 4.525-4.524 4.525h-.105l-4.076 2.911c0 .052.004.105.004.159 0 1.875-1.515 3.396-3.39 3.396-1.635 0-3.016-1.173-3.331-2.727L.436 15.27C1.862 20.307 6.486 24 11.979 24c6.624 0 11.979-5.354 11.979-11.979C23.958 5.354 18.603.001 11.979.001zM7.54 18.21l-1.473-.61c.262.543.735.986 1.348 1.25 1.338.576 2.88-.069 3.456-1.406.274-.635.24-1.333-.096-1.918s-.94-.hang1.575-1.054c-.635-.274-1.333-.24-1.918.096l1.503.624c.986.41 1.447 1.578 1.037 2.564-.41.987-1.578 1.448-2.564 1.038z"/>
                </svg>
                Login with Steam
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
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
              {/* Profile Card */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Profile</h3>
                <div className="flex items-center space-x-4">
                  {user.avatarUrl && (
                    <img
                      src={user.avatarUrl}
                      alt={user.displayName || user.username}
                      className="h-16 w-16 rounded-full"
                    />
                  )}
                  <div>
                    <p className="font-medium text-gray-900">
                      {user.displayName || user.username}
                    </p>
                    <p className="text-sm text-gray-500">Steam ID: {user.steamId}</p>
                    {user.profileUrl && (
                      <a
                        href={user.profileUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 hover:text-blue-800"
                      >
                        View Steam Profile
                      </a>
                    )}
                  </div>
                </div>
              </div>

              {/* Games Statistics */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Game Library</h3>
                <div className="text-center">
                  <div className="text-3xl font-bold text-blue-600">
                    {user.ownedGames ? user.ownedGames.length : 0}
                  </div>
                  <div className="text-sm text-gray-500">Games Owned</div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
                <div className="space-y-3">
                  <button className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded">
                    View Game Library
                  </button>
                  <button className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded">
                    Check Achievements
                  </button>
                  <button className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded">
                    Gaming Statistics
                  </button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              Welcome to Steam Companion
            </h2>
            <p className="text-lg text-gray-600 mb-8">
              Connect with Steam to access your gaming library and stats.
            </p>
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-gray-500 mb-4">
                Please log in with your Steam account to get started.
              </p>
              <button
                onClick={handleSteamLogin}
                className="inline-flex items-center px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                <svg
                  className="w-5 h-5 mr-2"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M11.979 0C5.678 0 0.511 4.86 0.022 11.037l6.432 2.658c.545-.371 1.203-.59 1.912-.59.063 0 .125.004.188.006l2.861-4.142V8.91c0-2.495 2.028-4.524 4.524-4.524 2.494 0 4.524 2.029 4.524 4.524s-2.03 4.525-4.524 4.525h-.105l-4.076 2.911c0 .052.004.105.004.159 0 1.875-1.515 3.396-3.39 3.396-1.635 0-3.016-1.173-3.331-2.727L.436 15.27C1.862 20.307 6.486 24 11.979 24c6.624 0 11.979-5.354 11.979-11.979C23.958 5.354 18.603.001 11.979.001zM7.54 18.21l-1.473-.61c.262.543.735.986 1.348 1.25 1.338.576 2.88-.069 3.456-1.406.274-.635.24-1.333-.096-1.918s-.94-.hang1.575-1.054c-.635-.274-1.333-.24-1.918.096l1.503.624c.986.41 1.447 1.578 1.037 2.564-.41.987-1.578 1.448-2.564 1.038z"/>
                </svg>
                Login with Steam
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default HomePage;
