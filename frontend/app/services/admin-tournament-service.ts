import type { TournamentSummary, TournamentDetail, Page } from "~/types";

export interface TournamentCreateData {
    name: string;
    description: string;
    slots: number;
    registrationStarts: string;
    startDate: string;
    price: number;
    imageFile?: File;
}

export interface TournamentUpdateData extends TournamentCreateData {
    status: string;
}

export const adminTournamentService = {
    async getTournaments(page = 0, size = 10, query = ""): Promise<Page<TournamentSummary>> {
        const url = `/api/v1/tournaments?page=${page}&size=${size}&query=${encodeURIComponent(query)}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("Failed to fetch tournaments");
        }
        return await response.json();
    },

    async getTournament(id: string | number): Promise<TournamentDetail> {
        const response = await fetch(`/api/v1/tournaments/${id}`);
        if (!response.ok) {
            throw new Error("Failed to fetch tournament detail");
        }
        return await response.json();
    },

    async createTournament(data: TournamentCreateData): Promise<TournamentDetail> {
        const formData = new FormData();
        const requestBlob = new Blob(
            [
                JSON.stringify({
                    name: data.name,
                    description: data.description,
                    slots: data.slots,
                    registrationStarts: data.registrationStarts,
                    startDate: data.startDate,
                    price: data.price,
                }),
            ],
            { type: "application/json" }
        );

        formData.append("request", requestBlob);
        if (data.imageFile) {
            formData.append("imageFile", data.imageFile);
        }

        const response = await fetch("/api/v1/tournaments", {
            method: "POST",
            body: formData,
        });

        if (!response.ok) {
            throw new Error("Failed to create tournament");
        }
        return await response.json();
    },

    async updateTournament(id: number, data: TournamentUpdateData): Promise<TournamentDetail> {
        const formData = new FormData();
        const requestBlob = new Blob(
            [
                JSON.stringify({
                    name: data.name,
                    description: data.description,
                    status: data.status,
                    slots: data.slots,
                    registrationStarts: data.registrationStarts,
                    startDate: data.startDate,
                    price: data.price,
                }),
            ],
            { type: "application/json" }
        );

        formData.append("request", requestBlob);
        if (data.imageFile) {
            formData.append("imageFile", data.imageFile);
        }

        const response = await fetch(`/api/v1/tournaments/${id}`, {
            method: "PUT",
            body: formData,
        });

        if (!response.ok) {
            throw new Error("Failed to update tournament");
        }
        return await response.json();
    },

    async deleteTournament(id: number): Promise<void> {
        const response = await fetch(`/api/v1/tournaments/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) {
            throw new Error("Failed to delete tournament");
        }
    },
};
