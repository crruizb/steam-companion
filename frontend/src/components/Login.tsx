import {initiateSteamLogin} from "../services/authService.ts";

export default function Login() {
    const handleSteamLogin = () => {
        initiateSteamLogin();
    };

    return <div className="text-center">
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
}