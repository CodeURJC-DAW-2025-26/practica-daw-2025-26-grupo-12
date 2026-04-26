import type { UserProfileData } from "./user-service";

export interface UserPageResponse {
    content: UserProfileData[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const adminUserService = {
    async getUsers(page = 0, size = 10, query = ""): Promise<UserPageResponse> {
        const url = `/api/v1/users?page=${page}&size=${size}&query=${encodeURIComponent(query)}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("Failed to fetch users");
        }
        return await response.json();
    },

    async blockUser(id: number): Promise<UserProfileData> {
        const response = await fetch(`/api/v1/users/${id}/block`, {
            method: "PUT",
        });
        if (!response.ok) {
            throw new Error("Failed to block user");
        }
        return await response.json();
    },

    async unblockUser(id: number): Promise<UserProfileData> {
        const response = await fetch(`/api/v1/users/${id}/unblock`, {
            method: "PUT",
        });
        if (!response.ok) {
            throw new Error("Failed to unblock user");
        }
        return await response.json();
    },

    async deleteUser(id: number): Promise<void> {
        const response = await fetch(`/api/v1/users/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) {
            throw new Error("Failed to delete user");
        }
    },
};
