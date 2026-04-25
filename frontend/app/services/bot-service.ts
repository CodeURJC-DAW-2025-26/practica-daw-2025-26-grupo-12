const BOTS_API_PATH = "/api/v1/bots";

export interface BotListItem {
  id: number;
  name: string;
  description: string | null;
  isPublic: boolean;
  elo: number;
  ownerId: number | null;
  wins: number;
  losses: number;
  draws: number;
  tags: string[];
  hasImage: boolean;
  imageUrl?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface BotPageResponse {
  content: BotListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  first: boolean;
  last: boolean;
}

interface ApiErrorPayload {
  message?: string;
  error?: string;
}

async function parseApiResponse<T>(response: Response, fallbackMessage: string): Promise<T> {
  if (!response.ok) {
    let message = fallbackMessage;

    try {
      const payload = (await response.json()) as ApiErrorPayload;
      if (typeof payload?.message === "string" && payload.message.trim()) {
        message = payload.message;
      } else if (typeof payload?.error === "string" && payload.error.trim()) {
        message = payload.error;
      }
    } catch {
      message = fallbackMessage;
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

export async function fetchBotPage({
  page = 0,
  size = 20,
  query = "",
}: {
  page?: number;
  size?: number;
  query?: string;
} = {}): Promise<BotPageResponse> {
  const params = new URLSearchParams({
    page: String(Math.max(page, 0)),
    size: String(Math.max(size, 1)),
  });

  if (query.trim()) {
    params.set("query", query.trim());
  }

  const response = await fetch(`${BOTS_API_PATH}?${params.toString()}`, {
    credentials: "include",
  });

  return parseApiResponse<BotPageResponse>(response, "Unable to load bots right now.");
}
