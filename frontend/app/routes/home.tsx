import { useEffect } from "react";
import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Badge } from "react-bootstrap";
import type { Route } from "./+types/home";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";
import { useAuthStore, type AuthUser } from "~/stores/auth-store";
import { getMe } from "~/services/auth-service";
import { getMyBots } from "~/services/bot-service";
import type { BotDetail, MatchSummary, TournamentSummary } from "~/types";
import { getMyTournaments } from "~/services/tournament-service";
import { getRecentMatches } from "~/services/match-service";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Scissors, Please – Code. Compete. Conquer." },
        { name: "description", content: "The ultimate Rock, Paper, Scissors bot arena." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    const botsPage = user ? await getMyBots(user.id) : null;
    const tournamentsPage = user ? await getMyTournaments() : null;
    const matchesPage = user ? await getRecentMatches() : null;

    return {
        user,
        botsPage,
        tournamentsPage,
        matchesPage,
    };
}

export default function Home() {
    const {
        user: loadedUser,
        botsPage,
        tournamentsPage,
        matchesPage,
    } = useLoaderData<typeof clientLoader>();
    const { setUser, setInitialized, isAdmin, isLoggedIn } = useAuthStore();

    useEffect(() => {
        setUser(loadedUser);
        setInitialized(true);
    }, [loadedUser, setUser, setInitialized]);

    const loggedIn = isLoggedIn();
    const admin = isAdmin();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            {!loggedIn && <GuestHome />}
            {loggedIn && admin && <AdminHome />}
            {loggedIn && !admin && (
                <UserHome
                    user={loadedUser}
                    botsPage={botsPage}
                    tournamentsPage={tournamentsPage}
                    matchesPage={matchesPage}
                />
            )}
            <Footer />
        </div>
    );
}

function GuestHome() {
    return (
        <>
            <header className="hero-section text-center mb-5">
                <Container>
                    <Row className="justify-content-center">
                        <Col lg={8}>
                            <h1 className="display-4 fw-bold mb-3">Code. Compete. Conquer.</h1>
                            <p className="lead text-secondary mb-4">
                                Welcome to the ultimate <strong>Rock, Paper, Scissors</strong> bot
                                arena. Program your strategy, deploy your bot, and watch it climb
                                the global leaderboard.
                            </p>
                            <div className="d-flex justify-content-center gap-3">
                                <Button
                                    as={Link as any}
                                    to="/signup"
                                    variant="primary"
                                    size="lg"
                                    className="px-4"
                                >
                                    Enter the Arena
                                </Button>
                                <Button
                                    as={Link as any}
                                    to="/tournaments"
                                    variant="outline-secondary"
                                    size="lg"
                                    className="px-4"
                                >
                                    Browse Tournaments
                                </Button>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </header>

            <Container className="mb-5">
                <Row className="justify-content-center">
                    <Col lg={8}>
                        <Row className="g-4">
                            <Col md={6}>
                                <Card className="h-100 p-4 border-0 bg-transparent">
                                    <h2 className="h4 fw-bold mb-3 text-primary">
                                        🤖 The Challenge
                                    </h2>
                                    <p className="text-secondary">
                                        It's not just luck. It's psychology, probability, and
                                        pattern recognition. Can you write a bot that outsmarts the
                                        competition?
                                    </p>
                                </Card>
                            </Col>
                            <Col md={6}>
                                <Card className="h-100 p-4 border-0 bg-transparent">
                                    <h2 className="h4 fw-bold mb-3 text-primary">
                                        ⚔️ How It Works
                                    </h2>
                                    <p className="text-secondary">
                                        Upload your bot script. We run thousands of simulations
                                        against other players. Your ELO rating adjusts after every
                                        match.
                                    </p>
                                </Card>
                            </Col>
                            <Col xs={12} className="mt-4">
                                <Card className="p-4">
                                    <div className="d-flex align-items-start gap-3">
                                        <div className="fs-1">💡</div>
                                        <div>
                                            <h3 className="h5 fw-bold">Strategy Tip</h3>
                                            <p className="mb-0 text-secondary">
                                                Simple randomized strategies often beat predictable
                                                patterns. But the best bots analyze their opponent's
                                                history to predict the next move.
                                            </p>
                                        </div>
                                    </div>
                                </Card>
                            </Col>
                        </Row>
                    </Col>
                </Row>
            </Container>
        </>
    );
}

