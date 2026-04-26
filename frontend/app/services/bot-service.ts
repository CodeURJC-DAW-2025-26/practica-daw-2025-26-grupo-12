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

export async function getMyBots(userId: number): Promise<Page<BotDetail>> {
    const res = await fetch(`${BASE}/user/${userId}`, {
        credentials: "include",
    });

    if (!res.ok) throw new Error("Failed to fetch user bots");

    return res.json();
}
export async function createBot(data: {
    name: string;
    description: string;
    tags: string[];
    isPublic: boolean;
    imageFile?: File;
}): Promise<BotDetail> {
    const formData = new FormData();
    formData.append("name", data.name);
    formData.append("description", data.description);
    data.tags.forEach((tag) => formData.append("tags", tag));
    formData.append("public", String(data.isPublic));
    if (data.imageFile) {
        formData.append("imageFile", data.imageFile);
    }

    const res = await fetch(BASE, {
        method: "POST",
        body: formData,
        credentials: "include",
    });
    if (!res.ok) {
        const error = await res.json().catch(() => ({}));
        throw new Error(error.message || "Failed to create bot");
    }
    return res.json();
}

export async function updateBot(
    id: number,
    data: {
        name?: string;
        description?: string;
        code?: string;
        tags?: string[];
        isPublic?: boolean;
        imageFile?: File;
    }
): Promise<BotDetail> {
    const formData = new FormData();
    if (data.name) formData.append("name", data.name);
    if (data.description) formData.append("description", data.description);
    if (data.code) formData.append("code", data.code);
    if (data.tags) data.tags.forEach((tag) => formData.append("tags", tag));
    if (data.isPublic !== undefined) formData.append("public", String(data.isPublic));
    if (data.imageFile) {
        formData.append("imageFile", data.imageFile);
    }

    const res = await fetch(`${BASE}/${id}`, {
        method: "PUT",
        body: formData,
        credentials: "include",
    });
    if (!res.ok) {
        const error = await res.json().catch(() => ({}));
        throw new Error(error.message || "Failed to update bot");
    }
    return res.json();
}

export async function deleteBot(id: number): Promise<void> {
    const res = await fetch(`${BASE}/${id}`, {
        method: "DELETE",
        credentials: "include",
    });
    if (!res.ok) throw new Error("Failed to delete bot");
}