import type { TournamentDetail, TournamentSummary, Page } from "~/types";

const BASE = "/api/v1/tournaments";

export async function getTournaments(
    page = 0,
    size = 10,
    query?: string
): Promise<Page<TournamentSummary>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (query) params.set("query", query);
    const res = await fetch(`${BASE}?${params}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Failed to fetch tournaments: ${res.status}`);
    return res.json();
}

export async function getTournamentById(id: number): Promise<TournamentDetail> {
    const res = await fetch(`${BASE}/${id}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Tournament ${id} not found`);
    return res.json();
}

export async function getMyTournaments(): Promise<Page<TournamentSummary>> {
    const res = await fetch(`${BASE}/my-tournaments`, {
        credentials: "include",
    });
    if (!res.ok) throw new Error("Failed to fetch user tournaments");

    return res.json();
}

export async function joinTournament(tournamentId: number, botId: number): Promise<void> {
    const res = await fetch(`${BASE}/${tournamentId}/join?botId=${botId}`, {
        method: "POST",
        credentials: "include",
    });
    if (!res.ok) {
        const error = await res.json().catch(() => ({}));
        throw new Error(error.message || "Failed to join tournament");
    }
}

export async function getUserTournaments(
    userId: number,
    page = 0,
    size = 10
): Promise<Page<TournamentSummary>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    const res = await fetch(`${BASE}/user/${userId}?${params}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Failed to fetch tournaments for user ${userId}`);
    return res.json();
}
