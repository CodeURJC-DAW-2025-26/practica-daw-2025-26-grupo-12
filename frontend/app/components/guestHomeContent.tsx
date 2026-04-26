import { Container, Row, Col, Card } from "react-bootstrap";

export default function GuestHomeContent() {
    return (
        <>
            <header className="hero-section text-center mb-5">
                <Container>
                    <Row className="justify-content-center">
                        <Col lg={8}>
                            <h1 className="display-4 fw-bold mb-3">
                                Code. Compete. Conquer.
                            </h1>

                            <p className="lead text-secondary mb-4">
                                Welcome to the ultimate <strong>Rock, Paper, Scissors</strong>{" "}
                                bot arena. Program your strategy, deploy your bot, and watch
                                it climb the global leaderboard.
                            </p>

                            <div className="d-flex justify-content-center gap-3">
                                <a
                                    href="/sign-up"
                                    className="btn btn-primary btn-lg px-4"
                                >
                                    Enter the Area
                                </a>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </header>

            <Container className="mb-5">
                <Row className="justify-content-center">
                    <Col lg={8} as="main" id="about">
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