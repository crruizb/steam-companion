import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { authKeys } from "../hooks/useAuth.ts";
import { storeUser } from "../services/authService.ts";

function CallbackPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading"
  );
  const [error, setError] = useState<string>("");
  const queryClient = useQueryClient();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get the authentication result from URL parameters
        const success = searchParams.get("success") === "true";
        const errorMessage = searchParams.get("error");

        if (!success || errorMessage) {
          throw new Error(errorMessage || "Authentication failed");
        }

        // Get user data from URL parameters
        const userParam = searchParams.get("user");
        if (userParam) {
          try {
            const userData = JSON.parse(decodeURIComponent(userParam));
            storeUser(userData);
          } catch (parseError) {
            console.error("Failed to parse user data:", parseError);
            throw new Error("Invalid user data received");
          }
        } else {
          throw new Error("No user data received");
        }

        setStatus("success");

        // Invalidate auth queries to trigger fresh fetch with new user data
        queryClient.invalidateQueries({ queryKey: authKeys.all });

        // Redirect to home page after successful login
        setTimeout(() => {
          navigate("/");
        }, 2000);
      } catch (err) {
        console.error("Authentication error:", err);
        setError(err instanceof Error ? err.message : "Unknown error occurred");
        setStatus("error");

        // Redirect to home page after error
        setTimeout(() => {
          navigate("/");
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, navigate]);

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow p-8 max-w-md w-full mx-4">
        {status === "success" && (
          <div className="text-center">
            <div className="text-green-500 mb-4">
              <svg
                className="w-12 h-12 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">
              Login Successful!
            </h2>
            <p className="text-gray-600">Redirecting you to the dashboard...</p>
          </div>
        )}

        {status === "error" && (
          <div className="text-center">
            <div className="text-red-500 mb-4">
              <svg
                className="w-12 h-12 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">
              Authentication Failed
            </h2>
            <p className="text-red-600 mb-4">{error}</p>
            <p className="text-gray-600">
              Redirecting you back to the home page...
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

export default CallbackPage;
