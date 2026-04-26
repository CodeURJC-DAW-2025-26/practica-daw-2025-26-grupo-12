import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Table, Badge } from "react-bootstrap";
import type { Route } from "./+types/user-tournaments";
import { getMe } from "~/services/auth-service";
import { getUserTournaments } from "~/services/tournament-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "My Tournaments – Scissors, Please" },
        { name: "description", content: "View the tournaments you have joined." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    const tournamentPage = await getUserTournaments(user.id);
    return { user, tournamentPage };
}

export default function UserTournaments() {
    const { tournamentPage } = useLoaderData<typeof clientLoader>();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="mb-4">
                    <h1 className="h3 fw-bold mb-1">My Tournaments</h1>
                    <p className="text-secondary">
                        You are participating in {tournamentPage.totalElements} tournaments.
                    </p>
                </div>

                <Card className="p-0 glass-card border-0 overflow-hidden">
                    <div className="table-responsive">
                        <Table hover variant="dark" className="mb-0 align-middle">
                            <thead>
                                <tr className="text-secondary small text-uppercase">
                                    <th className="ps-4">Tournament</th>
                                    <th>Status</th>
                                    <th>Participants</th>
                                    <th>Start Date</th>
                                    <th className="text-end pe-4">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {tournamentPage.content.length > 0 ? (
                                    tournamentPage.content.map((t) => (
                                        <tr key={t.id}>
                                            <td className="ps-4">
                                                <div className="d-flex align-items-center gap-3">
                                                    <div
                                                        className="rounded bg-dark d-flex align-items-center justify-content-center"
                                                        style={{
                                                            width: 48,
                                                            height: 48,
                                                            overflow: "hidden",
                                                        }}
                                                    >
                                                        {t.imageUrl ? (
                                                            <img
                                                                src={t.imageUrl}
                                                                className="w-100 h-100"
                                                                style={{ objectFit: "cover" }}
                                                                alt={t.name}
                                                            />
                                                        ) : (
                                                            <span className="fs-4">🏆</span>
                                                        )}
                                                    </div>
                                                    <div className="fw-bold text-white">
                                                        {t.name}
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <Badge
                                                    bg={
                                                        t.status === "OPEN"
                                                            ? "success"
                                                            : t.status === "STARTED"
                                                              ? "primary"
                                                              : "secondary"
                                                    }
                                                    className="rounded-pill"
                                                >
                                                    {t.status}
                                                </Badge>
                                            </td>
                                            <td className="text-white">
                                                {t.participants} / {t.slots}
                                            </td>
                                            <td className="text-secondary">{t.startDate}</td>
                                            <td className="text-end pe-4">
                                                <Button
                                                    as={Link as any}
                                                    to={`/tournaments/${t.id}`}
                                                    variant="outline-light"
                                                    size="sm"
                                                >
                                                    View Tournament
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="py-5 text-center text-secondary">
                                            You haven't joined any tournaments yet.{" "}
                                            <Link to="/tournaments" className="text-primary">
                                                Browse all tournaments
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
