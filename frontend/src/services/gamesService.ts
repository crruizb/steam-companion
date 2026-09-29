import type { Game, User } from "../types";
import { apiFetch } from "./api";

export const importGamesFromSteam = async (): Promise<void> => {
    await apiFetch("/games/import", { method: "POST" });
}

export const fetchOwnedGames = async (): Promise<User> => {
    const response = await apiFetch("/user/games", { method: "GET" });
    return response.json()
}

export const fetchRandomGame = async (): Promise<Game> => {
    const response = await apiFetch("/games/random", { method: "GET" });
    return response.json()
}
