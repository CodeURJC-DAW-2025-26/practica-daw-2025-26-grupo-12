import type { AuthUser } from "~/stores/auth-store";

interface LoginData {
    username: string;
    password: string;
}

interface RegisterData {
    username: string;
    email: string;
    password: string;
}

export async function logUser(credentials: LoginData): Promise<boolean> {
    const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
        credentials: "include",
    });
    return response.ok;
}

export async function registerUser(data: RegisterData): Promise<{ ok: boolean; error?: string }> {
    const response = await fetch("/api/v1/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
        credentials: "include",
    });
    if (response.ok) return { ok: true };
    let error = "Registration failed";
    try {
        const json = await response.json();
        error = json.message ?? error;
    } catch {}
    return { ok: false, error };
}

export async function logoutUser(): Promise<void> {
    await fetch("/api/v1/auth/logout", { method: "POST", credentials: "include" });
}

export async function getMe(): Promise<AuthUser | null> {
    const response = await fetch("/api/v1/users/me", { credentials: "include" });
    if (!response.ok) return null;
    return response.json();
}
