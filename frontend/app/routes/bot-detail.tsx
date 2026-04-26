import { useState } from "react";
import { Link, useLoaderData, useNavigate } from "react-router";
import { Container, Row, Col, Card, Button, Badge, Modal } from "react-bootstrap";
import type { Route } from "./+types/bot-detail";
import { getBotById, deleteBot } from "~/services/bot-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";
import { useAuthStore } from "~/stores/auth-store";
import Chart from "~/components/chart";

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

    const handleDelete = async () => {
        setLoading(true);
        try {
            await deleteBot(bot.id);
            navigate("/bots/user-bots");
        } catch (err) {
            alert("Failed to delete bot");
        } finally {
            setLoading(false);
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
                                className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                style={{ width: 100, height: 100, fontSize: "2.5rem" }}
                            >
                                {initial}
                            </div>
                        )}
                        <div>
                            <h1 className="h2 fw-bold mb-1">{bot.name}</h1>
                            <p className="text-secondary mb-1">
                                Owner:{" "}
                                <span className="text-white fw-semibold">{bot.ownerUsername}</span>
                            </p>
                            <p className="text-secondary mb-2">Created: {bot.createdAt}</p>
                            {bot.description && (
                                <p className="text-secondary mb-0">{bot.description}</p>
                            )}
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
                                    variant="outline-secondary"
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

                <Row className="g-4 mb-5">
                    <Col lg={8}>
                        {bot.code && isOwner && (
                            <Card className="glass-card mb-4 border-0 p-4">
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

                        <Card className="glass-card border-0 p-4">
                            <h3 className="h6 fw-bold mb-4">ELO Progress</h3>
                            {bot.eloHistory && bot.eloHistory.length > 1 ? (
                                <Chart type="elo" body={bot.eloHistory} />
                            ) : (
                                <div className="text-center py-5 text-secondary">
                                    Not enough data to generate ELO chart.
                                </div>
                            )}
                        </Card>
                    </Col>

                    <Col lg={4}>
                        <Card className="p-4 glass-card border-0 mb-4 h-100">
                            <h3 className="h6 fw-bold mb-4">Battle Stats</h3>
                            <div className="text-center mb-4">
                                <Chart
                                    type="results"
                                    params={{ wins: bot.wins, losses: bot.losses, draws: bot.draws }}
                                />
                            </div>
                            <div className="d-flex justify-content-between mb-3">
                                <span className="text-secondary small">Current ELO</span>
                                <span className="fw-bold text-white">{bot.elo}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-3">
                                <span className="text-secondary small">Win Rate</span>
                                <span className="fw-bold text-success">{winRate}%</span>
                            </div>
                            <hr className="border-secondary opacity-25" />
                            <div className="d-flex justify-content-between pt-2">
                                <Badge bg="success">W: {bot.wins}</Badge>
                                <Badge bg="danger">L: {bot.losses}</Badge>
                                <Badge bg="secondary">D: {bot.draws}</Badge>
                            </div>
                        </Card>
                    </Col>
                </Row>

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