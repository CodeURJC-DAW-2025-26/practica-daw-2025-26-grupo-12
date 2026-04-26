export interface UserSummary {
    id: number;
    username: string;
    imageUrl?: string | null;
    blocked: boolean;
}

export interface UserDetail extends UserSummary {
    email: string;
    createdAt: string;
    roles: string[];
}

export interface BotSummary {
    id: number;
    name: string;
    elo: number;
    imageUrl?: string | null;
    ownerUsername?: string;
    ownerId: number;
    public: boolean;
}

export interface BotDetail extends BotSummary {
    description?: string;
    code?: string;
    tags?: string[];
    wins: number;
    losses: number;
    draws: number;
    createdAt: string;
}

export interface TournamentSummary {
    id: number;
    name: string;
    slots: number;
    participants: number;
    status: string;
    startDate: string;
    imageUrl?: string | null;
}

export interface TournamentDetail extends TournamentSummary {
    description?: string;
}

export interface MatchSummary {
    id: number;
    bot1Id: number;
    bot1Name: string;
    bot2Id: number;
    bot2Name: string;
    bot1OwnerName: string;
    bot2OwnerName: string;
    winnerBotId?: number | null;
    maxElo: number;
    playedAt: string;
}

export interface RoundDetail {
    roundNumber: number;
    bot1Move: string;
    bot2Move: string;
    result: string;
}

export interface MatchStats {
    matchId: number;
    bot1Id: number;
    bot1Name: string;
    bot2Id: number;
    bot2Name: string;
    bot1OwnerName: string;
    bot2OwnerName: string;
    bot1Score: number;
    bot2Score: number;
    totalRounds: number;
    winnerId?: number | null;
    winnerName?: string | null;
    playedAt: string;
    rounds: RoundDetail[];
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    last: boolean;
}
