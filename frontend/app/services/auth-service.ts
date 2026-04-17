interface LoginData {
  username: string;
  password: string;
}
interface RegisterData{
  username: string;
  password: string;
  email:string;
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

export async function registerUser(registerCredentials: RegisterData):Promise<Response> {
  const response = await fetch("api/v1/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: registerCredentials.username,
      password: registerCredentials.password,
      email:registerCredentials.email
    }),
    credentials: "include"
  });
  return response;
}