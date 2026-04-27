import { useState } from "react";
import { Link, useLoaderData } from "react-router";
import { Container, Card, Table, Button, Badge, Spinner } from "react-bootstrap";
import type { Route } from "./+types/matches";
import { getMatches } from "~/services/match-service";
import type { MatchSummary } from "~/types";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Best Matches - Scissors, Please" },
        { name: "description", content: "Browse the top-rated matches in the arena." },
    ];
}

export async function clientLoader(_args: Route.ClientLoaderArgs) {
    const data = await getMatches(0, 10);
    return { matches: data.content, total: data.totalElements, hasMore: !data.last };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Could not load matches.";
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

export default function Matches() {
    const {
        matches: initial,
        total,
        hasMore: initialHasMore,
    } = useLoaderData<typeof clientLoader>();
    const [matches, setMatches] = useState<MatchSummary[]>(initial);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(initialHasMore);
    const [loadingMore, setLoadingMore] = useState(false);

    async function loadMore() {
        setLoadingMore(true);
        try {
            const nextPage = page + 1;
            const data = await getMatches(nextPage, 10);
            setMatches((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } finally {
            setLoadingMore(false);
        }
    }

    function getResultBadge(m: MatchSummary) {
        if (!m.winnerBotId) return <Badge bg="secondary">Draw</Badge>;
        if (m.winnerBotId === m.bot1Id) return <Badge bg="success">{m.bot1Name} wins</Badge>;
        return <Badge bg="danger">{m.bot2Name} wins</Badge>;
    }

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                    <div>
                        <p className="text-secondary mb-1 small text-uppercase">Leaderboard</p>
                        <h1 className="h3 fw-bold mb-0">Best Matches</h1>
                    </div>
                </div>

                <Card className="p-4">
                    <div className="d-flex justify-content-between align-items-center gap-3 mb-3 flex-wrap">
                        <h2 className="h5 fw-bold mb-0 text-white">Match List</h2>
                        <span id="match-counter" className="text-secondary small">
                            Showing {matches.length} of {total}
                        </span>
                    </div>
                    <div className="table-responsive">
                        <Table className="mb-0 align-middle text-light">
                            <thead>
                                <tr className="text-secondary small text-uppercase border-secondary">
                                    <th scope="col">Player 1</th>
                                    <th scope="col">Player 2</th>
                                    <th scope="col">Top ELO</th>
                                    <th scope="col">Result</th>
                                    <th scope="col">Date</th>
                                    <th scope="col" className="text-end pe-4">
                                        Action
                                    </th>
                                </tr>
                            </thead>
                            <tbody id="match-table-body">
                                {matches.length === 0 ? (
                                    <tr>
                                        <td colSpan={6} className="text-center text-secondary py-4">
                                            No matches found.
                                        </td>
                                    </tr>
                                ) : (
                                    matches.map((m) => (
                                        <tr key={m.id} className="border-secondary">
                                            <td>
                                                <div className="d-flex align-items-center gap-2">
                                                    <div
                                                        className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white small fw-bold"
                                                        style={{
                                                            width: 24,
                                                            height: 24,
                                                            fontSize: "0.7rem",
                                                        }}
                                                    >
                                                        {m.bot1Name.charAt(0).toUpperCase()}
                                                    </div>
                                                    <Link
                                                        to={`/bots/${m.bot1Id}`}
                                                        className="text-white text-decoration-none hover-link fw-semibold"
                                                    >
                                                        {m.bot1Name}
                                                    </Link>
                                                </div>
                                                <span className="text-secondary small opacity-75">
                                                    ({m.bot1OwnerName})
                                                </span>
                                            </td>
                                            <td>
                                                <div className="d-flex align-items-center gap-2">
                                                    <div
                                                        className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white small fw-bold"
                                                        style={{
                                                            width: 24,
                                                            height: 24,
                                                            fontSize: "0.7rem",
                                                        }}
                                                    >
                                                        {m.bot2Name.charAt(0).toUpperCase()}
                                                    </div>
                                                    <Link
                                                        to={`/bots/${m.bot2Id}`}
                                                        className="text-white text-decoration-none hover-link fw-semibold"
                                                    >
                                                        {m.bot2Name}
                                                    </Link>
                                                </div>
                                                <span className="text-secondary small opacity-75">
                                                    ({m.bot2OwnerName})
                                                </span>
                                            </td>
                                            <td>{m.maxElo}</td>
                                            <td>{getResultBadge(m)}</td>
                                            <td className="text-secondary small">{m.playedAt}</td>
                                            <td className="text-end pe-4">
                                                <Button
                                                    as={Link as any}
                                                    to={`/matches/${m.id}/stats`}
                                                    variant="outline-secondary"
                                                    size="sm"
                                                >
                                                    Stats
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
                                id="show-more-matches-btn"
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
