import { useState, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router";
import { Form, Button, Alert, Card } from "react-bootstrap";
import type { Route } from "./+types/signup";
import { registerUser } from "~/services/auth-service";
import { getMe } from "~/services/auth-service";
import { useAuthStore } from "~/stores/auth-store";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "Sign Up – Scissors, Please" },
        {
            name: "description",
            content: "Create your Scissors, Please account and start competing.",
        },
    ];
}

export default function Signup() {
    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { setUser, setInitialized } = useAuthStore();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }
        setLoading(true);
        try {
            const result = await registerUser({ username, email, password });
            if (result.ok) {
                const me = await getMe();
                setUser(me);
                setInitialized(true);
                navigate("/");
            } else {
                setError(result.error ?? "Registration failed. Please try again.");
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
                <Card.Body className="w-70">
                    <div className="text-center mb-4 w-70">
                        <a href="/" className="text-decoration-none">
                            <div className="auth-header-icon" style={{ fontSize: "3rem" }}>
                                ✂️
                            </div>
                            <h1 className="h4 fw-bold text-white mb-0">Scissors, Please</h1>
                        </a>
                        <p className="text-secondary mt-2">
                            Create your account to start competing.
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
                        <Form.Group className="mb-3" controlId="email">
                            <Form.Label className="text-uppercase small text-secondary">
                                Email Address
                            </Form.Label>
                            <Form.Control
                                className="form-control-lg"
                                type="email"
                                value={email}
                                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                    setEmail(e.target.value)
                                }
                                placeholder="name@example.com"
                                required
                                autoComplete="email"
                            />
                        </Form.Group>

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
                                placeholder="Choose a unique username"
                                required
                                autoComplete="username"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="password">
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
                                placeholder="Min. 8 characters"
                                minLength={8}
                                required
                                autoComplete="new-password"
                            />
                        </Form.Group>

                        <Form.Group className="mb-4" controlId="confirmPassword">
                            <Form.Label className="text-uppercase small text-secondary">
                                Confirm Password
                            </Form.Label>
                            <Form.Control
                                className="form-control-lg"
                                type="password"
                                value={confirmPassword}
                                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                                    setConfirmPassword(e.target.value)
                                }
                                placeholder="Confirm your password"
                                required
                                autoComplete="new-password"
                            />
                        </Form.Group>

                        <div className="d-grid mb-3">
                            <Button
                                type="submit"
                                className="btn-gradient-primary btn-lg fw-bold py-2"
                                disabled={loading}
                            >
                                {loading ? "Creating account…" : "Create Account"}
                            </Button>
                        </div>
                    </Form>

                    <div className="text-center">
                        <p className="text-secondary small mb-0">
                            Already have an account?{" "}
                            <a href="/login" className="text-primary text-decoration-none fw-bold">
                                Log in
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
