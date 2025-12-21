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
        throw new Error(`Failed to import games: ${response.status}`)
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