import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import type { Route } from "./+types/bot-detail";
import { adminBotService } from "../../services/admin-bot-service";
import type { BotDetail } from "~/types";
import { Button, Row, Col, Badge, Card, Modal } from "react-bootstrap";
import { getEloChart, getResultsChart } from "~/services/chart-service";
import {
    PieChart, Pie, Cell,
    LineChart, Line,
    XAxis, YAxis, Tooltip, ResponsiveContainer
} from "recharts";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const { id } = params;
    try {
        const bot = await adminBotService.getBot(id);
        return { bot };
    } catch (error) {
        throw new Error("Bot not found or access denied");
    }
}

export default function AdminBotDetail({ loaderData }: Route.ComponentProps) {
    const { bot } = loaderData as { bot: BotDetail };
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const navigate = useNavigate();

    const [resultsData, setResultsData] = useState<any>(null);
    const [eloData, setEloData] = useState<any>(null);

    useEffect(() => {
        const loadCharts = async () => {
            try {
                const results = await getResultsChart({
                    wins: bot.wins,
                    losses: bot.losses,
                    draws: bot.draws
                });

                setResultsData([
                    { name: "Wins", value: results.wins },
                    { name: "Losses", value: results.losses },
                    { name: "Draws", value: results.draws }
                ]);

                const eloHistory = bot.eloHistory ?? [];
                const elo = await getEloChart(eloHistory);

                setEloData(
                    elo.map((value: number, index: number) => ({
                        game: index + 1,
                        elo: value
                    }))
                );
            } catch (e) {
                console.error(e);
            }
        };

        loadCharts();
    }, [bot]);

    const onDelete = async () => {
        try {
            await adminBotService.deleteBot(bot.id);
            navigate("/admin/bots");
        } catch (error) {
            alert("Error deleting bot");
        }
    };

    return (
        <div className="admin-bot-detail animate__animated animate__fadeIn">
            <div className="mb-4">
                <Link to="/admin/bots" className="text-decoration-none text-muted small hover-link">
                    &larr; Back to Inventory
                </Link>
            </div>

            <Row className="mb-5 align-items-end">
                <Col md={8}>
                    <div className="d-flex align-items-center gap-4">
                        {bot.imageUrl ? (
                            <img
                                src={bot.imageUrl}
                                alt={bot.name}
                                className="rounded-4 border border-primary border-3"
                                style={{ width: 100, height: 100, objectFit: "cover" }}
                            />
                        ) : (
                            <div
                                className="avatar-placeholder rounded-4 bg-primary d-flex align-items-center justify-content-center text-white fw-bold fs-1"
                                style={{ width: 100, height: 100 }}
                            >
                                {bot.name.charAt(0).toUpperCase()}
                            </div>
                        )}
                        <div>
                            <h1 className="fw-bold m-0">{bot.name}</h1>
                            <p className="text-muted m-0">
                                Owned by{" "}
                                <Link to={`/profile/${bot.ownerId}`} className="text-info fw-bold">
                                    @{bot.ownerUsername}
                                </Link>
                            </p>
                            <div className="mt-2 text-muted small">
                                ID: {bot.id} • Created:{" "}
                                {new Date(bot.createdAt).toLocaleDateString()}
                            </div>
                        </div>
                    </div>
                </Col>
                <Col md={4} className="text-md-end mt-4 mt-md-0">
                    <Badge
                        className={`${bot.public ? "badge-soft-success" : "badge-soft-warning"} me-3 p-2 px-3 fs-6`}
                        pill
                    >
                        {bot.public ? "Public Visibility" : "Private Bot"}
                    </Badge>
                    <Button
                        variant="danger"
                        className="rounded-pill px-4"
                        onClick={() => setShowDeleteModal(true)}
                    >
                        Delete Bot
                    </Button>
                </Col>
            </Row>

            <Row className="g-4">
                <Col lg={4}>
                    <div className="glass-card p-4 h-100">
                        <h4 className="fw-bold mb-4">Performance</h4>
                        <div className="d-flex justify-content-between mb-3 border-bottom border-secondary pb-2">
                            <span className="text-muted">Current Elo</span>
                            <span className="fw-bold text-primary fs-4">{bot.elo}</span>
                        </div>
                        <Row className="text-center g-2 mt-4">
                            <Col xs={4}>
                                <Card className="bg-dark border-secondary p-2">
                                    <div className="text-success small fw-bold">WINS</div>
                                    <div className="fs-4">{bot.wins}</div>
                                </Card>
                            </Col>
                            <Col xs={4}>
                                <Card className="bg-dark border-secondary p-2">
                                    <div className="text-danger small fw-bold">LOSSES</div>
                                    <div className="fs-4">{bot.losses}</div>
                                </Card>
                            </Col>
                            <Col xs={4}>
                                <Card className="bg-dark border-secondary p-2">
                                    <div className="text-warning small fw-bold">DRAWS</div>
                                    <div className="fs-4">{bot.draws}</div>
                                </Card>
                            </Col>
                        </Row>

                        {resultsData && (
                            <div className="mt-4">
                                <ResponsiveContainer width="100%" height={200}>
                                    <PieChart>
                                        <Pie data={resultsData} dataKey="value">
                                            <Cell fill="#22c55e" />
                                            <Cell fill="#ef4444" />
                                            <Cell fill="#eab308" />
                                        </Pie>
                                        <Tooltip />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                        )}

                        <div className="mt-5">
                            <h5 className="small fw-bold text-muted text-uppercase letter-spacing-lg mb-3">
                                Tags
                            </h5>
                            <div className="d-flex flex-wrap gap-2">
                                {bot.tags?.map((tag) => (
                                    <Badge key={tag} className="badge-soft-info" pill>
                                        {tag}
                                    </Badge>
                                ))}
                            </div>
                        </div>
                    </div>
                </Col>

                <Col lg={8}>
                    <div className="glass-card p-4 mb-4">
                        <h4 className="fw-bold mb-4">Description</h4>
                        <p className="text-light">
                            {bot.description || "No description provided."}
                        </p>
                    </div>

                    {eloData && (
                        <div className="glass-card p-4 mb-4">
                            <h4 className="fw-bold mb-4">ELO Progression</h4>
                            <ResponsiveContainer width="100%" height={250}>
                                <LineChart data={eloData}>
                                    <XAxis dataKey="game" />
                                    <YAxis />
                                    <Tooltip />
                                    <Line type="monotone" dataKey="elo" stroke="#3b82f6" />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    )}

                    <div className="glass-card p-4">
                        <h4 className="fw-bold mb-4">Source Code</h4>
                        <pre
                            className="bg-dark p-4 rounded-4 border border-secondary text-info overflow-auto"
                            style={{ maxHeight: "500px" }}
                        >
                            <code>{bot.code || "// No code available for this bot."}</code>
                        </pre>
                    </div>
                </Col>
            </Row>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Delete Bot</Modal.Title>
                </Modal.Header>
                <Modal.Body className="text-danger">
                    Are you sure you want to globally delete <strong>{bot.name}</strong>?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={onDelete}>
                        Delete Bot
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}