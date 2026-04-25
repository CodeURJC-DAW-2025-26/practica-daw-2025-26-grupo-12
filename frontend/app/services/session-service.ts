interface AuthRefreshPayload {
  status?: string;
}

interface AdminStatusPayload {
  admin?: boolean;
}

export interface SessionState {
  logged: boolean;
  admin: boolean;
}

const SESSION_STORAGE_KEY = "scissors-please-session";

const GUEST_SESSION: SessionState = {
  logged: false,
  admin: false,
};

function readStoredSessionPayload(): unknown {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const storedSession = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    return storedSession ? JSON.parse(storedSession) : null;
  } catch {
    return null;
  }
}

export function getStoredSessionState(): SessionState | null {
  const payload = readStoredSessionPayload();
  if (payload === null || typeof payload !== "object") {
    return null;
  }

  const session = payload as Partial<SessionState>;
  if (typeof session.logged !== "boolean" || typeof session.admin !== "boolean") {
    return null;
  }

  return {
    logged: session.logged,
    admin: session.admin,
  };
}

export function storeSessionState(session: SessionState) {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
  } catch {
    // Session storage is a convenience cache; auth cookies remain the source of truth.
  }
}

export function clearStoredSessionState() {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.sessionStorage.removeItem(SESSION_STORAGE_KEY);
  } catch {
    // Ignore storage errors so logout/session resolution never breaks the UI.
  }
}

export async function fetchAdminStatus(): Promise<boolean> {
  try {
    const response = await fetch("/api/v1/auth/is-admin", {
      credentials: "include",
    });

    if (!response.ok) {
      return false;
    }

    const payload = (await response.json()) as AdminStatusPayload;
    return payload.admin === true;
  } catch {
    return false;
  }
}

export async function fetchSessionState(): Promise<SessionState> {
  try {
    const refreshResponse = await fetch("/api/v1/auth/refresh", {
      method: "POST",
      credentials: "include",
    });

    if (!refreshResponse.ok) {
      clearStoredSessionState();
      return GUEST_SESSION;
    }

    const refreshPayload = (await refreshResponse.json()) as AuthRefreshPayload;
    if (refreshPayload.status === "FAILURE") {
      clearStoredSessionState();
      return GUEST_SESSION;
    }

    const session = {
      logged: true,
      admin: await fetchAdminStatus(),
    };

    storeSessionState(session);
    return session;
  } catch {
    clearStoredSessionState();
    return GUEST_SESSION;
  }
}
