import { useState, useEffect } from "react";
import { useNavigate, Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Form, Alert, Spinner } from "react-bootstrap";
import type { Route } from "./+types/matchmaking-search";
import { getMe } from "~/services/auth-service";
import { getMyBots } from "~/services/bot-service";
import {
    startMatchmaking,
    getMatchmakingStatus,
    cancelMatchmaking,
} from "~/services/match-service";
import type { BotDetail } from "~/types";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Find Match - Scissors, Please" },
        { name: "description", content: "Search for an opponent in the matchmaking arena." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    const botPage = await getMyBots(user.id);
    return { user, bots: botPage.content };
}

export default function MatchmakingSearch() {
    const { bots } = useLoaderData<typeof clientLoader>();
    const navigate = useNavigate();

    const [selectedBotId, setSelectedBotId] = useState<number | "">(
        bots.length > 0 ? bots[0].id : ""
    );
    const [searching, setSearching] = useState(false);
    const [error, setError] = useState("");
    const [matchId, setMatchId] = useState<number | null>(null);

    // Initial check for searching status
    useEffect(() => {
        let isMounted = true;
        const checkStatus = async () => {
            try {
                const status = await getMatchmakingStatus();
                if (status.status === "SEARCHING" && isMounted) {
                    setSearching(true);
                } else if (status.status === "FOUND" && status.matchId && isMounted) {
                    navigate(`/matches/battle/${status.matchId}`);
                }
            } catch (err) {}
        };
        checkStatus();
        return () => {
            isMounted = false;
        };
    }, []);

    // Polling while searching
    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (searching && !matchId) {
            interval = setInterval(async () => {
                try {
                    const status = await getMatchmakingStatus();
                    if (status.status === "FOUND" && status.matchId) {
                        setMatchId(status.matchId);
                        setSearching(false);
                        clearInterval(interval);
                        navigate(`/matches/battle/${status.matchId}`);
                    }
                } catch (err) {
                    console.error("Status check failed", err);
                }
            }, 2000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [searching, matchId, navigate]);

    const handleStart = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        try {
            await startMatchmaking(selectedBotId ? Number(selectedBotId) : undefined);
            setSearching(true);
        } catch (err: any) {
            setError(err.message || "Failed to start matchmaking.");
        }
    };

    const handleCancel = async () => {
        try {
            await cancelMatchmaking();
            setSearching(false);
        } catch (err: any) {
            setError(err.message || "Failed to cancel matchmaking.");
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 flex-grow-1 d-flex align-items-center justify-content-center">
                <Col lg={6} md={8}>
                    <Card className="p-5 glass-card border-0 text-center">
                        {!searching ? (
                            <>
                                <div className="mb-4">
                                    <div
                                        className="mx-auto mb-3 d-flex align-items-center justify-content-center bg-primary text-white rounded-circle"
                                        style={{ width: 80, height: 80 }}
                                    >
                                        <span className="fs-1">⚔️</span>
                                    </div>
                                    <h1 className="h3 fw-bold mb-2">Arena Matchmaking</h1>
                                    <p className="text-secondary">
                                        Choose your bot and find a suitable opponent.
                                    </p>
                                </div>

                                {error && (
                                    <Alert variant="danger" className="mb-4">
                                        {error}
                                    </Alert>
                                )}

                                {bots.length > 0 ? (
                                    <Form onSubmit={handleStart}>
                                        <Form.Group className="mb-4" controlId="botSelect">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                Select Bot
                                            </Form.Label>
                                            <Form.Select
                                                value={selectedBotId}
                                                onChange={(e) =>
                                                    setSelectedBotId(e.target.value as any)
                                                }
                                                className="form-select-lg bg-dark text-white border-secondary"
                                            >
                                                {bots.map((bot) => (
                                                    <option key={bot.id} value={bot.id}>
                                                        {bot.name} (ELO: {bot.elo})
                                                    </option>
                                                ))}
                                                <option value="">No Bot (Random/Default)</option>
                                            </Form.Select>
                                        </Form.Group>

                                        <div className="d-grid">
                                            <Button variant="primary" size="lg" type="submit">
                                                Find Opponent
                                            </Button>
                                        </div>
                                    </Form>
                                ) : (
                                    <div className="py-4">
                                        <p className="text-warning mb-4">
                                            You need at least one bot to compete.
                                        </p>
                                        <Button
                                            as={Link as any}
                                            to="/bots/create"
                                            variant="primary"
                                        >
                                            Create My First Bot
                                        </Button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="py-5">
                                <div className="radar-container mb-5 mx-auto">
                                    <div className="radar"></div>
                                    <div className="radar-scanner"></div>
                                    <div className="radar-inner">
                                        <Spinner
                                            animation="border"
                                            variant="primary"
                                            style={{ width: "3rem", height: "3rem" }}
                                        />
                                    </div>
                                </div>

                                <h2 className="h4 fw-bold mb-3 text-white">
                                    Searching for an opponent...
                                </h2>
                                <p className="text-secondary mb-5">
                                    Our arena masters are looking for a worthy challenger for{" "}
                                    <span className="text-primary fw-bold">
                                        {bots.find((b) => b.id === Number(selectedBotId))?.name ||
                                            "your champion"}
                                    </span>
                                    .
                                </p>

                                <Button
                                    variant="outline-danger"
                                    onClick={handleCancel}
                                    className="px-5 rounded-pill"
                                >
                                    Cancel Search
                                </Button>
                            </div>
                        )}
                    </Card>
                </Col>
            </Container>
            <Footer />
        </div>
    );
}
