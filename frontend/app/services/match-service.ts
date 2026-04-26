import type { MatchSummary, MatchStats, Page } from "~/types";
export interface MatchmakingStatus {
    status: "IDLE" | "SEARCHING" | "FOUND";
    matchId?: number;
}

export interface RecentMatches {
    matches: MatchSummary[];
    totalMatches: number;
}

const BASE = "/api/v1/matches";

export async function getMatches(page = 0, size = 10): Promise<Page<MatchSummary>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    const res = await fetch(`${BASE}?${params}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Failed to fetch matches: ${res.status}`);
    return res.json();
}

export async function getMatchStats(id: number): Promise<MatchStats> {
    const res = await fetch(`${BASE}/${id}/stats`, { credentials: "include" });
    if (!res.ok) throw new Error(`Match stats ${id} not found`);
    return res.json();
}

export async function getRecentMatches(): Promise<RecentMatches> {
    const res = await fetch(`${BASE}/recent`, { credentials: "include" });
    if (!res.ok) throw new Error("Failed to fetch recent matches");
    return res.json();
}

export async function getMatchmakingStatus(): Promise<MatchmakingStatus> {
    const res = await fetch(`${BASE}/matchmaking/status`, { credentials: "include" });
    if (!res.ok) throw new Error("Failed to fetch matchmaking status");
    return res.json();
}

export async function startMatchmaking(botId?: number): Promise<{ success: boolean }> {
    const url = botId ? `${BASE}/matchmaking/start?botId=${botId}` : `${BASE}/matchmaking/start`;
    const res = await fetch(url, { method: "POST", credentials: "include" });
    if (!res.ok) throw new Error("Failed to start matchmaking");
    return res.json();
}

export async function cancelMatchmaking(): Promise<void> {
    const res = await fetch(`${BASE}/matchmaking/cancel`, {
        method: "POST",
        credentials: "include",
    });
    if (!res.ok) throw new Error("Failed to cancel matchmaking");
}

export async function getMatchBattle(id: number): Promise<any> {
    const res = await fetch(`${BASE}/${id}/battle`, { credentials: "include" });
    if (!res.ok) throw new Error("Failed to fetch battle data");
    return res.json();
}
