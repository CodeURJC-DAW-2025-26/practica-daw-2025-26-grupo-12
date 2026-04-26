import type { BotSummary, BotDetail } from "~/types";

export interface BotPageResponse {
    content: BotSummary[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const adminBotService = {
    async getBots(page = 0, size = 10, query = ""): Promise<BotPageResponse> {
        const url = `/api/v1/bots?page=${page}&size=${size}&query=${encodeURIComponent(query)}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("Failed to fetch bots");
        }
        return await response.json();
    },

    async getBot(id: string | number): Promise<BotDetail> {
        const response = await fetch(`/api/v1/bots/${id}`);
        if (!response.ok) {
            throw new Error("Failed to fetch bot detail");
        }
        return await response.json();
    },

    async deleteBot(id: number): Promise<void> {
        const response = await fetch(`/api/v1/bots/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) {
            throw new Error("Failed to delete bot");
        }
    },
};
