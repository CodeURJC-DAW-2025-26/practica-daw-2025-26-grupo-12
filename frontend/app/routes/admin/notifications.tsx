import { useState } from "react";
import { Form, Button, Row, Col, Alert, Badge } from "react-bootstrap";

export default function AdminNotifications() {
    const [usernames, setUsernames] = useState("");
    const [message, setMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!usernames.trim() || !message.trim()) return;

        setIsSubmitting(true);
        setSuccess(false);
        setErrorMsg("");

        try {
            const parsedUsernames = usernames
                .split(",")
                .map((u) => u.trim())
                .filter((u) => u.length > 0);

            const req = await fetch("/api/v1/notifications/admin", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ usernames: parsedUsernames, message }),
            });

            if (!req.ok) throw new Error("Failed to broadcast notifications");

            setSuccess(true);
            setUsernames("");
            setMessage("");
        } catch (error) {
            setErrorMsg("Error broadcasting notifications.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="admin-notifications-page animate__animated animate__fadeIn">
            <div className="d-flex justify-content-between align-items-center mb-5 mt-2">
                <div>
                    <h2 className="fw-bold m-0">Notification Broadcaster</h2>
                    <p className="text-muted m-0">Send direct notifications to specific users</p>
                </div>
            </div>

            {success && (
                <Alert variant="success" onClose={() => setSuccess(false)} dismissible>
                    <div className="d-flex align-items-center">
                        <span className="fs-4 me-3">✅</span>
                        <div>
                            <h5 className="alert-heading fw-bold mb-1">Notifications Sent</h5>
                            <p className="mb-0 small">
                                The messages have been successfully dispatched to the target users.
                            </p>
                        </div>
                    </div>
                </Alert>
            )}

            {errorMsg && (
                <Alert variant="danger" onClose={() => setErrorMsg("")} dismissible>
                    <div className="d-flex align-items-center">
                        <span className="fs-4 me-3">⚠️</span>
                        <div>
                            <h5 className="alert-heading fw-bold mb-1">Error</h5>
                            <p className="mb-0 small">{errorMsg}</p>
                        </div>
                    </div>
                </Alert>
            )}

            <div className="glass-card p-5">
                <Form onSubmit={handleSubmit}>
                    <Row className="g-4 mb-4">
                        <Col md={12}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light d-flex align-items-center gap-2">
                                    Target Users <Badge bg="primary">Required</Badge>
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="text"
                                    placeholder="e.g. admin, bot_master, john_doe"
                                    value={usernames}
                                    onChange={(e) => setUsernames(e.target.value)}
                                    className="bg-dark text-light border-secondary form-control-lg"
                                />
                                <Form.Text className="text-muted">
                                    Comma-separated list of usernames to send the notification to.
                                </Form.Text>
                            </Form.Group>
                        </Col>
                    </Row>

                    <Row className="g-4 mb-4">
                        <Col md={12}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light d-flex align-items-center gap-2">
                                    Message Body <Badge bg="primary">Required</Badge>
                                </Form.Label>
                                <Form.Control
                                    required
                                    as="textarea"
                                    rows={5}
                                    placeholder="Enter the notification message..."
                                    value={message}
                                    onChange={(e) => setMessage(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                            </Form.Group>
                        </Col>
                    </Row>

                    <div className="mt-5 text-end border-top border-secondary pt-4">
                        <Button
                            type="button"
                            variant="secondary"
                            onClick={() => {
                                setUsernames("");
                                setMessage("");
                            }}
                            className="me-3 px-4 rounded-pill"
                        >
                            Clear
                        </Button>
                        <Button
                            type="submit"
                            variant="primary"
                            className="btn-gradient-primary rounded-pill px-5"
                            disabled={isSubmitting || !usernames || !message}
                        >
                            {isSubmitting ? "Sending..." : "Send Notifications"}
                        </Button>
                    </div>
                </Form>
            </div>

            <Row className="mt-5">
                <Col md={6}>
                    <div className="glass-card p-4">
                        <h5 className="fw-bold text-warning mb-3">⚠️ Administrative Note</h5>
                        <p className="text-muted small mb-0">
                            Notifications sent from this panel will appear in the target users'
                            notification hubs immediately on their next poll or page refresh. Use
                            wisely to communicate tournament results, moderation warnings, or system
                            updates.
                        </p>
                    </div>
                </Col>
            </Row>
        </div>
    );
}
