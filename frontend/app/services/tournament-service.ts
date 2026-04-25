const TOURNAMENTS_API_PATH = "/api/v1/tournaments";

export const TOURNAMENT_PAGE_SIZE = 10;
export const TOURNAMENT_LOOKUP_PAGE_SIZE = 20;

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

export interface TournamentDetail {
  id: number;
  name: string;
  description: string | null;
  status: string | null;
  slots: number;
  startDate: string | null;
  imageUrl?: string | null;
}

interface TournamentPageRequest {
  query?: string;
  page?: number;
  size?: number;
}

export interface TournamentJoinResult {
  status: string;
  message: string;
}

export interface TournamentMutationData {
  name: string;
  description: string;
  slots: number;
  registrationStarts: string;
  startDate: string;
  price: string;
  status?: string;
  imageFile?: File | null;
}

interface ApiErrorPayload {
  message?: string;
  error?: string;
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

function normalizeStatusLabel(status?: string | null): string {
  const value = (status ?? "").trim();
  if (!value) {
    return "Unknown";
  }

  if (!value.includes("_") && value !== value.toUpperCase()) {
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
      badgeClass: "bg-success",
      actionLabel: "Details",
      actionDisabled: false,
    };
  }

  if (normalized.includes("scheduled")) {
    return {
      label,
      badgeClass: "bg-info text-dark",
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

export function getTournamentActionMeta(status?: string | null): TournamentStatusMeta {
  return getTournamentStatusMeta(status);
}

export function formatTournamentDate(value?: string | null): string {
  if (!value) {
    return "Unknown";
  }

  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "2-digit",
    year: "numeric",
  }).format(date);
}

export function extractRegistrationOpenDate(description?: string | null): string | null {
  const match = (description ?? "").match(/Registration opens:\s*(\d{4}-\d{2}-\d{2})/i);
  return match?.[1] ?? null;
}

export function extractTournamentPrize(description?: string | null): string {
  const match = (description ?? "").match(/Prize:\s*([^]+?)(?:\s+-\s+|$)/i);
  return match?.[1]?.trim() ?? "";
}

export function stripTournamentMetadata(description?: string | null): string {
  return (description ?? "")
    .split(" - ")
    .filter((segment) => {
      const normalized = segment.trim().toLowerCase();
      return (
        !normalized.startsWith("max players:") &&
        !normalized.startsWith("registration opens:") &&
        !normalized.startsWith("prize:")
      );
    })
    .join(" - ")
    .trim();
}

export function extractTournamentFormat(description?: string | null): string {
  const value = description ?? "";
  const markerIndex = value.toLowerCase().indexOf("format:");
  if (markerIndex < 0) {
    return "Unknown";
  }

  const formatStart = markerIndex + "format:".length;
  const formatEnd = value.indexOf(" - ", formatStart);
  const rawFormat = formatEnd >= 0 ? value.slice(formatStart, formatEnd) : value.slice(formatStart);
  const format = rawFormat.trim();
  return format || "Unknown";
}

export function isTournamentRegistrationOpen(
  tournament: TournamentDetail,
  registeredParticipants = 0,
): boolean {
  const status = (tournament.status ?? "").toUpperCase().replace(/\s+/g, "_");
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const startDate = tournament.startDate ? new Date(`${tournament.startDate}T00:00:00`) : null;
  const startsInFuture = startDate !== null && !Number.isNaN(startDate.getTime()) && today < startDate;

  const registrationOpenDateValue = extractRegistrationOpenDate(tournament.description);
  const registrationOpenDate = registrationOpenDateValue
    ? new Date(`${registrationOpenDateValue}T00:00:00`)
    : null;
  const registrationStarted =
    registrationOpenDate === null ||
    Number.isNaN(registrationOpenDate.getTime()) ||
    today >= registrationOpenDate;

  const hasSlots = Math.max(tournament.slots, 0) > registeredParticipants;
  return (
    (status === "UPCOMING" || status === "REGISTRATION_OPEN" || status === "SCHEDULED") &&
    startsInFuture &&
    registrationStarted &&
    hasSlots
  );
}

export function getTournamentAvailabilityMessage(
  tournament: TournamentDetail,
  registeredParticipants = 0,
  logged = false,
  admin = false,
): { message: string; className: string } | null {
  const status = (tournament.status ?? "").toUpperCase().replace(/\s+/g, "_");
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const startDate = tournament.startDate ? new Date(`${tournament.startDate}T00:00:00`) : null;
  const startsInFuture = startDate !== null && !Number.isNaN(startDate.getTime()) && today < startDate;
  const registrationOpenDateValue = extractRegistrationOpenDate(tournament.description);
  const registrationOpenDate = registrationOpenDateValue
    ? new Date(`${registrationOpenDateValue}T00:00:00`)
    : null;
  const registrationStarted =
    registrationOpenDate === null ||
    Number.isNaN(registrationOpenDate.getTime()) ||
    today >= registrationOpenDate;
  const hasSlots = Math.max(tournament.slots, 0) > registeredParticipants;

  if (admin) {
    return null;
  }

  if (status !== "UPCOMING" && status !== "REGISTRATION_OPEN" && status !== "SCHEDULED") {
    return {
      message: "Registration is closed for this tournament.",
      className: "alert-warning",
    };
  }

  if (!startsInFuture) {
    return {
      message: "Registration is closed for this tournament.",
      className: "alert-warning",
    };
  }

  if (!registrationStarted) {
    return {
      message: `Registration opens on ${formatTournamentDate(registrationOpenDateValue)}.`,
      className: "alert-info",
    };
  }

  if (!hasSlots) {
    return {
      message: "Tournament is full.",
      className: "alert-warning",
    };
  }

  if (!logged) {
    return {
      message: "Log in to join this tournament.",
      className: "alert-info",
    };
  }

  return {
    message: "Registration is open. Choose a bot to join.",
    className: "alert-success",
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

  const response = await fetch(`${TOURNAMENTS_API_PATH}?${params.toString()}`, {
    credentials: "include",
  });
  return parseApiResponse<TournamentPageResponse>(
    response,
    "Unable to load tournaments right now.",
  );
}

export async function fetchMyTournamentPage({
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

  const response = await fetch(`${TOURNAMENTS_API_PATH}/my-tournaments?${params.toString()}`, {
    credentials: "include",
  });
  return parseApiResponse<TournamentPageResponse>(
    response,
    "Unable to load your tournaments right now.",
  );
}

export async function fetchTournament(id: number | string): Promise<TournamentDetail> {
  const response = await fetch(`${TOURNAMENTS_API_PATH}/${id}`, {
    credentials: "include",
  });
  return parseApiResponse<TournamentDetail>(response, "The tournament could not be loaded.");
}

export async function fetchTournamentSummaryById(
  id: number | string,
): Promise<TournamentListItem | null> {
  const targetId = Number(id);
  if (!Number.isFinite(targetId)) {
    return null;
  }

  let page = 0;
  let totalPages = 1;

  while (page < totalPages) {
    const pageResponse = await fetchTournamentPage({
      page,
      size: TOURNAMENT_LOOKUP_PAGE_SIZE,
    });
    const tournament = pageResponse.content.find((item) => item.id === targetId);
    if (tournament) {
      return tournament;
    }

    totalPages = pageResponse.totalPages;
    page += 1;
  }

  return null;
}

export async function joinTournament(
  tournamentId: number | string,
  botId: number | string,
): Promise<TournamentJoinResult> {
  const response = await fetch(`${TOURNAMENTS_API_PATH}/join`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({
      tournamentId: Number(tournamentId),
      botId: Number(botId),
    }),
  });

  return parseApiResponse<TournamentJoinResult>(
    response,
    "Unable to join this tournament right now.",
  );
}

function buildTournamentFormData(data: TournamentMutationData): FormData {
  const formData = new FormData();
  const payload = {
    name: data.name,
    description: data.description,
    status: data.status,
    slots: data.slots,
    registrationStarts: data.registrationStarts,
    startDate: data.startDate,
    price: data.price,
  };

  formData.append(
    "request",
    new Blob([JSON.stringify(payload)], {
      type: "application/json",
    }),
  );

  if (data.imageFile) {
    formData.append("imageFile", data.imageFile);
  }

  return formData;
}

export async function createTournament(
  data: TournamentMutationData,
): Promise<TournamentDetail> {
  const response = await fetch(TOURNAMENTS_API_PATH, {
    method: "POST",
    credentials: "include",
    body: buildTournamentFormData(data),
  });

  return parseApiResponse<TournamentDetail>(
    response,
    "Unable to create this tournament right now.",
  );
}

export async function updateTournament(
  id: number | string,
  data: TournamentMutationData,
): Promise<TournamentDetail> {
  const response = await fetch(`${TOURNAMENTS_API_PATH}/${id}`, {
    method: "PUT",
    credentials: "include",
    body: buildTournamentFormData(data),
  });

  return parseApiResponse<TournamentDetail>(
    response,
    "Unable to update this tournament right now.",
  );
}

export async function deleteTournament(id: number | string): Promise<TournamentDetail> {
  const response = await fetch(`${TOURNAMENTS_API_PATH}/${id}`, {
    method: "DELETE",
    credentials: "include",
  });

  return parseApiResponse<TournamentDetail>(
    response,
    "Unable to delete this tournament right now.",
  );
}
