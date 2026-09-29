import { initiateSteamLogin } from "../services/authService.ts";

export default function Login() {
  const handleSteamLogin = () => {
    initiateSteamLogin();
  };

  const features = [
    {
      icon: "📊",
      title: "Game Library Statistics",
      description:
        "View your complete game collection with total games owned and achievements earned",
    },
    {
      icon: "🏆",
      title: "Achievement Tracking",
      description:
        "Visualize your achievement progress with interactive heatmaps showing your gaming activity throughout the year",
    },
    {
      icon: "🎮",
      title: "Beautiful Game Display",
      description:
        "Browse your game library with high-quality cover art and detailed information",
    },
    {
      icon: "⚡",
      title: "Quick Actions",
      description:
        "Import your games and achievements, or let us pick a random game for your next session",
    },
    {
      icon: "👤",
      title: "Profile Integration",
      description:
        "Connect directly to your Steam profile and keep track of your gaming identity",
    },
  ];

  return (
    <div className="max-w-5xl mx-auto">
      <div className="text-center mb-12">
        <h2 className="text-4xl font-bold text-gray-900 mb-4">
          Welcome to Steam Companion
        </h2>
        <p className="text-xl text-gray-600 mb-2">
          Your personal Steam gaming dashboard
        </p>
        <p className="text-lg text-gray-500">
          Connect with Steam to unlock powerful insights about your gaming
          journey
        </p>
      </div>

      {/* Features Grid */}
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        {features.map((feature, index) => (
          <div
            key={index}
            className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow duration-200"
          >
            <div className="text-4xl mb-3">{feature.icon}</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {feature.title}
            </h3>
            <p className="text-gray-600 text-sm">{feature.description}</p>
          </div>
        ))}
      </div>

      {/* Login Card */}
      <div className="bg-white rounded-lg shadow-lg p-8 text-center">
        <h3 className="text-2xl font-semibold text-gray-900 mb-4">
          Ready to get started?
        </h3>
        <p className="text-gray-600 mb-6">
          Sign in with your Steam account to access your personalized dashboard
        </p>
        <button
          onClick={handleSteamLogin}
          className="inline-flex items-center px-8 py-4 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-semibold rounded-lg transition-all duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
        >
          <svg className="w-6 h-6 mr-3" viewBox="0 0 24 24" fill="currentColor">
            <path d="M11.979 0C5.678 0 .511 4.86.022 11.037l6.432 2.658c.545-.371 1.203-.59 1.912-.59.063 0 .125.004.188.006l2.861-4.142V8.91c0-2.495 2.028-4.524 4.524-4.524 2.494 0 4.524 2.031 4.524 4.527s-2.03 4.525-4.524 4.525h-.105l-4.076 2.911c0 .052.004.105.004.159 0 1.875-1.515 3.396-3.39 3.396-1.635 0-3.016-1.173-3.331-2.727L.436 15.27C1.862 20.307 6.486 24 11.979 24c6.627 0 11.999-5.373 11.999-12S18.605 0 11.979 0zM7.54 18.21l-1.473-.61c.262.543.714.999 1.314 1.25 1.297.539 2.793-.076 3.332-1.375.263-.63.264-1.319.005-1.949s-.75-1.121-1.377-1.383c-.624-.26-1.29-.249-1.878-.03l1.523.63c.956.4 1.409 1.5 1.009 2.455-.397.957-1.497 1.41-2.454 1.012H7.54zm11.415-9.303c0-1.662-1.353-3.015-3.015-3.015-1.665 0-3.015 1.353-3.015 3.015 0 1.665 1.35 3.015 3.015 3.015 1.663 0 3.015-1.35 3.015-3.015zm-5.273-.005c0-1.252 1.013-2.266 2.265-2.266 1.249 0 2.266 1.014 2.266 2.266 0 1.251-1.017 2.265-2.266 2.265-1.253 0-2.265-1.014-2.265-2.265z" />
          </svg>
          Login with Steam
        </button>
        <p className="text-sm text-gray-500 mt-4">
          We only access public Steam profile information
        </p>
      </div>
    </div>
  );
}
