export interface UserProfileData {
    id: number;
    username: string;
    email: string;
    imageUrl?: string;
    createdAt: string;
    blocked: boolean;
}

export interface UserUpdateRequest {
    username?: string;
    email?: string;
    password?: string;
}

export async function getUserById(id: number | string): Promise<UserProfileData | null> {
    const url = `/api/v1/users/${id}`;
    const response = await fetch(url, { credentials: "include" });

    if (!response.ok) {
        return null;
    }
    return response.json();
}

export async function updateUserProfile(
    id: number,
    data: UserUpdateRequest,
    imageFile?: File
): Promise<{ ok: boolean; error?: string; user?: UserProfileData }> {
    const formData = new FormData();
    formData.append("request", new Blob([JSON.stringify(data)], { type: "application/json" }));
    if (imageFile) {
        formData.append("imageFile", imageFile);
    }

    const response = await fetch(`/api/v1/users/${id}`, {
        method: "PUT",
        body: formData,
        credentials: "include",
    });

    if (response.ok) {
        return { ok: true, user: await response.json() };
    }

    let error = "Failed to update profile";
    try {
        const json = await response.json();
        error = json.message ?? error;
    } catch {}
    return { ok: false, error };
}

export async function blockUser(id: number): Promise<boolean> {
    const response = await fetch(`/api/v1/users/${id}/block`, {
        method: "PUT",
        credentials: "include",
    });
    return response.ok;
}

export async function unblockUser(id: number): Promise<boolean> {
    const response = await fetch(`/api/v1/users/${id}/unblock`, {
        method: "PUT",
        credentials: "include",
    });
    return response.ok;
}

export async function deleteUser(id: number): Promise<boolean> {
    const response = await fetch(`/api/v1/users/${id}`, {
        method: "DELETE",
        credentials: "include",
    });
    return response.ok;
}
