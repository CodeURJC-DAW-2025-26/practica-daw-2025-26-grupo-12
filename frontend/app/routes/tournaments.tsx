import { useState } from "react";
import { Link, useLoaderData } from "react-router";
import { Container, Card, Table, Button, Form, Row, Col, Badge, Spinner } from "react-bootstrap";
import type { Route } from "./+types/tournaments";
import { getTournaments } from "~/services/tournament-service";
import type { TournamentSummary } from "~/types";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Tournaments - Scissors, Please" },
        { name: "description", content: "Browse all available bot tournaments." },
    ];
}

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const query = url.searchParams.get("q") ?? "";
    const data = await getTournaments(0, 10, query || undefined);
    return { tournaments: data.content, total: data.totalElements, hasMore: !data.last, query };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Could not load tournaments.";
    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 text-center">
                <p className="text-danger">{msg}</p>
                <Button as={Link as any} to="/" variant="primary">
                    Go Home
                </Button>
            </Container>
            <Footer />
        </div>
    );
}

export default function Tournaments() {
    const {
        tournaments: initial,
        total: initialTotal,
        hasMore: initialHasMore,
        query,
    } = useLoaderData<typeof clientLoader>();

    const [tournaments, setTournaments] = useState<TournamentSummary[]>(initial);
    const [total, setTotal] = useState(initialTotal);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(initialHasMore);
    const [loadingMore, setLoadingMore] = useState(false);
    const [searchQuery, setSearchQuery] = useState(query);

    async function loadMore() {
        setLoadingMore(true);
        try {
            const nextPage = page + 1;
            const data = await getTournaments(nextPage, 10, searchQuery || undefined);
            setTournaments((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } finally {
            setLoadingMore(false);
        }
    }

    function getStatusBadge(status: string) {
        if (status === "OPEN") return <Badge bg="success">Open</Badge>;
        if (status === "CLOSED") return <Badge bg="danger">Closed</Badge>;
        return <Badge bg="secondary">{status}</Badge>;
    }

    function handleSearch(e: React.FormEvent) {
        e.preventDefault();
        const params = new URLSearchParams();
        if (searchQuery) params.set("q", searchQuery);
        window.history.pushState({}, "", `/tournaments?${params.toString()}`);
        setPage(0);
        getTournaments(0, 10, searchQuery || undefined).then((data) => {
            setTournaments(data.content);
            setTotal(data.totalElements);
            setHasMore(!data.last);
        });
    }

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                    <div>
                        <p className="text-secondary mb-1 small text-uppercase">Season 2026</p>
                        <h1 className="h3 fw-bold mb-0">Tournaments</h1>
                    </div>
                </div>

                <Card className="p-4 mb-4">
                    <Form onSubmit={handleSearch}>
                        <Row className="g-3 align-items-center">
                            <Col lg={9}>
                                <Form.Label
                                    htmlFor="tournament-search"
                                    className="text-uppercase small mb-1"
                                >
                                    Search tournaments
                                </Form.Label>
                                <Form.Control
                                    id="tournament-search"
                                    type="search"
                                    name="q"
                                    placeholder="Search by tournament name..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
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
                                    as={Link as any}
                                    to="/tournaments"
                                    variant="outline-secondary"
                                    size="sm"
                                    className="px-3 mt-lg-4"
                                    onClick={() => {
                                        setSearchQuery("");
                                    }}
                                >
                                    Clear
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </Card>

                <Card className="p-4">
                    <div className="d-flex justify-content-between align-items-center gap-3 mb-3 flex-wrap">
                        <h2 className="h5 fw-bold mb-0 text-white">Tournament List</h2>
                        <span id="tournament-counter" className="text-light small">
                            Showing {tournaments.length} of {total}
                        </span>
                    </div>
                    <div className="table-responsive">
                        <Table className="mb-0 align-middle text-light">
                            <thead>
                                <tr className="text-secondary small text-uppercase border-secondary">
                                    <th scope="col" className="ps-3" style={{ width: 60 }}>
                                        Image
                                    </th>
                                    <th scope="col">Tournament</th>
                                    <th scope="col">Slots</th>
                                    <th scope="col">Status</th>
                                    <th scope="col" className="text-end pe-3">
                                        Action
                                    </th>
                                </tr>
                            </thead>
                            <tbody id="tournament-table-body">
                                {tournaments.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="text-center text-secondary py-4">
                                            No tournaments found.
                                        </td>
                                    </tr>
                                ) : (
                                    tournaments.map((t) => (
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
                                                    variant="outline-secondary"
                                                    size="sm"
                                                >
                                                    Details
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </Table>
                    </div>
                    {hasMore && tournaments.length < total && (
                        <div className="d-flex justify-content-center mt-3">
                            <Button
                                id="show-more-tournaments-btn"
                                variant="outline-secondary"
                                size="sm"
                                onClick={loadMore}
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
            </Container>
            <Footer />
        </div>
    );
}
