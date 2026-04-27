import { useState } from "react";
import { Link, useLoaderData } from "react-router";
import { Container, Card, Table, Button, Form, Row, Col, Spinner } from "react-bootstrap";
import type { Route } from "./+types/bots";
import { getBots } from "~/services/bot-service";
import type { BotDetail } from "~/types";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Global Bots - Scissors, Please" },
        {
            name: "description",
            content: "Explore the best Rock, Paper, Scissors bots in the arena.",
        },
    ];
}

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const query = url.searchParams.get("q") ?? "";
    const data = await getBots(0, 10, query || undefined);
    return { bots: data.content, total: data.totalElements, hasMore: !data.last, query };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Could not load bots.";
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

export default function BotsList() {
    const {
        bots: initial,
        total: initialTotal,
        hasMore: initialHasMore,
        query,
    } = useLoaderData<typeof clientLoader>();

    const [bots, setBots] = useState<BotDetail[]>(initial);
    const [total, setTotal] = useState(initialTotal);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(initialHasMore);
    const [loadingMore, setLoadingMore] = useState(false);
    const [searchQuery, setSearchQuery] = useState(query);

    async function handleSearch(e: React.FormEvent) {
        e.preventDefault();
        const params = new URLSearchParams();
        if (searchQuery) params.set("q", searchQuery);
        window.history.pushState({}, "", `/bots?${params.toString()}`);
        setPage(0);
        const data = await getBots(0, 10, searchQuery || undefined);
        setBots(data.content);
        setTotal(data.totalElements);
        setHasMore(!data.last);
    }

    async function loadMore() {
        setLoadingMore(true);
        try {
            const nextPage = page + 1;
            const data = await getBots(nextPage, 10, searchQuery || undefined);
            setBots((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } finally {
            setLoadingMore(false);
        }
    }

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                    <div>
                        <p className="text-secondary mb-1 small text-uppercase">The Arena</p>
                        <h1 className="h3 fw-bold mb-0">Global Leaderboard</h1>
                    </div>
                </div>

                <Card className="p-4 mb-4">
                    <Form onSubmit={handleSearch}>
                        <Row className="g-3 align-items-center">
                            <Col lg={9}>
                                <Form.Label
                                    htmlFor="bot-search"
                                    className="text-uppercase small mb-1"
                                >
                                    Search bots
                                </Form.Label>
                                <Form.Control
                                    id="bot-search"
                                    type="search"
                                    name="q"
                                    placeholder="Search by bot name..."
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
                                    to="/bots"
                                    variant="outline-secondary"
                                    size="sm"
                                    className="px-3 mt-lg-4"
                                    onClick={() => setSearchQuery("")}
                                >
                                    Clear
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </Card>

                <Card className="p-4">
                    <div className="d-flex justify-content-between align-items-center gap-3 mb-3 flex-wrap">
                        <h2 className="h5 fw-bold mb-0 text-white">Bot List</h2>
                        <span className="text-secondary small">
                            Showing {bots.length} of {total}
                        </span>
                    </div>
                    <div className="table-responsive">
                        <Table className="mb-0 align-middle text-light">
                            <thead>
                                <tr className="text-secondary small text-uppercase border-secondary">
                                    <th scope="col" className="ps-3" style={{ width: 60 }}>
                                        Icon
                                    </th>
                                    <th scope="col">Bot Name</th>
                                    <th scope="col">Owner</th>
                                    <th scope="col">ELO</th>
                                    <th scope="col" className="text-end pe-3">
                                        Action
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {bots.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="text-center text-secondary py-4">
                                            No bots found.
                                        </td>
                                    </tr>
                                ) : (
                                    bots.map((b) => (
                                        <tr key={b.id} className="border-secondary">
                                            <td className="ps-3">
                                                {b.imageUrl ? (
                                                    <img
                                                        src={`/api/v1/images/bots/${b.id}`}
                                                        alt={b.name}
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
                                                        {b.name.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                            </td>
                                            <td className="fw-semibold text-white">{b.name}</td>
                                            <td className="text-secondary small">
                                                {b.ownerUsername}
                                            </td>
                                            <td className="fw-bold text-primary">{b.elo}</td>
                                            <td className="text-end pe-3">
                                                <Button
                                                    as={Link as any}
                                                    to={`/bots/${b.id}`}
                                                    variant="outline-secondary"
                                                    size="sm"
                                                >
                                                    Profile
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </Table>
                    </div>
                    {hasMore && (
                        <div className="d-flex justify-content-center mt-3">
                            <Button
                                variant="outline-secondary"
                                size="sm"
                                onClick={loadMore}
                                disabled={loadingMore}
                            >
                                {loadingMore ? (
                                    <Spinner animation="border" size="sm" />
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
