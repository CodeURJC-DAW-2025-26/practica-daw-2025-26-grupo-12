interface LoginData {
  username: string;
  password: string;
}

interface RegisterData {
  email: string;
  username: string;
  password: string;
}

interface AuthPayload {
  message?: string;
  error?: string;
}

interface ExceptionPayload {
  message?: string;
}

export interface AuthResult {
  ok: boolean;
  message: string;
}

async function readAuthMessage(response: Response, fallbackMessage: string): Promise<string> {
  try {
    const payload = (await response.json()) as AuthPayload | ExceptionPayload;
    if (typeof payload?.message === "string" && payload.message.trim()) {
      return payload.message;
    }
    if ("error" in (payload ?? {}) && typeof payload.error === "string" && payload.error.trim()) {
      return payload.error;
    }
  } catch {
    return fallbackMessage;
  }

  return fallbackMessage;
}

export async function logUser(loginCredentials: LoginData): Promise<boolean> {
  const response = await fetch("/api/v1/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username: loginCredentials.username,
      password: loginCredentials.password,
    }),
    credentials: "include",
  });

  return response.ok;
}

export async function registerUser(registerData: RegisterData): Promise<AuthResult> {
  const response = await fetch("/api/v1/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email: registerData.email,
      username: registerData.username,
      password: registerData.password,
    }),
    credentials: "include",
  });

  return {
    ok: response.ok,
    message: await readAuthMessage(
      response,
      response.ok ? "Account created successfully." : "Unable to create your account right now.",
    ),
  };
}

export async function logoutUser(): Promise<boolean> {
  const response = await fetch("/api/v1/auth/logout", {
    method: "POST",
    credentials: "include",
  });

  return response.ok;
}
