import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Table, Button, Badge } from "react-bootstrap";
import type { Route } from "./+types/match-stats";
import { getMatchStats } from "~/services/match-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta({ data }: Route.MetaArgs) {
    const stats = (data as any)?.stats;
    const title = stats
        ? `Match #${stats.matchId} – Scissors, Please`
        : "Match Stats – Scissors, Please";
    return [
        { title },
        { name: "description", content: "Detailed match statistics and round timeline." },
    ];
}

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const id = Number(params.id);
    if (isNaN(id)) throw new Error("Invalid match ID");
    const stats = await getMatchStats(id);
    return { stats };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Match not found.";
    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 text-center">
                <h1 className="error-code">404</h1>
                <p className="text-secondary">{msg}</p>
                <Button as={Link as any} to="/matches" variant="primary">
                    All Matches
                </Button>
            </Container>
            <Footer />
        </div>
    );
}

function getMoveBadgeClass(move: string) {
    if (move === "ROCK") return "secondary";
    if (move === "PAPER") return "primary";
    if (move === "SCISSORS") return "warning";
    return "dark";
}

function getResultBadgeClass(result: string) {
    if (result === "WIN") return "success";
    if (result === "LOSS") return "danger";
    return "secondary";
}

export default function MatchStats() {
    const { stats } = useLoaderData<typeof clientLoader>();

    const winnerLabel = stats.winnerName ? `${stats.winnerName} wins` : "Draw";
    const winnerVariant = stats.winnerName ? "success" : "secondary";

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                    <div>
                        <p className="text-secondary mb-1 small text-uppercase">
                            Match #{stats.matchId}
                        </p>
                        <h1 className="h3 fw-bold mb-0">
                            <Link
                                to={`/bots/${stats.bot1Id}`}
                                className="text-light text-decoration-none hover-link"
                            >
                                {stats.bot1Name}
                            </Link>
                            <span className="text-secondary opacity-50 small ms-1">
                                (by <span className="text-secondary">{stats.bot1OwnerName}</span>)
                            </span>
                            {" vs "}
                            <Link
                                to={`/bots/${stats.bot2Id}`}
                                className="text-light text-decoration-none hover-link"
                            >
                                {stats.bot2Name}
                            </Link>
                            <span className="text-secondary opacity-50 small ms-1">
                                (by <span className="text-secondary">{stats.bot2OwnerName}</span>)
                            </span>
                        </h1>
                    </div>
                    <div className="d-flex flex-wrap align-items-center gap-2">
                        <Badge bg={winnerVariant} className="rounded-pill px-3 py-2">
                            {winnerLabel}
                        </Badge>
                    </div>
                </div>

                <Row className="g-4 mb-5">
                    <Col md={4}>
                        <Card className="p-3 stat-card h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                Final Score
                            </h6>
                            <div className="d-flex align-items-baseline gap-2">
                                <span className="h2 fw-bold mb-0 text-white">
                                    {stats.bot1Score}
                                </span>
                                <span className="text-secondary">-</span>
                                <span className="h2 fw-bold mb-0 text-white">
                                    {stats.bot2Score}
                                </span>
                            </div>
                            <p className="text-secondary small mb-0">
                                {stats.totalRounds} rounds played
                            </p>
                        </Card>
                    </Col>
                    <Col md={4}>
                        <Card className="p-3 stat-card h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                Winner
                            </h6>
                            <span className="h5 fw-bold mb-0 text-white">{winnerLabel}</span>
                        </Card>
                    </Col>
                    <Col md={4}>
                        <Card className="p-3 stat-card h-100">
                            <h6 className="text-secondary text-uppercase small fw-bold mb-2">
                                Played At
                            </h6>
                            <span className="h6 fw-bold mb-0 text-white">{stats.playedAt}</span>
                        </Card>
                    </Col>
                </Row>

                <Card className="p-4">
                    <h2 className="h5 fw-bold mb-3">Round Timeline</h2>
                    <div className="table-responsive">
                        <Table className="mb-0 align-middle text-nowrap">
                            <thead>
                                <tr className="text-secondary small text-uppercase">
                                    <th scope="col" className="ps-3">
                                        Round
                                    </th>
                                    <th scope="col">
                                        <Link
                                            to={`/bots/${stats.bot1Id}`}
                                            className="text-secondary text-decoration-none hover-link"
                                        >
                                            {stats.bot1Name}
                                        </Link>
                                    </th>
                                    <th scope="col">
                                        <Link
                                            to={`/bots/${stats.bot2Id}`}
                                            className="text-secondary text-decoration-none hover-link"
                                        >
                                            {stats.bot2Name}
                                        </Link>
                                    </th>
                                    <th scope="col">Result</th>
                                </tr>
                            </thead>
                            <tbody>
                                {stats.rounds.length > 0 ? (
                                    stats.rounds.map((r) => (
                                        <tr key={r.roundNumber}>
                                            <td className="ps-3 text-secondary">
                                                #{r.roundNumber}
                                            </td>
                                            <td>{r.bot1Move}</td>
                                            <td>{r.bot2Move}</td>
                                            <td>
                                                <Badge
                                                    bg={getResultBadgeClass(r.result)}
                                                    className="rounded-pill px-3"
                                                >
                                                    {r.result}
                                                </Badge>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={4} className="ps-3 text-secondary">
                                            No rounds available for this match.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </Table>
                    </div>
                </Card>
            </Container>
            <Footer />
        </div>
    );
}
