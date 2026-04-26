import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Table, Badge } from "react-bootstrap";
import type { Route } from "./+types/user-matches";
import { getMe } from "~/services/auth-service";
import { getRecentMatches } from "~/services/match-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "My Matches – Scissors, Please" },
        { name: "description", content: "View your recent battle history." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    const recentMatches = await getRecentMatches();
    return { user, recentMatches };
}

export default function UserMatches() {
    const { recentMatches } = useLoaderData<typeof clientLoader>();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="mb-4">
                    <h1 className="h3 fw-bold mb-1">My Match History</h1>
                    <p className="text-secondary">
                        You have played {recentMatches.totalMatches} matches total.
                    </p>
                </div>

                <Card className="p-0 glass-card border-0 overflow-hidden">
                    <div className="table-responsive">
                        <Table hover variant="dark" className="mb-0 align-middle">
                            <thead>
                                <tr className="text-secondary small text-uppercase">
                                    <th className="ps-4">Matchup</th>
                                    <th>Winner</th>
                                    <th>Max ELO</th>
                                    <th>Played At</th>
                                    <th className="text-end pe-4">Details</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentMatches.matches.length > 0 ? (
                                    recentMatches.matches.map((match) => (
                                        <tr key={match.id}>
                                            <td className="ps-4">
                                                <div className="d-flex align-items-center gap-2">
                                                    <span className="fw-bold">
                                                        {match.bot1Name}
                                                    </span>
                                                    <span className="text-secondary small">vs</span>
                                                    <span className="fw-bold">
                                                        {match.bot2Name}
                                                    </span>
                                                </div>
                                            </td>
                                            <td>
                                                {match.winnerBotId ? (
                                                    <Badge bg="success" className="rounded-pill">
                                                        {match.winnerBotId === match.bot1Id
                                                            ? match.bot1Name
                                                            : match.bot2Name}
                                                    </Badge>
                                                ) : (
                                                    <Badge bg="secondary" className="rounded-pill">
                                                        Draw
                                                    </Badge>
                                                )}
                                            </td>
                                            <td className="text-secondary">{match.maxElo}</td>
                                            <td className="text-secondary">{match.playedAt}</td>
                                            <td className="text-end pe-4">
                                                <Button
                                                    as={Link as any}
                                                    to={`/matches/${match.id}/stats`}
                                                    variant="outline-light"
                                                    size="sm"
                                                >
                                                    View Stats
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="py-5 text-center text-secondary">
                                            No recent matches.{" "}
                                            <Link to="/matches/search" className="text-primary">
                                                Start matchmaking!
                                            </Link>
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
