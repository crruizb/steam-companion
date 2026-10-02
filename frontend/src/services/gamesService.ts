import type { Game, RandomGameFilter, User } from "../types";
import { apiFetch } from "./api";

export const importGamesFromSteam = async (): Promise<void> => {
    await apiFetch("/games/import", { method: "POST" });
}

export const fetchOwnedGames = async (): Promise<User> => {
    const response = await apiFetch("/user/games", { method: "GET" });
    return response.json()
}

/** A random game matching the filter; exclude is the game shown before a reroll. */
export const fetchRandomGame = async ({
    filter,
    exclude,
}: {
    filter: RandomGameFilter;
    exclude?: number;
}): Promise<Game> => {
    const params = new URLSearchParams({ filter });
    if (exclude !== undefined) params.set("exclude", String(exclude));
    const response = await apiFetch(`/games/random?${params}`, { method: "GET" });
    return response.json()
}
