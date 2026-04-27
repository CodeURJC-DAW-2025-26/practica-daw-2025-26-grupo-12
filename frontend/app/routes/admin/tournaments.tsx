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
import type { Route } from "./+types/tournaments";
import { adminTournamentService } from "../../services/admin-tournament-service";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const query = url.searchParams.get("query") || "";
    try {
        const data = await adminTournamentService.getTournaments(0, 10, query);
        return { data, query };
    } catch (error) {
        throw new Error("Failed to load tournaments");
    }
}

export default function AdminTournaments() {
    const { data: initialData, query: initialQuery } = useLoaderData<typeof clientLoader>();
    const [tournaments, setTournaments] = useState(initialData.content);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(!initialData.last);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(initialQuery);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [targetTournament, setTargetTournament] = useState<{ id: number; name: string } | null>(
        null
    );

    useEffect(() => {
        setTournaments(initialData.content);
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
            const data = await adminTournamentService.getTournaments(nextPage, 10, query);
            setTournaments((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } catch (error) {
            alert("Error loading more tournaments");
        } finally {
            setLoadingMore(false);
        }
    };

    const onDelete = async () => {
        if (!targetTournament) return;
        try {
            await adminTournamentService.deleteTournament(targetTournament.id);
            setTournaments((prev) => prev.filter((t) => t.id !== targetTournament.id));
        } catch (error) {
            alert("Error deleting tournament");
        }
        setShowDeleteModal(false);
    };

    const getStatusBadge = (status: string) => {
        if (status === "OPEN") return <Badge bg="success">Open</Badge>;
        if (status === "CLOSED") return <Badge bg="danger">Closed</Badge>;
        return <Badge bg="secondary">{status}</Badge>;
    };

    return (
        <Container className="py-5">
            <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                <div>
                    <p className="text-secondary mb-1 small text-uppercase">Administration</p>
                    <h1 className="h3 fw-bold mb-0">Tournament Admin</h1>
                </div>
                <Form className="d-flex">
                    <Button variant="outline-muted" size="sm">
                        Run Daily Job Now
                    </Button>
                </Form>
            </div>

            <Card className="p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3 align-items-center">
                        <Col lg={9}>
                            <Form.Label className="text-uppercase small mb-1">
                                Search tournaments
                            </Form.Label>
                            <Form.Control
                                type="search"
                                placeholder="Search by tournament name..."
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                autoComplete="off"
                            />
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

            <Row className="g-4">
                <Col xs={12}>
                    <Card className="p-4">
                        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                            <h2 className="h5 fw-bold mb-0">Managed Tournaments</h2>
                            <div className="d-flex flex-wrap gap-2 align-items-center">
                                <Badge bg="secondary">
                                    Showing {tournaments.length} of {initialData.totalElements}
                                </Badge>
                                <Button
                                    as={Link as any}
                                    to="/admin/tournaments/new"
                                    variant="outline-muted"
                                    size="sm"
                                >
                                    New Tournament
                                </Button>
                            </div>
                        </div>
                        <div className="table-responsive">
                            <Table className="mb-0 align-middle text-nowrap">
                                <thead>
                                    <tr className="text-secondary small text-uppercase border-secondary">
                                        <th scope="col" className="ps-3" style={{ width: 60 }}>
                                            Image
                                        </th>
                                        <th scope="col">Tournament</th>
                                        <th scope="col">Slots</th>
                                        <th scope="col">Status</th>
                                        <th scope="col" className="text-end pe-3">
                                            Actions
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {tournaments.map((t) => (
                                        <tr key={t.id} className="border-secondary">
                                            <td className="ps-3">
                                                {t.imageUrl ? (
                                                    <img
                                                        src={`/api/v1/images/tournaments/${t.id}`}
                                                        alt={t.name}
                                                        className="rounded"
                                                        style={{
                                                            width: 40,
                                                            height: 40,
                                                            objectFit: "cover",
                                                        }}
                                                    />
                                                ) : (
                                                    <div
                                                        className="rounded bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                                        style={{ width: 40, height: 40 }}
                                                    >
                                                        T
                                                    </div>
                                                )}
                                            </td>
                                            <td className="fw-semibold text-white">{t.name}</td>
                                            <td className="text-secondary">
                                                {t.participants} / {t.slots}
                                            </td>
                                            <td>{getStatusBadge(t.status)}</td>
                                            <td className="text-end pe-3">
                                                <Button
                                                    as={Link as any}
                                                    to={`/tournaments/${t.id}`}
                                                    variant="outline-muted"
                                                    size="sm"
                                                    className="me-2"
                                                >
                                                    View
                                                </Button>
                                                <Button
                                                    as={Link as any}
                                                    to={`/admin/tournaments/${t.id}`}
                                                    variant="outline-info"
                                                    size="sm"
                                                    className="me-2"
                                                >
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => {
                                                        setTargetTournament({
                                                            id: t.id,
                                                            name: t.name,
                                                        });
                                                        setShowDeleteModal(true);
                                                    }}
                                                >
                                                    Delete
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    {tournaments.length === 0 && (
                                        <tr>
                                            <td
                                                colSpan={5}
                                                className="text-center text-secondary py-4"
                                            >
                                                No tournaments found.
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
                                            <Spinner
                                                animation="border"
                                                size="sm"
                                                className="me-2"
                                            />
                                            Loading…
                                        </>
                                    ) : (
                                        "Show more"
                                    )}
                                </Button>
                            </div>
                        )}
                    </Card>
                </Col>
            </Row>

            <Row className="g-4 mt-1">
                <Col xs={12}>
                    <Card className="p-4">
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <h2 className="h5 fw-bold mb-0">
                                Real-Time Notifications (Admin Testing)
                            </h2>
                            <Badge bg="primary">Live</Badge>
                        </div>
                        <Form
                            onSubmit={async (e) => {
                                e.preventDefault();
                                const target = (e.target as any)[0].value;
                                const msg = (e.target as any)[1].value;
                                if (!msg) return;
                                try {
                                    const usernames = target
                                        ? target
                                              .split(",")
                                              .map((u: string) => u.trim())
                                              .filter((u: string) => u.length > 0)
                                        : [];
                                    await fetch("/api/v1/notifications/admin", {
                                        method: "POST",
                                        headers: { "Content-Type": "application/json" },
                                        body: JSON.stringify({ usernames, message: msg }),
                                        credentials: "include",
                                    });
                                    (e.target as any)[0].value = "";
                                    (e.target as any)[1].value = "";
                                    alert("Notification sent!");
                                } catch (err) {
                                    alert("Failed to send notification");
                                }
                            }}
                        >
                            <Row className="g-3">
                                <Col md={5}>
                                    <Form.Label className="text-secondary small">
                                        Target Username (Leave empty for ALL users)
                                    </Form.Label>
                                    <Form.Control type="text" placeholder="e.g. user1" />
                                </Col>
                                <Col md={5}>
                                    <Form.Label className="text-secondary small">
                                        Message
                                    </Form.Label>
                                    <Form.Control type="text" placeholder="Hello World!" required />
                                </Col>
                                <Col md={2} className="d-flex align-items-end">
                                    <Button type="submit" variant="primary" className="w-100">
                                        Send
                                    </Button>
                                </Col>
                            </Row>
                        </Form>
                    </Card>
                </Col>
            </Row>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton className="border-secondary">
                    <Modal.Title className="h5">Delete Tournament</Modal.Title>
                </Modal.Header>
                <Modal.Body className="p-0">
                    <div className="text-center p-5">
                        <h4 className="fw-bold mb-2 text-white">
                            Delete{" "}
                            <span className="text-primary fst-italic">
                                {targetTournament?.name}
                            </span>
                            ?
                        </h4>
                        <p className="text-secondary px-3">
                            This will permanently remove the tournament. All matches, rankings, and
                            historical data associated with it will be gone forever.
                        </p>
                    </div>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="outline-muted" onClick={() => setShowDeleteModal(false)}>
                        Keep Tournament
                    </Button>
                    <Button variant="danger" onClick={onDelete}>
                        Delete Forever
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}
