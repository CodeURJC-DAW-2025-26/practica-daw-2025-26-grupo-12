interface AuthRefreshPayload {
  status?: string;
}

export interface SessionState {
  logged: boolean;
  admin: boolean;
}

const GUEST_SESSION: SessionState = {
  logged: false,
  admin: false,
};

export async function fetchSessionState(): Promise<SessionState> {
  try {
    const refreshResponse = await fetch("/api/v1/auth/refresh", {
      method: "POST",
      credentials: "include",
    });

    if (!refreshResponse.ok) {
      return GUEST_SESSION;
    }

    const refreshPayload = (await refreshResponse.json()) as AuthRefreshPayload;
    if (refreshPayload.status === "FAILURE") {
      return GUEST_SESSION;
    }

    const adminProbe = await fetch("/api/v1/charts/users", {
      credentials: "include",
    });

    return {
      logged: true,
      admin: adminProbe.ok,
    };
  } catch {
    return GUEST_SESSION;
  }
}
