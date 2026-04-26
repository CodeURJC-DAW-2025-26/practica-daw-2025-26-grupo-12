import { useState, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router";
import { Form, Button, Alert, Card } from "react-bootstrap";
import type { Route } from "./+types/login";
import { logUser } from "~/services/auth-service";
import { getMe } from "~/services/auth-service";
import { useAuthStore } from "~/stores/auth-store";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Log In – Scissors, Please" },
        { name: "description", content: "Sign in to your Scissors, Please account." },
    ];
}

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { setUser, setInitialized } = useAuthStore();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const ok = await logUser({ username, password });
            if (ok) {
                const me = await getMe();
                setUser(me);
                setInitialized(true);
                navigate("/");
            } else {
                setError("Invalid username or password.");
            }
        } catch {
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="centered-layout">
            <Card className="glass-card p-4">
                <Card.Body>
                    <div className="text-center mb-4">
                        <a href="/" className="text-decoration-none">
                            <div className="auth-header-icon" style={{ fontSize: "3rem" }}>
                                ✂️
                            </div>
                            <h1 className="h4 fw-bold text-white mb-0">Scissors, Please</h1>
                        </a>
                        <p className="text-secondary mt-2">
                            Welcome back! Please enter your details.
                        </p>
                    </div>

                    {error && (
                        <Alert
                            variant="danger"
                            className="px-3 py-2 small mb-4"
                            dismissible
                            onClose={() => setError("")}
                        >
                            {error}
                        </Alert>
                    )}

                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="username">
                            <Form.Label className="text-uppercase small text-secondary">
                                Username
                            </Form.Label>
                            <Form.Control
                                className="form-control-lg"
                                type="text"
                                value={username}
                                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                    setUsername(e.target.value)
                                }
                                placeholder="Enter your username"
                                required
                                autoComplete="username"
                            />
                        </Form.Group>

                        <Form.Group className="mb-4" controlId="password">
                            <Form.Label className="text-uppercase small text-secondary">
                                Password
                            </Form.Label>
                            <Form.Control
                                className="form-control-lg"
                                type="password"
                                value={password}
                                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                    setPassword(e.target.value)
                                }
                                placeholder="••••••••"
                                required
                                autoComplete="current-password"
                            />
                        </Form.Group>

                        <div className="d-flex justify-content-between align-items-center mb-4">
                            <div className="form-check">
                                <input
                                    className="form-check-input"
                                    type="checkbox"
                                    id="rememberMe"
                                />
                                <label
                                    className="form-check-label text-secondary small"
                                    htmlFor="rememberMe"
                                >
                                    Remember me
                                </label>
                            </div>
                            <a href="#" className="small text-primary text-decoration-none">
                                Forgot password?
                            </a>
                        </div>

                        <div className="d-grid mb-3">
                            <Button
                                type="submit"
                                className="btn-gradient-primary btn-lg fw-bold py-2"
                                disabled={loading}
                            >
                                {loading ? "Signing in…" : "Log In"}
                            </Button>
                        </div>
                    </Form>

                    <div className="divider">or continue with</div>

                    <div className="d-grid gap-2 mb-4">
                        <a
                            href="/oauth2/authorization/google"
                            className="btn btn-outline-muted btn-sm text-decoration-none"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="16"
                                height="16"
                                fill="currentColor"
                                className="bi bi-google me-2"
                                viewBox="0 0 16 16"
                            >
                                <path d="M15.545 6.558a9.42 9.42 0 0 1 .139 1.626c0 2.434-.87 4.492-2.384 5.885h.002C11.978 15.292 10.158 16 8 16A8 8 0 1 1 8 0a7.689 7.689 0 0 1 5.352 2.082l-2.284 2.284A4.347 4.347 0 0 0 8 3.166c-2.087 0-3.86 1.408-4.492 3.304a4.792 4.792 0 0 0 0 3.063h.003c.635 1.893 2.405 3.301 4.492 3.301 1.078 0 2.004-.276 2.722-.764h-.003a3.702 3.702 0 0 0 1.599-2.431H8v-3.08h7.545z" />
                            </svg>
                            Continue with Google
                        </a>
                    </div>

                    <div className="text-center">
                        <p className="text-secondary small mb-0">
                            Don't have an account?{" "}
                            <a href="/signup" className="text-primary text-decoration-none fw-bold">
                                Sign up
                            </a>
                        </p>
                    </div>
                    <div className="text-center mt-3">
                        <a href="/" className="small text-secondary text-decoration-none">
                            Back to Home
                        </a>
                    </div>
                </Card.Body>
            </Card>
        </div>
    );
}
