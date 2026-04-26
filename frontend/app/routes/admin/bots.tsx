import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import type { Route } from "./+types/bots";
import { adminBotService, type BotPageResponse } from "../../services/admin-bot-service";
import { Table, Button, Form, Modal, Pagination, Badge, Row, Col } from "react-bootstrap";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get("page") || "0");
    const query = url.searchParams.get("query") || "";

    try {
        const data = await adminBotService.getBots(page, 10, query);
        return { data };
    } catch (error) {
        throw new Error("Failed to load bots");
    }
}

export default function AdminBots({ loaderData }: Route.ComponentProps) {
    const initialData = (loaderData as { data: BotPageResponse }).data;
    const [bots, setBots] = useState(initialData.content);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(searchParams.get("query") || "");

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [targetBot, setTargetBot] = useState<{ id: number; name: string } | null>(null);

    useEffect(() => {
        setBots(initialData.content);
    }, [initialData.content]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setSearchParams((prev) => {
            prev.set("query", query);
            prev.set("page", "0");
            return prev;
        });
    };

    const handlePageChange = (page: number) => {
        setSearchParams((prev) => {
            prev.set("page", page.toString());
            return prev;
        });
    };

    const onDelete = async () => {
        if (!targetBot) return;
        try {
            await adminBotService.deleteBot(targetBot.id);
            setBots((prev) => prev.filter((b) => b.id !== targetBot.id));
            setShowDeleteModal(false);
        } catch (error) {
            alert("Error deleting bot");
        }
    };

    return (
        <div className="admin-bots-page animate__animated animate__fadeIn">
            <div className="d-flex justify-content-between align-items-center mb-5 mt-2">
                <div>
                    <h2 className="fw-bold m-0">Bot Inventory</h2>
                    <p className="text-muted m-0">Manage and monitor all system bots</p>
                </div>
            </div>

            <div className="glass-card p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3">
                        <Col md={8}>
                            <Form.Control
                                type="text"
                                placeholder="Search by bot name..."
                                className="form-control-lg"
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                            />
                        </Col>
                        <Col md={4} className="d-grid">
                            <Button
                                variant="primary"
                                type="submit"
                                className="btn-gradient-primary btn-lg"
                            >
                                Search Bots
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </div>

            <div className="glass-card overflow-hidden">
                <Table hover responsive className="m-0 align-middle">
                    <thead className="bg-dark text-uppercase small letter-spacing-lg">
                        <tr>
                            <th className="border-0 ps-4 py-3">Bot</th>
                            <th className="border-0 py-3">Owner</th>
                            <th className="border-0 py-3">Visibility</th>
                            <th className="border-0 py-3">Elo</th>
                            <th className="border-0 pe-4 py-3 text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="border-top-0">
                        {bots.map((bot) => (
                            <tr key={bot.id} className="border-bottom border-secondary">
                                <td className="ps-4 py-3">
                                    <div className="d-flex align-items-center gap-3">
                                        {bot.imageUrl ? (
                                            <img
                                                src={bot.imageUrl}
                                                alt={bot.name}
                                                className="rounded-circle border border-primary border-2"
                                                style={{
                                                    width: 40,
                                                    height: 40,
                                                    objectFit: "cover",
                                                }}
                                            />
                                        ) : (
                                            <div
                                                className="avatar-placeholder rounded-circle bg-primary d-flex align-items-center justify-content-center text-white"
                                                style={{ width: 40, height: 40 }}
                                            >
                                                {bot.name.charAt(0).toUpperCase()}
                                            </div>
                                        )}
                                        <div>
                                            <Link
                                                to={`/admin/bots/${bot.id}`}
                                                className="fw-bold text-white text-decoration-none hover-link"
                                            >
                                                {bot.name}
                                            </Link>
                                            <div className="small text-muted">ID: {bot.id}</div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <Link
                                        to={`/profile/${bot.ownerId}`}
                                        className="text-info text-decoration-none small"
                                    >
                                        @{bot.ownerUsername}
                                    </Link>
                                </td>
                                <td>
                                    <Badge
                                        className={
                                            bot.public ? "badge-soft-success" : "badge-soft-warning"
                                        }
                                        pill
                                    >
                                        {bot.public ? "Public" : "Private"}
                                    </Badge>
                                </td>
                                <td className="fw-bold">{bot.elo}</td>
                                <td className="pe-4 py-3 text-end">
                                    <Link
                                        to={`/admin/bots/${bot.id}`}
                                        className="btn btn-outline-info btn-sm me-2 rounded-pill px-3"
                                    >
                                        Inspect
                                    </Link>
                                    <Button
                                        variant="outline-danger"
                                        size="sm"
                                        className="rounded-pill px-3"
                                        onClick={() => {
                                            setTargetBot({ id: bot.id, name: bot.name });
                                            setShowDeleteModal(true);
                                        }}
                                    >
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {bots.length === 0 && (
                            <tr>
                                <td colSpan={5} className="text-center py-5 text-muted">
                                    <div className="mb-2 fs-2">🤖</div>
                                    No bots found.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </Table>
            </div>

            {initialData.totalPages > 1 && (
                <Pagination className="mt-4">
                    <Pagination.First
                        onClick={() => handlePageChange(0)}
                        disabled={initialData.number === 0}
                    />
                    <Pagination.Prev
                        onClick={() => handlePageChange(initialData.number - 1)}
                        disabled={initialData.number === 0}
                    />
                    {[...Array(initialData.totalPages)].map((_, i) => (
                        <Pagination.Item
                            key={i}
                            active={i === initialData.number}
                            onClick={() => handlePageChange(i)}
                        >
                            {i + 1}
                        </Pagination.Item>
                    ))}
                    <Pagination.Next
                        onClick={() => handlePageChange(initialData.number + 1)}
                        disabled={initialData.number === initialData.totalPages - 1}
                    />
                    <Pagination.Last
                        onClick={() => handlePageChange(initialData.totalPages - 1)}
                        disabled={initialData.number === initialData.totalPages - 1}
                    />
                </Pagination>
            )}

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Delete Bot</Modal.Title>
                </Modal.Header>
                <Modal.Body className="text-danger">
                    <strong>Warning!</strong> You are about to globally delete bot{" "}
                    <strong>{targetBot?.name}</strong>. This is irreversible.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={onDelete}>
                        Confirm Deletion
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
