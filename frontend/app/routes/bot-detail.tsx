import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Badge } from "react-bootstrap";
import type { Route } from "./+types/bot-detail";
import { getBotById } from "~/services/bot-service";
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
    const isOwner = user?.id === bot.ownerId;
    const canManage = isOwner || useAuthStore.getState().isAdmin();
    const initial = bot.name.charAt(0).toUpperCase();
    const totalMatches = bot.wins + bot.losses + bot.draws;
    const winRate = totalMatches > 0 ? ((bot.wins / totalMatches) * 100).toFixed(1) : "0.0";

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <Row className="mb-5 align-items-end">
                    <Col md="auto">
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
                    </Col>
                    <Col>
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
                    </Col>
                    <Col md="auto" className="mt-3 mt-md-0 d-flex flex-wrap gap-2">
                        {canManage && (
                            <Button
                                as={Link as any}
                                to={`/bots/${bot.id}/edit`}
                                variant="outline-secondary"
                            >
                                Edit Bot
                            </Button>
                        )}
                        {isOwner && (
                            <Button as={Link as any} to="/matches/search" variant="primary">
                                Find Match
                            </Button>
                        )}
                    </Col>
                </Row>

                {bot.code && isOwner && (
                    <Row className="justify-content-center">
                        <Col lg={10}>
                            <Card className="glass-card mb-4 border-0">
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
                        </Col>
                    </Row>
                )}

                <Row className="g-4 mb-5">
                    <Col md={3}>
                        <Card className="p-3 h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                ELO Rating
                            </h6>
                            <span className="h2 fw-bold mb-0 text-white">{bot.elo}</span>
                        </Card>
                    </Col>
                    <Col md={3}>
                        <Card className="p-3 h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                Win Rate
                            </h6>
                            <span className="h2 fw-bold mb-0 text-success">{winRate}%</span>
                            <p className="text-secondary small mb-0">{totalMatches} matches</p>
                        </Card>
                    </Col>
                    <Col md={3}>
                        <Card className="p-3 h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                Wins
                            </h6>
                            <span className="h2 fw-bold mb-0 text-success">{bot.wins}</span>
                        </Card>
                    </Col>
                    <Col md={3}>
                        <Card className="p-3 h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                W / L / D
                            </h6>
                            <div className="d-flex flex-wrap gap-2 mt-1">
                                <Badge bg="success">W: {bot.wins}</Badge>
                                <Badge bg="danger">L: {bot.losses}</Badge>
                                <Badge bg="secondary">D: {bot.draws}</Badge>
                            </div>
                        </Card>
                    </Col>
                </Row>
            </Container>
            <Footer />
        </div>
    );
}
