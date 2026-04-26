import type { BotDetail, Page } from "~/types";

const BASE = "/api/v1/bots";

export async function getBots(page = 0, size = 10, query?: string): Promise<Page<BotDetail>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (query) params.set("query", query);
    const res = await fetch(`${BASE}?${params}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Failed to fetch bots: ${res.status}`);
    return res.json();
}

export async function getBotById(id: number): Promise<BotDetail> {
    const res = await fetch(`${BASE}/${id}`, { credentials: "include" });
    if (!res.ok) throw new Error(`Bot ${id} not found`);
    return res.json();
}
