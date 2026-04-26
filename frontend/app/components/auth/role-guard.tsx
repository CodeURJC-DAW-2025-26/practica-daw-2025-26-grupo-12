import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import { useAuthStore } from "../../stores/auth-store";

interface RoleGuardProps {
    children: ReactNode;
    allowedRoles?: string[];
    requireAdmin?: boolean;
}

export function RoleGuard({ children, allowedRoles, requireAdmin = false }: RoleGuardProps) {
    const user = useAuthStore((state) => state.user);
    const initialized = useAuthStore((state) => state.initialized);
    const location = useLocation();

    if (!initialized) {
        return null;
    }

    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requireAdmin && !user.roles.includes("ADMIN")) {
        return <Navigate to="/" replace />;
    }

    if (allowedRoles && !allowedRoles.some((role) => user.roles.includes(role))) {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
}
