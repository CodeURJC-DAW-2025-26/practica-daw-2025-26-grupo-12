import { NavLink, Outlet } from "react-router";
import { Container, Row, Col, Nav } from "react-bootstrap";
import { RoleGuard } from "../components/auth/role-guard";

export default function AdminLayout() {
    return (
        <RoleGuard requireAdmin>
            <Container fluid className="p-0 overflow-hidden">
                <Row className="g-0 min-vh-100">
                    <Col
                        md={3}
                        lg={2}
                        className="glass-card m-0 rounded-0 border-0 border-end d-flex flex-column"
                        style={{
                            background: "rgba(15, 23, 42, 0.95)",
                            position: "sticky",
                            top: 0,
                            height: "100vh",
                            zIndex: 1000,
                        }}
                    >
                        <div className="p-4 border-bottom border-secondary mb-4">
                            <h4
                                className="m-0 text-gradient-primary fw-bold"
                                style={{
                                    background: "linear-gradient(45deg, #a78bfa, #8b5cf6)",
                                    WebkitBackgroundClip: "text",
                                    WebkitTextFillColor: "transparent",
                                }}
                            >
                                Admin Panel
                            </h4>
                            <small className="text-muted">Moderator Access</small>
                        </div>
                        <Nav className="flex-column flex-grow-1 px-3">
                            <Nav.Link
                                as={NavLink}
                                to="/admin/users"
                                className="text-white mb-2 p-3 rounded hover-link d-flex align-items-center gap-2"
                                style={{ transition: "all 0.2s" }}
                            >
                                <span>👥</span> Users
                            </Nav.Link>
                            <Nav.Link
                                as={NavLink}
                                to="/admin/bots"
                                className="text-white mb-2 p-3 rounded hover-link d-flex align-items-center gap-2"
                                style={{ transition: "all 0.2s" }}
                            >
                                <span>🤖</span> Bots
                            </Nav.Link>
                            <Nav.Link
                                as={NavLink}
                                to="/admin/tournaments"
                                className="text-white mb-2 p-3 rounded hover-link d-flex align-items-center gap-2"
                                style={{ transition: "all 0.2s" }}
                            >
                                <span>🏆</span> Tournaments
                            </Nav.Link>
                            <Nav.Link
                                as={NavLink}
                                to="/admin/notifications"
                                className="text-white mb-2 p-3 rounded hover-link d-flex align-items-center gap-2"
                                style={{ transition: "all 0.2s" }}
                            >
                                <span>📢</span> Notifications
                            </Nav.Link>
                        </Nav>
                        <div className="mt-auto p-4 border-top border-secondary">
                            <Nav.Link
                                as={NavLink}
                                to="/"
                                className="text-secondary small d-flex align-items-center gap-2"
                            >
                                <span>🏠</span> Back to Site
                            </Nav.Link>
                        </div>
                    </Col>
                    <Col md={9} lg={10} className="p-0 overflow-auto" style={{ height: "100vh" }}>
                        <div className="p-4 p-lg-5" style={{ minHeight: "100vh" }}>
                            <Outlet />
                        </div>
                    </Col>
                </Row>
            </Container>
        </RoleGuard>
    );
}
