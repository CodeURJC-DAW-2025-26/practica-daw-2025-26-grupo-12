import type { MatchSummary, MatchStats, Page } from "~/types";

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
