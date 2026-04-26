import { useState, useEffect } from "react";
import { Link, useLoaderData } from "react-router";
import { Container, Row, Col, Card, Button, Image } from "react-bootstrap";
import type { Route } from "./+types/battle";
import { getMe } from "~/services/auth-service";
import { getMatchBattle } from "~/services/match-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Arena Battle – Scissors, Please" },
        { name: "description", content: "Witness the clash of the bots." },
    ];
}

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    const id = Number(params.id);
    const battle = await getMatchBattle(id);
    return { battle };
}

export default function Battle() {
    const { battle } = useLoaderData<typeof clientLoader>();
    const [animationState, setAnimationState] = useState<"idle" | "clash" | "finish">("idle");

    useEffect(() => {
        const timer1 = setTimeout(() => setAnimationState("clash"), 1000);
        const timer2 = setTimeout(() => setAnimationState("finish"), 3000);
        return () => {
            clearTimeout(timer1);
            clearTimeout(timer2);
        };
    }, []);

    return (
        <div className="d-flex flex-column min-vh-100 bg-black overflow-hidden">
            <AppNavbar />
            <Container className="flex-grow-1 d-flex align-items-center justify-content-center py-5">
                <Col lg={10}>
                    <div className="arena-battle position-relative mb-5">
                        <Row className="align-items-center justify-content-between g-0">
                            {/* Bot 1 */}
                            <Col xs={5} className={`battle-side left ${animationState}`}>
                                <Card className="p-4 glass-card border-0 text-center">
                                    <div className="avatar-wrapper mb-3 mx-auto">
                                        {battle.bot1HasImage ? (
                                            <Image
                                                src={`/api/v1/images/bots/${battle.bot1Id}`}
                                                className="battle-avatar rounded-circle border border-primary border-4"
                                            />
                                        ) : (
                                            <div className="battle-avatar-placeholder bg-primary rounded-circle d-flex align-items-center justify-content-center text-white fw-bold display-4">
                                                {battle.bot1Initial}
                                            </div>
                                        )}
                                    </div>
                                    <h2 className="h4 fw-bold mb-1">{battle.bot1Name}</h2>
                                    <p className="text-secondary small mb-0">
                                        by {battle.bot1OwnerName}
                                    </p>
                                </Card>
                            </Col>

                            {/* VS Center */}
                            <Col xs={2} className="text-center position-relative">
                                <div className={`vs-logo ${animationState}`}>
                                    <span className="display-1 fw-black text-primary font-italic">
                                        VS
                                    </span>
                                </div>
                            </Col>

                            {/* Bot 2 */}
                            <Col xs={5} className={`battle-side right ${animationState}`}>
                                <Card className="p-4 glass-card border-0 text-center">
                                    <div className="avatar-wrapper mb-3 mx-auto">
                                        {battle.bot2HasImage ? (
                                            <Image
                                                src={`/api/v1/images/bots/${battle.bot2Id}`}
                                                className="battle-avatar rounded-circle border border-danger border-4"
                                            />
                                        ) : (
                                            <div className="battle-avatar-placeholder bg-danger rounded-circle d-flex align-items-center justify-content-center text-white fw-bold display-4">
                                                {battle.bot2Initial}
                                            </div>
                                        )}
                                    </div>
                                    <h2 className="h4 fw-bold mb-1">{battle.bot2Name}</h2>
                                    <p className="text-secondary small mb-0">
                                        by {battle.bot2OwnerName}
                                    </p>
                                </Card>
                            </Col>
                        </Row>

                        {/* Effects */}
                        <div className={`sparks ${animationState}`}></div>
                    </div>

                    <div className={`text-center result-btn-container ${animationState}`}>
                        <Button
                            as={Link as any}
                            to={`/matches/${battle.matchId}/stats`}
                            variant="primary"
                            size="lg"
                            className="px-5 rounded-pill shadow-lg"
                        >
                            View Result Details
                        </Button>
                    </div>
                </Col>
            </Container>
            <Footer />
        </div>
    );
}
