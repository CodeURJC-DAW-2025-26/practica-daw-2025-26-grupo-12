interface LoginData {
  username: string;
  password: string;
}

export async function logUser(loginCredentials: LoginData): Promise<boolean> {
  const response = await fetch("api/v1/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: loginCredentials.username,
      password: loginCredentials.password
    }),
    credentials: "include"
  });

  return response.ok;
}