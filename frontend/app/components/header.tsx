import { Link, useNavigate } from "react-router";
import { Navbar, Nav, Container, Button } from "react-bootstrap";
import { useAuthStore } from "~/stores/auth-store";
import { logoutUser } from "~/services/auth-service";

export default function AppNavbar() {
    const { user, isAdmin, isLoggedIn, logout } = useAuthStore();
    const navigate = useNavigate();
    const admin = isAdmin();
    const loggedIn = isLoggedIn();

    async function handleLogout() {
        await logoutUser();
        logout();
        navigate("/", { replace: true });
    }

    return (
        <Navbar expand="lg" sticky="top" className="navbar-dark">
            <Container>
                <Navbar.Brand as={Link} to="/">
                    ✂️ Scissors, Please
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="main-nav" />
                <Navbar.Collapse id="main-nav">
                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/">
                            Home
                        </Nav.Link>
                        <Nav.Link as={Link} to="/matches">
                            Best Matches
                        </Nav.Link>
                        {!loggedIn && (
                            <Nav.Link as={Link} to="/bots">
                                Bots
                            </Nav.Link>
                        )}
                        {admin ? (
                            <>
                                <Nav.Link as={Link} to="/admin/tournaments">
                                    Tournaments
                                </Nav.Link>
                                <Nav.Link as={Link} to="/admin/users">
                                    Users
                                </Nav.Link>
                                <Nav.Link as={Link} to="/admin/bots">
                                    Bots
                                </Nav.Link>
                            </>
                        ) : (
                            <Nav.Link as={Link} to="/tournaments">
                                Tournaments
                            </Nav.Link>
                        )}
                    </Nav>

                    <div className="d-flex gap-2 align-items-center">
                        {loggedIn && admin && <span className="badge bg-primary me-2">Admin</span>}
                        {loggedIn && !admin && (
                            <Button
                                as={Link as any}
                                to="/profile"
                                variant="outline-secondary"
                                size="sm"
                            >
                                Profile
                            </Button>
                        )}
                        {loggedIn && (
                            <Button variant="primary" size="sm" onClick={handleLogout}>
                                Log Out
                            </Button>
                        )}
                        {!loggedIn && (
                            <>
                                <Button
                                    as={Link as any}
                                    to="/login"
                                    variant="outline-secondary"
                                    size="sm"
                                >
                                    Log In
                                </Button>
                                <Button as={Link as any} to="/signup" variant="primary" size="sm">
                                    Sign Up
                                </Button>
                            </>
                        )}
                    </div>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
