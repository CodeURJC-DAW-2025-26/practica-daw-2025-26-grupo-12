import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Table } from "react-bootstrap";
import type { Route } from "./+types/user-bots";
import { getMe } from "~/services/auth-service";
import { getMyBots } from "~/services/bot-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "My Bots – Scissors, Please" },
        { name: "description", content: "Manage your collection of bots." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    const botPage = await getMyBots(user.id);
    return { user, botPage };
}

export default function UserBots() {
    const { botPage } = useLoaderData<typeof clientLoader>();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h1 className="h3 fw-bold mb-1">My Bots</h1>
                        <p className="text-secondary mb-0">
                            {botPage.totalElements} bots in your collection.
                        </p>
                    </div>
                    <Button as={Link as any} to="/bots/create" variant="primary" className="px-4">
                        Create New Bot
                    </Button>
                </div>

                <Card className="p-0 glass-card border-0 overflow-hidden">
                    <div className="table-responsive">
                        <Table hover variant="dark" className="mb-0 align-middle">
                            <thead>
                                <tr className="text-secondary small text-uppercase">
                                    <th className="ps-4">Bot</th>
                                    <th>Status</th>
                                    <th>ELO</th>
                                    <th>Record (W/L/D)</th>
                                    <th className="text-end pe-4">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {botPage.content.length > 0 ? (
                                    botPage.content.map((bot) => (
                                        <tr key={bot.id}>
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
                                                        {bot.imageUrl ? (
                                                            <img
                                                                src={bot.imageUrl}
                                                                className="w-100 h-100"
                                                                style={{ objectFit: "cover" }}
                                                                alt={bot.name}
                                                            />
                                                        ) : (
                                                            <span className="fs-4">🤖</span>
                                                        )}
                                                    </div>
                                                    <div>
                                                        <div className="fw-bold text-white">
                                                            {bot.name}
                                                        </div>
                                                        <div className="text-secondary small">
                                                            {bot.ownerUsername}
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                {bot.public ? (
                                                    <Badge bg="success" className="rounded-pill">
                                                        Public
                                                    </Badge>
                                                ) : (
                                                    <Badge bg="secondary" className="rounded-pill">
                                                        Private
                                                    </Badge>
                                                )}
                                            </td>
                                            <td className="fw-bold text-primary">{bot.elo}</td>
                                            <td>
                                                <span className="text-success">{bot.wins}W</span>
                                                <span className="text-secondary mx-1">/</span>
                                                <span className="text-danger">{bot.losses}L</span>
                                                <span className="text-secondary mx-1">/</span>
                                                <span className="text-secondary">{bot.draws}D</span>
                                            </td>
                                            <td className="text-end pe-4">
                                                <div className="d-flex justify-content-end gap-2">
                                                    <Button
                                                        as={Link as any}
                                                        to={`/bots/${bot.id}`}
                                                        variant="outline-light"
                                                        size="sm"
                                                    >
                                                        Details
                                                    </Button>
                                                    <Button
                                                        as={Link as any}
                                                        to={`/bots/${bot.id}/edit`}
                                                        variant="primary"
                                                        size="sm"
                                                    >
                                                        Edit
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="py-5 text-center text-secondary">
                                            You haven't created any bots yet.
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

function Badge({
    children,
    bg,
    className,
}: {
    children: React.ReactNode;
    bg: string;
    className?: string;
}) {
    return <span className={`badge bg-${bg} ${className}`}>{children}</span>;
}
