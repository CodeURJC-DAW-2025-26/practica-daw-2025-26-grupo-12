import { useState, useEffect } from "react";
import { useSearchParams, Link, useLoaderData } from "react-router";
import {
    Container,
    Row,
    Col,
    Card,
    Table,
    Form,
    Button,
    Badge,
    Modal,
    Spinner,
} from "react-bootstrap";
import type { Route } from "./+types/bots";
import { adminBotService, type BotPageResponse } from "../../services/admin-bot-service";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const query = url.searchParams.get("query") || "";
    try {
        const data = await adminBotService.getBots(0, 10, query);
        return { data, query };
    } catch (error) {
        throw new Error("Failed to load bots");
    }
}

export default function AdminBots() {
    const { data: initialData, query: initialQuery } = useLoaderData<typeof clientLoader>();
    const [bots, setBots] = useState(initialData.content);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(!initialData.last);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(initialQuery);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [targetBot, setTargetBot] = useState<{ id: number; name: string } | null>(null);

    useEffect(() => {
        setBots(initialData.content);
        setPage(0);
        setHasMore(!initialData.last);
        setQuery(initialQuery);
    }, [initialData, initialQuery]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setSearchParams({ query });
    };

    const [loadingMore, setLoadingMore] = useState(false);

    const handleLoadMore = async () => {
        setLoadingMore(true);
        const nextPage = page + 1;
        try {
            const data = await adminBotService.getBots(nextPage, 10, query);
            setBots((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } catch (error) {
            alert("Error loading more bots");
        } finally {
            setLoadingMore(false);
        }
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
        <Container className="py-5">
            <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                <div>
                    <p className="text-secondary mb-1 small text-uppercase">Administration</p>
                    <h1 className="h3 fw-bold mb-0">Bot Management</h1>
                </div>
            </div>

            <Card className="p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3 align-items-center">
                        <Col lg={7}>
                            <Form.Label className="text-uppercase small mb-1">
                                Search bots
                            </Form.Label>
                            <Form.Control
                                type="search"
                                placeholder="Search by bot name..."
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                autoComplete="off"
                            />
                        </Col>
                        <Col lg={2}>
                            <Form.Label className="text-uppercase small mb-1">
                                Visibility
                            </Form.Label>
                            <Form.Select>
                                <option value="all">All</option>
                                <option value="public">Public</option>
                                <option value="private">Private</option>
                            </Form.Select>
                        </Col>
                        <Col lg={3} className="d-grid d-lg-flex justify-content-lg-end gap-2">
                            <Button
                                type="submit"
                                variant="primary"
                                size="sm"
                                className="px-3 mt-lg-4"
                            >
                                Search
                            </Button>
                            <Button
                                variant="outline-muted"
                                size="sm"
                                className="px-3 mt-lg-4"
                                onClick={() => {
                                    setQuery("");
                                    setSearchParams({});
                                }}
                            >
                                Clear
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card>

            <Card className="p-4">
                <div className="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-3">
                    <h2 className="h5 fw-bold mb-0">Matching Bots</h2>
                    <Badge bg="secondary">
                        Showing {bots.length} of {initialData.totalElements}
                    </Badge>
                </div>
                <div className="table-responsive">
                    <Table className="mb-0 align-middle text-nowrap">
                        <thead>
                            <tr className="text-secondary small text-uppercase">
                                <th scope="col" className="ps-3">
                                    Icon
                                </th>
                                <th scope="col">Name</th>
                                <th scope="col">Owner</th>
                                <th scope="col">ELO</th>
                                <th scope="col">Visibility</th>
                                <th scope="col" className="text-end pe-3">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {bots.map((bot) => (
                                <tr key={bot.id} className="border-secondary">
                                    <td className="ps-3">
                                        {bot.imageUrl ? (
                                            <img
                                                src={`/api/v1/images/bots/${bot.id}`}
                                                alt={bot.name}
                                                className="rounded-circle"
                                                style={{
                                                    width: 40,
                                                    height: 40,
                                                    objectFit: "cover",
                                                }}
                                            />
                                        ) : (
                                            <div
                                                className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                                style={{ width: 40, height: 40 }}
                                            >
                                                {bot.name.charAt(0).toUpperCase()}
                                            </div>
                                        )}
                                    </td>
                                    <td className="fw-semibold text-white">{bot.name}</td>
                                    <td className="text-secondary">{bot.ownerUsername}</td>
                                    <td>{bot.elo}</td>
                                    <td>
                                        {bot.public ? (
                                            <Badge bg="secondary">Public</Badge>
                                        ) : (
                                            <Badge
                                                bg="dark"
                                                className="border border-secondary text-secondary"
                                            >
                                                Private
                                            </Badge>
                                        )}
                                    </td>
                                    <td className="text-end pe-3">
                                        <Button
                                            as={Link as any}
                                            to={`/bots/${bot.id}`}
                                            variant="outline-muted"
                                            size="sm"
                                            className="me-2"
                                        >
                                            View
                                        </Button>
                                        <Button
                                            variant="outline-danger"
                                            size="sm"
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
                                    <td colSpan={6} className="text-center text-secondary py-4">
                                        No bots found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </div>
                {hasMore && (
                    <div className="d-flex justify-content-center mt-3">
                        <Button
                            variant="outline-secondary"
                            size="sm"
                            onClick={handleLoadMore}
                            disabled={loadingMore}
                        >
                            {loadingMore ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Loading…
                                </>
                            ) : (
                                "Show more"
                            )}
                        </Button>
                    </div>
                )}
            </Card>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton className="border-secondary">
                    <Modal.Title className="h5">Delete Bot</Modal.Title>
                </Modal.Header>
                <Modal.Body className="p-0">
                    <div className="text-center p-5">
                        <h4 className="fw-bold mb-2 text-white">
                            Delete{" "}
                            <span className="text-primary fst-italic">{targetBot?.name}</span>?
                        </h4>
                        <p className="text-secondary px-3">
                            This will permanently remove this bot from the system. This action
                            cannot be undone.
                        </p>
                    </div>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button
                        variant="outline-muted"
                        className="py-2"
                        onClick={() => setShowDeleteModal(false)}
                    >
                        Cancel
                    </Button>
                    <Button variant="danger" className="py-2" onClick={onDelete}>
                        Delete Forever
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}