function AdminHome() {
    return (
        <header className="hero-section text-center mb-5">
            <Container>
                <Row className="justify-content-center">
                    <Col lg={8}>
                        <h1 className="display-4 fw-bold mb-3">Admin Dashboard</h1>
                        <p className="lead text-secondary mb-4">
                            Welcome back, Administrator. Manage users, bots, and tournaments from
                            here.
                        </p>
                        <div className="d-flex justify-content-center gap-3">
                            <Button
                                as={Link as any}
                                to="/admin/users"
                                variant="primary"
                                size="lg"
                                className="px-4"
                            >
                                Users
                            </Button>
                            <Button
                                as={Link as any}
                                to="/admin/bots"
                                variant="primary"
                                size="lg"
                                className="px-4"
                            >
                                Bots
                            </Button>
                            <Button
                                as={Link as any}
                                to="/admin/tournaments"
                                variant="primary"
                                size="lg"
                                className="px-4"
                            >
                                Tournaments
                            </Button>
                        </div>
                    </Col>
                </Row>
            </Container>
        </header>
    );
}

function UserHome({
    user,
    botsPage,
    tournamentsPage,
    matchesPage,
}: {
    user: AuthUser | null;
    botsPage: any;
    tournamentsPage: any;
    matchesPage: any;
}) {
    if (!user) return null;
    const initial = user.username.charAt(0).toUpperCase();

    const bots: BotDetail[] = botsPage?.content ?? [];
    const hasBots = bots.length > 0;

    const tournaments: TournamentSummary[] = tournamentsPage?.content ?? [];
    const hasTournaments = tournaments.length > 0;

    const matches: MatchSummary[] = matchesPage?.content ?? [];
    const hasMatches = matches.length > 0;

    return (
        <>
            <header className="hero-section mb-5">
                <Container>
                    <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-4">
                        <div className="d-flex align-items-center gap-3">
                            <div className="position-relative" style={{ width: 64, height: 64 }}>
                                {user.imageUrl ? (
                                    <img
                                        src={`/api/v1/images/users/${user.id}`}
                                        className="rounded-circle border border-primary"
                                        style={{ width: 64, height: 64, objectFit: "cover" }}
                                        alt={user.username}
                                    />
                                ) : (
                                    <div
                                        className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                        style={{ width: 64, height: 64, fontSize: "1.5rem" }}
                                    >
                                        {initial}
                                    </div>
                                )}
                            </div>
                            <div>
                                <h1 className="h3 fw-bold mb-1">{user.username}</h1>
                                <p className="text-secondary mb-0">
                                    Logged in • {bots.length} {bots.length === 1 ? "bot" : "bots"}
                                </p>
                            </div>
                        </div>
                        <div className="d-flex flex-wrap gap-3 align-items-center">
                            <Button
                                as={Link as any}
                                to="/bots/create"
                                variant="primary"
                                size="lg"
                                className="px-4"
                            >
                                Create Bot
                            </Button>
                            <Button
                                as={Link as any}
                                to="/matches/search"
                                className="btn-outline-muted px-4"
                                variant="outline"
                                size="lg"
                            >
                                Find Match
                            </Button>
                        </div>
                    </div>
                </Container>
            </header>

            <Container className="mb-5">
                <Row className="g-4">
                    <Col lg={8}>
                        <Row className="g-4">
                            <Col md={6}>
                                <Card className="p-4 h-100">
                                    <h2 className="h5 fw-bold mb-3">My Bots</h2>
                                    {hasBots ? (
                                        <>
                                            <div className="list-group list-group-flush mb-3">
                                                {bots.slice(0, 5).map((bot) => (
                                                    <div
                                                        key={bot.id}
                                                        className="list-group-item bg-transparent border-secondary px-0 d-flex justify-content-between align-items-center"
                                                    >
                                                        <Link
                                                            to={`/bots/${bot.id}`}
                                                            className="text-white text-decoration-none hover-link"
                                                        >
                                                            {bot.name}
                                                        </Link>
                                                        <Badge bg="secondary" pill>
                                                            {bot.elo} ELO
                                                        </Badge>
                                                    </div>
                                                ))}
                                            </div>
                                            <Button
                                                as={Link as any}
                                                to="/bots/user-bots"
                                                variant="outline-secondary"
                                                size="sm"
                                            >
                                                Manage Bots
                                            </Button>
                                        </>
                                    ) : (
                                        <>
                                            <p className="text-secondary mb-3">
                                                You don't have any bots yet.
                                            </p>
                                            <Button
                                                as={Link as any}
                                                to="/bots/create"
                                                variant="primary"
                                                size="sm"
                                            >
                                                Create Bot
                                            </Button>
                                        </>
                                    )}
                                </Card>
                            </Col>
                            <Col md={6}>
                                <Card className="p-4 h-100">
                                    <h2 className="h5 fw-bold mb-3">Recent Matches</h2>
                                    {hasMatches ? (
                                        <>
                                            <div className="list-group list-group-flush mb-3">
                                                {matches.slice(0, 5).map((match) => (
                                                    <div
                                                        key={match.id}
                                                        className="list-group-item bg-transparent border-secondary px-0 d-flex justify-content-between align-items-center"
                                                    >
                                                        <div className="small">
                                                            <div className="text-white fw-semibold">
                                                                Match #{match.id}
                                                            </div>
                                                            <div className="text-muted">
                                                                {match.bot1Name} vs {match.bot2Name}
                                                            </div>
                                                        </div>
                                                        <Badge
                                                            bg={
                                                                match.winnerBotId
                                                                    ? "primary"
                                                                    : "secondary"
                                                            }
                                                            pill
                                                        >
                                                            {match.winnerBotId ? "FINISH" : "DRAW"}
                                                        </Badge>
                                                    </div>
                                                ))}
                                            </div>
                                            <Button
                                                as={Link as any}
                                                to="/matches/recent"
                                                variant="outline-secondary"
                                                size="sm"
                                            >
                                                View Matches
                                            </Button>
                                        </>
                                    ) : (
                                        <>
                                            <p className="text-secondary mb-3">
                                                You have no recent matches yet.
                                            </p>
                                            <Button
                                                as={Link as any}
                                                to="/matches/search"
                                                variant="outline-secondary"
                                                size="sm"
                                            >
                                                Find Match
                                            </Button>
                                        </>
                                    )}
                                </Card>
                            </Col>
                            <Col xs={12}>
                                <Card className="p-4">
                                    <h2 className="h5 fw-bold mb-3">My Tournaments</h2>
                                    {hasTournaments ? (
                                        <>
                                            <div className="list-group list-group-flush mb-3">
                                                {tournaments.slice(0, 5).map((t) => (
                                                    <div
                                                        key={t.id}
                                                        className="list-group-item bg-transparent border-secondary px-0 d-flex justify-content-between align-items-center"
                                                    >
                                                        <div>
                                                            <div className="text-white fw-semibold">
                                                                {t.name}
                                                            </div>
                                                            <div className="text-muted small">
                                                                {t.startDate}
                                                            </div>
                                                        </div>
                                                        <Badge
                                                            bg={
                                                                t.status === "OPEN"
                                                                    ? "success"
                                                                    : t.status === "RUNNING"
                                                                      ? "warning"
                                                                      : "secondary"
                                                            }
                                                            pill
                                                        >
                                                            {t.status}
                                                        </Badge>
                                                    </div>
                                                ))}
                                            </div>
                                            <Button
                                                as={Link as any}
                                                to="/tournaments/my-tournaments"
                                                variant="outline-secondary"
                                                size="sm"
                                            >
                                                Manage Tournaments
                                            </Button>
                                        </>
                                    ) : (
                                        <>
                                            <p className="text-secondary mb-3">
                                                You are not registered in any tournament yet.
                                            </p>
                                            <Button
                                                as={Link as any}
                                                to="/tournaments"
                                                variant="primary"
                                                size="sm"
                                            >
                                                Browse Tournaments
                                            </Button>
                                        </>
                                    )}
                                </Card>
                            </Col>
                        </Row>
                    </Col>
                    <Col lg={4}>
                        <Card className="p-4 mb-4 shadow-sm border-0 bg-dark text-white">
                            <h2 className="h5 fw-bold mb-3">My Performance</h2>
                            <div className="d-flex justify-content-between text-secondary small mb-2">
                                <span>Win Rate</span>
                                <span>0.0%</span>
                            </div>
                            <div className="mb-3">
                                <img
                                    src="/api/v1/charts/progress?current=0&max=100&color=34,197,94"
                                    className="img-fluid rounded"
                                    alt="Win Rate Progress"
                                />
                            </div>
                            <div className="d-flex justify-content-between text-secondary small mb-2">
                                <span>Current ELO (Peak)</span>
                                <span>0</span>
                            </div>
                            <div>
                                <img
                                    src="/api/v1/charts/progress?current=0&max=3000&color=139,92,246"
                                    className="img-fluid rounded"
                                    alt="ELO Progress"
                                />
                            </div>
                        </Card>
                        <Card className="p-4 border-0 bg-dark text-white shadow-sm">
                            <h2 className="h5 fw-bold mb-3">My Rankings</h2>
                            <p className="text-secondary mb-2 small">
                                Current placement in the global ladder.
                            </p>
                            <p className="mb-0 text-secondary small">
                                Create and play with at least one bot to get your ranking.
                            </p>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </>
    );
}
