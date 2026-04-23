const TOURNAMENTS_API_PATH = "/api/v1/tournaments";

export const TOURNAMENT_PAGE_SIZE = 10;

export interface TournamentListItem {
  id: number;
  name: string;
  slots: number;
  registered: number;
  status: string;
}

export interface TournamentPageResponse {
  content: TournamentListItem[];
  pageNumber: number;
  totalPages: number;
  totalElements: number;
}

interface TournamentPageRequest {
  query?: string;
  page?: number;
  size?: number;
}

interface ApiErrorPayload {
  message?: string;
}

export interface TournamentStatusMeta {
  label: string;
  badgeClass: string;
  actionLabel: string;
  actionDisabled: boolean;
}

async function parseApiResponse<T>(response: Response, fallbackMessage: string): Promise<T> {
  if (!response.ok) {
    let message = fallbackMessage;

    try {
      const payload = (await response.json()) as ApiErrorPayload;
      if (typeof payload?.message === "string" && payload.message.trim()) {
        message = payload.message;
      }
    } catch {
      message = fallbackMessage;
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

function normalizeStatusLabel(status?: string | null): string {
  const value = (status ?? "").trim();
  if (!value) {
    return "Unknown";
  }

  if (!value.includes("_")) {
    return value;
  }

  return value
    .toLowerCase()
    .split("_")
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join(" ");
}

export function getTournamentStatusMeta(status?: string | null): TournamentStatusMeta {
  const label = normalizeStatusLabel(status);
  const normalized = label.toLowerCase();

  if (normalized.includes("progress")) {
    return {
      label,
      badgeClass: "bg-warning text-dark",
      actionLabel: "In Progress",
      actionDisabled: true,
    };
  }

  if (normalized.includes("complete") || normalized.includes("finish")) {
    return {
      label,
      badgeClass: "bg-success",
      actionLabel: "View Results",
      actionDisabled: false,
    };
  }

  if (normalized.includes("upcoming")) {
    return {
      label,
      badgeClass: "bg-info text-dark",
      actionLabel: "Details",
      actionDisabled: false,
    };
  }

  if (normalized.includes("registration open")) {
    return {
      label,
      badgeClass: "bg-primary",
      actionLabel: "Details",
      actionDisabled: false,
    };
  }

  return {
    label,
    badgeClass: "bg-secondary",
    actionLabel: "View",
    actionDisabled: false,
  };
}

export async function fetchTournamentPage({
  query = "",
  page = 0,
  size = TOURNAMENT_PAGE_SIZE,
}: TournamentPageRequest = {}): Promise<TournamentPageResponse> {
  const params = new URLSearchParams({
    page: String(Math.max(page, 0)),
    size: String(Math.max(size, 1)),
  });

  if (query.trim()) {
    params.set("query", query.trim());
  }

  const response = await fetch(`${TOURNAMENTS_API_PATH}?${params.toString()}`);
  return parseApiResponse<TournamentPageResponse>(
    response,
    "Unable to load tournaments right now.",
  );
}
