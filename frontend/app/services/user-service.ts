export interface UserProfileData {
    id: number;
    username: string;
    email: string;
    imageUrl?: string | null;
    roles: string[];
    createdAt: string;
    blocked: boolean;
}

export const userService = {
    async getUserById(id: string | number): Promise<UserProfileData | null> {
        const url = `/api/v1/users/${id}`;
        const response = await fetch(url);

        if (!response.ok) {
            return null;
        }
        return await response.json();
    },

    async getMe(): Promise<UserProfileData> {
        const response = await fetch("/api/v1/users/me");
        if (!response.ok) {
            throw new Error("Failed to fetch user");
        }
        return await response.json();
    },
};
