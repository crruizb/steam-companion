import { apiFetch } from "./api";

export const importGamesFromSteam = async () => {
    const response = await apiFetch("/games/import", { method: "POST" });

    if (!response.ok) {
        // The backend returns { message } for known failures, e.g. a private Steam profile
        const body = await response.json().catch(() => null)
        throw new Error(body?.message ?? "Could not import games. Please try again.")
    }
}

export const fetchOwnedGames = async () => {
    const response = await apiFetch("/user/games", { method: "GET" });

    if (!response.ok) {
        throw new Error(`Failed to fetch owned games: ${response.status}`)
    }

    const data = await response.json()
    return data
}