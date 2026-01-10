import { Achievement } from "./index";
export interface User {
  id: number | null;
  steamId: string;
  username: string;
  displayName: string | null;
  avatarUrl: string | null;
  profileUrl: string;
  ownedGames?: Game[] | null;
}

export interface Game {
  appId: number;
  playTimeForeverMinutes: number;
  name: string;
  imgUrl: string | null;
}

export interface AuthResponse {
  accessToken?: string;
  refreshToken?: string;
  user?: User;
  error?: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AchievementsPerDate {
  date: string;
  count: number;
}

export interface AchievementsHeatmap {
  achievementsPerDate: Record<number, AchievementsPerDate[]>;
}
