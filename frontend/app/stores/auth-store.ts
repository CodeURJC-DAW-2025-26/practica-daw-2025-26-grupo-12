import { create } from "zustand";
import { userService } from "../services/user-service";

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
    bootstrap: () => Promise<void>;
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

    bootstrap: async () => {
        set({ loading: true });
        try {
            const user = await userService.getMe();
            set({ user, initialized: true });
        } catch (error) {
            set({ user: null, initialized: true });
        } finally {
            set({ loading: false });
        }
    },
}));
