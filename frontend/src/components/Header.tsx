import { useLogout, useUser } from "../hooks/useAuth.ts";
import { initiateSteamLogin } from "../services/authService.ts";

export default function Header() {
  const { data: user } = useUser();

  const { mutate: logout, isPending: isLoggingOut } = useLogout();

  const handleSteamLogin = () => {
    initiateSteamLogin();
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo/Title */}
          <div className="flex items-center">
            <h1 className="sm:text-sm md:text-2xl font-bold text-gray-900">
              Steam Companion
            </h1>
          </div>

          {/* User Section */}
          {user ? (
            <div className="flex items-center space-x-4">
              <div className="hidden sm:flex items-center space-x-3">
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
                  <span className="text-xs text-gray-500">
                    Steam ID: {user.steamId}
                  </span>
                </div>
              </div>
              <button
                onClick={handleLogout}
                disabled={isLoggingOut}
                className="inline-flex items-center px-3 py-2 bg-red-600 hover:bg-red-700 disabled:bg-red-400 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
              >
                {isLoggingOut ? (
                  <span>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Logging out...
                  </span>
                ) : (
                  "Logout"
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
                <path d="M11.979 0C5.678 0 0.511 4.86 0.022 11.037l6.432 2.658c.545-.371 1.203-.59 1.912-.59.063 0 .125.004.188.006l2.861-4.142V8.91c0-2.495 2.028-4.524 4.524-4.524 2.494 0 4.524 2.029 4.524 4.524s-2.03 4.525-4.524 4.525h-.105l-4.076 2.911c0 .052.004.105.004.159 0 1.875-1.515 3.396-3.39 3.396-1.635 0-3.016-1.173-3.331-2.727L.436 15.27C1.862 20.307 6.486 24 11.979 24c6.624 0 11.979-5.354 11.979-11.979C23.958 5.354 18.603.001 11.979.001zM7.54 18.21l-1.473-.61c.262.543.735.986 1.348 1.25 1.338.576 2.88-.069 3.456-1.406.274-.635.24-1.333-.096-1.918s-.94-.hang1.575-1.054c-.635-.274-1.333-.24-1.918.096l1.503.624c.986.41 1.447 1.578 1.037 2.564-.41.987-1.578 1.448-2.564 1.038z" />
              </svg>
              Login with Steam
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
