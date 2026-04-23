import { useEffect, useState } from "react";
import { fetchSessionState, type SessionState } from "~/services/session-service";

interface ResolvedSessionState extends SessionState {
  resolved: boolean;
}

const INITIAL_SESSION_STATE: ResolvedSessionState = {
  logged: false,
  admin: false,
  resolved: false,
};

export function useSessionState(): ResolvedSessionState {
  const [sessionState, setSessionState] = useState(INITIAL_SESSION_STATE);

  useEffect(() => {
    let active = true;

    async function resolveSessionState() {
      const resolvedSession = await fetchSessionState();
      if (!active) {
        return;
      }

      setSessionState({
        ...resolvedSession,
        resolved: true,
      });
    }

    void resolveSessionState();

    return () => {
      active = false;
    };
  }, []);

  return sessionState;
}
