import { create } from "zustand";

export interface AuthUser {
    id: number;
    username: string;
    email: string;
    roles: string[];
    imageUrl?: string | null;
}

interface AuthState {
    user: AuthUser | null;
    loading: boolean;
    initialized: boolean;

    setUser: (user: AuthUser | null) => void;
    setLoading: (loading: boolean) => void;
    setInitialized: (initialized: boolean) => void;

    isAdmin: () => boolean;
    isLoggedIn: () => boolean;
    logout: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
    user: null,
    loading: true,
    initialized: false,

    setUser: (user) => set({ user }),
    setLoading: (loading) => set({ loading }),
    setInitialized: (initialized) => set({ initialized }),

    isAdmin: () => {
        const { user } = get();
        return user?.roles?.includes("ADMIN") ?? false;
    },

    isLoggedIn: () => {
        return get().user !== null;
    },

    logout: () => {
        set({ user: null });
    },
}));
