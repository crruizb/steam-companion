import type { Game } from "../types";
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

export const fetchRandomGame = async (): Promise<Game> => {
    const response = await apiFetch("/games/random", { method: "GET" });

    if (response.status === 404) {
        throw new Error("Import your games first, then we can pick one for you.")
    }
    if (!response.ok) {
        throw new Error("Could not pick a random game. Please try again.")
    }

    return response.json()
}
