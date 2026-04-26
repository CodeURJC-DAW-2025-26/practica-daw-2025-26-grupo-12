const BASE = "/api/v1/charts";

async function handleResponse(res: Response) {
    if (!res.ok) {
        throw new Error(`Chart API error: ${res.status}`);
    }
    return res.json();
}

export async function getResultsChart(params: {
    wins: number;
    losses: number;
    draws: number;
}) {
    const search = new URLSearchParams({
        wins: String(params.wins),
        losses: String(params.losses),
        draws: String(params.draws),
    });

    const res = await fetch(`${BASE}/results?${search.toString()}`, {
        method: "GET",
        credentials: "include",
    });

    return handleResponse(res);
}

export async function getEloChart(eloHistory: number[]) {
    const res = await fetch(`${BASE}/elo`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(eloHistory),
    });

    return handleResponse(res);
}

export async function getUsersChart(monthlyData: {
    year: number;
    month: number;
    count: number;
}[]) {
    const res = await fetch(`${BASE}/users`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(monthlyData),
    });

    return handleResponse(res);
}

export async function getProgressChart(params: {
    current: number;
    max: number;
}) {
    const search = new URLSearchParams({
        current: String(params.current),
        max: String(params.max),
    });

    const res = await fetch(`${BASE}/progress?${search.toString()}`, {
        method: "GET",
        credentials: "include",
    });

    return handleResponse(res);
}