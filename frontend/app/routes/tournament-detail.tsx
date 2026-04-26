import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Badge } from "react-bootstrap";
import type { Route } from "./+types/tournament-detail";
import { getTournamentById } from "~/services/tournament-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";
import { useAuthStore } from "~/stores/auth-store";

export function meta({ data }: Route.MetaArgs) {
    const name = (data as any)?.tournament?.name ?? "Tournament";
    return [
        { title: `${name} – Scissors, Please` },
        { name: "description", content: `Details for the ${name} tournament.` },
    ];
}

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const id = Number(params.id);
    if (isNaN(id)) throw new Error("Invalid tournament ID");
    const tournament = await getTournamentById(id);
    return { tournament };
}

export function ErrorBoundary({ error }: { error: unknown }) {
    const msg = error instanceof Error ? error.message : "Tournament not found.";
    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 text-center">
                <h1 className="error-code">404</h1>
                <p className="text-secondary">{msg}</p>
                <Button as={Link as any} to="/tournaments" variant="primary">
                    All Tournaments
                </Button>
            </Container>
            <Footer />
        </div>
    );
}

export default function TournamentDetail() {
    const { tournament } = useLoaderData<typeof clientLoader>();
    const { isAdmin, isLoggedIn } = useAuthStore();
    const admin = isAdmin();
    const loggedIn = isLoggedIn();
    const isOpen = tournament.status === "OPEN";

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <Card className="border-0 bg-transparent mb-5">
                    <div className="d-flex flex-column flex-md-row align-items-center gap-4">
                        <div className="position-relative">
                            {tournament.imageUrl ? (
                                <img
                                    src={`/api/v1/images/tournaments/${tournament.id}`}
                                    className="rounded-4 border border-primary shadow-lg"
                                    style={{ width: 140, height: 140, objectFit: "cover" }}
                                    alt={tournament.name}
                                />
                            ) : (
                                <div
                                    className="rounded-4 bg-primary d-flex align-items-center justify-content-center text-white shadow-lg"
                                    style={{ width: 140, height: 140 }}
                                >
                                    <span className="display-4 fw-bold">T</span>
                                </div>
                            )}
                            {isOpen && (
                                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-success border border-dark">
                                    OPEN
                                </span>
                            )}
                        </div>

                        <div className="text-center text-md-start flex-grow-1">
                            <p className="text-primary mb-1 small text-uppercase fw-bold">
                                {admin
                                    ? "Admin Control Panel"
                                    : isOpen
                                      ? "Registration Phase"
                                      : "Tournament Details"}
                            </p>
                            <h1 className="display-5 fw-bold mb-2">{tournament.name}</h1>
                            <div className="d-flex flex-wrap justify-content-center justify-content-md-start gap-3 mt-3">
                                {admin ? (
                                    <>
                                        <Button
                                            as={Link as any}
                                            to={`/admin/tournaments/edit/${tournament.id}`}
                                            variant="primary"
                                            className="px-4"
                                        >
                                            Edit Settings
                                        </Button>
                                        <Button
                                            as={Link as any}
                                            to="/admin/tournaments"
                                            variant="outline-secondary"
                                            className="px-4"
                                        >
                                            Admin Dashboard
                                        </Button>
                                    </>
                                ) : (
                                    <>
                                        {loggedIn && isOpen && (
                                            <Button
                                                as={Link as any}
                                                to={`/tournaments/join/${tournament.id}`}
                                                variant="primary"
                                                className="px-5 fw-bold"
                                            >
                                                JOIN NOW
                                            </Button>
                                        )}
                                        <Button
                                            as={Link as any}
                                            to="/tournaments"
                                            variant="outline-secondary"
                                            className="px-4"
                                        >
                                            All Tournaments
                                        </Button>
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                </Card>

                <Row className="g-4">
                    <Col lg={8}>
                        <Card className="p-4 border-secondary h-100 shadow-sm">
                            <h3 className="h5 fw-bold mb-4 border-bottom border-secondary pb-2 text-uppercase small">
                                About this Tournament
                            </h3>
                            <div className="description-text text-light-emphasis mb-4">
                                {tournament.description ?? "No description provided."}
                            </div>
                        </Card>
                    </Col>

                    <Col lg={4}>
                        <div className="d-flex flex-column gap-4">
                            <Card className="p-4 bg-dark-subtle border-primary">
                                <div className="mb-4">
                                    <label className="text-secondary small text-uppercase d-block mb-1">
                                        Status
                                    </label>
                                    {isOpen ? (
                                        <Badge
                                            bg="success"
                                            className="px-3 py-2 border border-success w-100 text-center"
                                        >
                                            Open for Registration
                                        </Badge>
                                    ) : (
                                        <Badge
                                            bg="danger"
                                            className="px-3 py-2 border border-danger w-100 text-center"
                                        >
                                            Registration Closed
                                        </Badge>
                                    )}
                                </div>

                                <Row className="g-3">
                                    <Col xs={6} className="text-center border-end border-secondary">
                                        <label className="text-secondary small text-uppercase d-block mb-1">
                                            Players
                                        </label>
                                        <span className="h4 fw-bold">
                                            {tournament.participants}
                                        </span>
                                        <span className="text-secondary small">
                                            {" "}
                                            / {tournament.slots}
                                        </span>
                                    </Col>
                                </Row>

                                <div className="mt-4 pt-4 border-top border-secondary">
                                    <div className="d-flex align-items-center gap-3">
                                        <div className="bg-primary bg-opacity-10 p-2 rounded">
                                            <span className="text-primary fs-4">📅</span>
                                        </div>
                                        <div>
                                            <label className="text-secondary small text-uppercase d-block">
                                                Start Date
                                            </label>
                                            <span className="fw-bold">{tournament.startDate}</span>
                                        </div>
                                    </div>
                                </div>
                            </Card>

                            <Card className="p-4 border-secondary shadow-sm">
                                <h3 className="h6 fw-bold mb-3 text-uppercase">Tournament Rules</h3>
                                <div className="small">
                                    {[
                                        "Best of 20 rounds per match.",
                                        "Bots 24h before start.",
                                        "No external API calls.",
                                    ].map((rule) => (
                                        <div key={rule} className="d-flex gap-2 mb-2">
                                            <span className="text-primary">✓</span>
                                            <span>{rule}</span>
                                        </div>
                                    ))}
                                </div>
                            </Card>
                        </div>
                    </Col>
                </Row>
            </Container>
            <Footer />
        </div>
    );
}
