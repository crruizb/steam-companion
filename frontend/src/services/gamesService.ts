import config from "../config";

export const importGamesFromSteam = async () => {
    const response = await fetch(`${config.API_BASE_URL}/games/import`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    });

    if (!response.ok) {
        // The backend returns { message } for known failures, e.g. a private Steam profile
        const body = await response.json().catch(() => null)
        throw new Error(body?.message ?? "Could not import games. Please try again.")
    }
}

export const fetchOwnedGames = async () => {
    const response = await fetch(`${config.API_BASE_URL}/user/games`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    });

    if (!response.ok) {
        throw new Error(`Failed to fetch owned games: ${response.status}`)
    }

    const data = await response.json()
    return data
}