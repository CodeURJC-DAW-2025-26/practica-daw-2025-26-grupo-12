import { useEffect, useState } from "react";
import { Link, useLoaderData, useNavigate } from "react-router";
import { Container, Row, Col, Card, Button, Badge, Modal } from "react-bootstrap";
import type { Route } from "./+types/bot-detail";
import { getBotById, deleteBot } from "~/services/bot-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";
import { useAuthStore } from "~/stores/auth-store";

export function meta({ data }: Route.MetaArgs) {
    const name = (data as any)?.bot?.name ?? "Bot";
    return [
        { title: `${name} – Scissors, Please` },
        { name: "description", content: `Details and statistics for bot ${name}.` },
    ];
}

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const id = Number(params.id);
    if (isNaN(id)) throw new Error("Invalid bot ID");
    const bot = await getBotById(id);
    return { bot };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Bot not found.";
    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 text-center">
                <h1 className="error-code">404</h1>
                <p className="text-secondary">{msg}</p>
                <Button as={Link as any} to="/" variant="primary">
                    Go Home
                </Button>
            </Container>
            <Footer />
        </div>
    );
}

export default function BotDetail() {
    const { bot } = useLoaderData<typeof clientLoader>();
    const { user } = useAuthStore();
    const navigate = useNavigate();
    const [showDelete, setShowDelete] = useState(false);
    const [loading, setLoading] = useState(false);

    const isOwner = user?.id === bot.ownerId;
    const canManage = isOwner || useAuthStore.getState().isAdmin();
    const initial = bot.name.charAt(0).toUpperCase();
    const totalMatches = bot.wins + bot.losses + bot.draws;
    const winRate = totalMatches > 0 ? ((bot.wins / totalMatches) * 100).toFixed(1) : "0.0";

    const [pieChart, setPieChart] = useState<string>("");
    const [eloChart, setEloChart] = useState<string>("");

    useEffect(() => {
        const fetchPieChart = async () => {
            try {
                const res = await fetch(
                    `/api/v1/charts/results?wins=${bot.wins}&losses=${bot.losses}&draws=${bot.draws}`
                );
                if (res.ok) {
                    const text = await res.text();
                    setPieChart(text);
                }
            } catch (e) {
                console.error("Failed to fetch pie chart", e);
            }
        };

        const fetchEloChart = async () => {
            try {
                const eloHistory = [1000, 1020, 1050, bot.elo];
                const res = await fetch(`/api/v1/charts/elo`, {
                    method: "GET",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(eloHistory),
                });
                if (res.ok) {
                    const text = await res.text();
                    setEloChart(text);
                }
            } catch (e) {
                console.error("Failed to fetch elo chart", e);
            }
        };

        fetchPieChart();
        fetchEloChart();
    }, [bot]);

    const handleDelete = async () => {
        setLoading(true);
        try {
            await deleteBot(bot.id);
            navigate("/bots/user-bots");
        } catch (err) {
            alert("Failed to delete bot");
        } finally {
            setLoading(false);
            setShowDelete(false);
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <Row className="mb-5 align-items-end justify-content-between">
                    <Col md="auto" className="d-flex align-items-end gap-3 flex-wrap">
                        {bot.imageUrl ? (
                            <img
                                src={`/api/v1/images/bots/${bot.id}`}
                                className="rounded-circle border border-primary"
                                style={{ width: 100, height: 100, objectFit: "cover" }}
                                alt={bot.name}
                            />
                        ) : (
                            <div
                                className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white display-4 fw-bold"
                                style={{ width: 100, height: 100 }}
                            >
                                {initial}
                            </div>
                        )}
                        <div>
                            <h1 className="h2 fw-bold mb-1">{bot.name}</h1>
                            <p className="text-secondary mb-1">
                                Owner:{" "}
                                <span className="text-white fw-semibold hover-link">
                                    {bot.ownerUsername}
                                </span>
                            </p>
                            <p className="text-secondary mb-2">Created: {bot.createdAt}</p>
                            <p className="text-secondary mb-0">{bot.description}</p>
                            {bot.tags && bot.tags.length > 0 && (
                                <div className="mt-2 d-flex flex-wrap gap-1">
                                    {bot.tags.map((tag) => (
                                        <Badge key={tag} bg="secondary" className="rounded-pill">
                                            {tag}
                                        </Badge>
                                    ))}
                                </div>
                            )}
                        </div>
                    </Col>
                    <Col md="auto" className="mt-3 mt-md-0 d-flex flex-wrap gap-2">
                        {canManage && (
                            <>
                                <Button
                                    as={Link as any}
                                    to={`/bots/${bot.id}/edit`}
                                    className="btn-outline-muted"
                                    variant="outline"
                                >
                                    Edit Bot
                                </Button>
                                <Button
                                    variant="outline-danger"
                                    onClick={() => setShowDelete(true)}
                                >
                                    Delete
                                </Button>
                            </>
                        )}
                        {isOwner && (
                            <Button as={Link as any} to="/matches/search" variant="primary">
                                Find Match
                            </Button>
                        )}
                    </Col>
                </Row>

                <div className="row g-4 mb-5">
                    <div className="col-lg-8">
                        {bot.code && isOwner && (
                            <Card className="p-4 h-100 mb-4">
                                <h3 className="h6 fw-bold mb-3">Bot Logic (Python)</h3>
                                <div
                                    className="bg-black border border-secondary rounded p-3 overflow-auto"
                                    style={{ maxHeight: 400 }}
                                >
                                    <pre
                                        className="font-monospace text-info mb-0"
                                        style={{ fontSize: "0.9rem" }}
                                    >
                                        <code>{bot.code}</code>
                                    </pre>
                                </div>
                            </Card>
                        )}

                        <div className="row g-4">
                            <div className="col-md-4">
                                <Card className="p-3 stat-card h-100">
                                    <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                        Global Rank
                                    </h6>
                                    <div className="d-flex align-items-baseline">
                                        <span className="h2 fw-bold mb-0 text-white">#?</span>
                                    </div>
                                </Card>
                            </div>
                            <div className="col-md-4">
                                <Card className="p-3 stat-card h-100">
                                    <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                        ELO Rating
                                    </h6>
                                    <div className="d-flex align-items-baseline">
                                        <span className="h2 fw-bold mb-0 text-white">
                                            {bot.elo}
                                        </span>
                                    </div>
                                </Card>
                            </div>
                            <div className="col-md-4">
                                <Card className="p-3 stat-card h-100">
                                    <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                        Win Rate
                                    </h6>
                                    <div className="d-flex align-items-baseline">
                                        <span className="h2 fw-bold mb-0 text-success">
                                            {winRate}%
                                        </span>
                                        <span className="text-secondary small ms-2">
                                            {totalMatches}
                                        </span>
                                    </div>
                                </Card>
                            </div>
                        </div>
                    </div>

                    <div className="col-lg-4">
                        <Card className="p-4 h-100">
                            <div className="d-flex justify-content-between align-items-center mb-3">
                                <h3 className="h6 fw-bold mb-0">Results Breakdown</h3>
                                <Badge bg="secondary">Total: {totalMatches}</Badge>
                            </div>
                            <div className="bg-secondary-bg border border-secondary rounded p-4 text-center">
                                {pieChart && (
                                    <img
                                        src={`data:image/png;base64,${pieChart}`}
                                        className="img-fluid mb-3"
                                        style={{ maxHeight: 180 }}
                                        alt="Results Breakdown"
                                    />
                                )}
                                <div className="d-flex flex-wrap justify-content-center gap-2">
                                    <Badge bg="success">Wins: {bot.wins}</Badge>
                                    <Badge bg="danger">Losses: {bot.losses}</Badge>
                                    <Badge bg="secondary">Draws: {bot.draws}</Badge>
                                </div>
                            </div>
                        </Card>
                    </div>
                </div>

                <div className="row g-4 mb-5">
                    <div className="col-lg-12">
                        <Card className="p-4 h-100">
                            <div className="d-flex justify-content-between align-items-center mb-3">
                                <h3 className="h6 fw-bold mb-0">ELO Progression</h3>
                                <Badge bg="secondary">Live History</Badge>
                            </div>
                            <div className="bg-secondary-bg border border-secondary rounded p-4 text-center">
                                {eloChart && (
                                    <img
                                        src={`data:image/png;base64,${eloChart}`}
                                        className="img-fluid mb-3"
                                        style={{ maxHeight: 250 }}
                                        alt="ELO Progression"
                                    />
                                )}
                                <div className="d-flex flex-wrap justify-content-center gap-2">
                                    <Badge bg="secondary">Start: 1000</Badge>
                                    <Badge bg="primary">Current: {bot.elo}</Badge>
                                    <Badge bg="success">Trend: +</Badge>
                                </div>
                                <div className="d-flex flex-wrap justify-content-center gap-3 mt-3 text-secondary small">
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <span
                                            className="rounded-circle"
                                            style={{
                                                width: 10,
                                                height: 10,
                                                backgroundColor: "#8b5cf6",
                                            }}
                                        ></span>{" "}
                                        ELO Line
                                    </span>
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <span
                                            className="rounded-circle"
                                            style={{
                                                width: 10,
                                                height: 10,
                                                backgroundColor: "#22c55e",
                                            }}
                                        ></span>{" "}
                                        Current
                                    </span>
                                </div>
                            </div>
                        </Card>
                    </div>
                </div>

                <Modal show={showDelete} onHide={() => setShowDelete(false)} centered>
                    <Modal.Header closeButton className="bg-dark border-secondary text-white">
                        <Modal.Title>Delete Bot</Modal.Title>
                    </Modal.Header>
                    <Modal.Body className="bg-dark text-white">
                        Are you sure you want to delete <strong>{bot.name}</strong>? This action
                        cannot be undone.
                    </Modal.Body>
                    <Modal.Footer className="bg-dark border-secondary">
                        <Button variant="secondary" onClick={() => setShowDelete(false)}>
                            Cancel
                        </Button>
                        <Button variant="danger" onClick={handleDelete} disabled={loading}>
                            {loading ? "Deleting..." : "Delete Bot"}
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Container>
            <Footer />
        </div>
    );
}
