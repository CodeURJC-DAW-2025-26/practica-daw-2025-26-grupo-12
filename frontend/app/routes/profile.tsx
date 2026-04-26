import { useState, type ChangeEvent } from "react";
import { useLoaderData, useNavigate } from "react-router";
import { Container, Row, Col, Card, Form, Button, Alert, Image } from "react-bootstrap";
import type { Route } from "./+types/profile";
import { getMe } from "~/services/auth-service";
import { updateUserProfile, type UserProfileData } from "~/services/user-service";
import { useAuthStore } from "~/stores/auth-store";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export function meta(_args: Route.MetaArgs) {
    return [
        { title: "My Profile – Scissors, Please" },
        { name: "description", content: "View and edit your profile settings." },
    ];
}

export async function clientLoader() {
    const user = await getMe();
    if (!user) throw new Response("Unauthorized", { status: 401 });
    return { user };
}

export default function Profile() {
    const { user: initialUser } = useLoaderData<typeof clientLoader>();
    const { setUser } = useAuthStore();
    const navigate = useNavigate();

    const [username, setUsername] = useState(initialUser.username);
    const [email, setEmail] = useState(initialUser.email);
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [imageFile, setImageFile] = useState<File | null>(null);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setImageFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        if (password && password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);
        try {
            const result = await updateUserProfile(
                initialUser.id,
                { username, email, password: password || undefined },
                imageFile || undefined
            );

            if (result.ok && result.user) {
                setSuccess("Profile updated successfully!");
                setUser(result.user);
                setPassword("");
                setConfirmPassword("");
                setImageFile(null);
            } else {
                setError(result.error || "Failed to update profile.");
            }
        } catch (err) {
            setError("An unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    };

    const initialLetter = initialUser.username.charAt(0).toUpperCase();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5">
                <Row className="justify-content-center">
                    <Col lg={8}>
                        <div className="mb-4">
                            <h1 className="h3 fw-bold mb-1">Account Settings</h1>
                            <p className="text-secondary">Manage your profile and security.</p>
                        </div>

                        {error && (
                            <Alert variant="danger" dismissible onClose={() => setError("")}>
                                {error}
                            </Alert>
                        )}
                        {success && (
                            <Alert variant="success" dismissible onClose={() => setSuccess("")}>
                                {success}
                            </Alert>
                        )}

                        <Form onSubmit={handleSubmit}>
                            <Card className="p-4 mb-4 glass-card border-0">
                                <h2 className="h5 fw-bold mb-4">Personal Information</h2>
                                <Row className="align-items-center mb-4">
                                    <Col xs="auto">
                                        <div
                                            className="position-relative"
                                            style={{ width: 100, height: 100 }}
                                        >
                                            {initialUser.imageUrl ? (
                                                <Image
                                                    src={`/api/v1/images/users/${initialUser.id}?t=${Date.now()}`}
                                                    roundedCircle
                                                    style={{
                                                        width: 100,
                                                        height: 100,
                                                        objectFit: "cover",
                                                    }}
                                                    className="border border-primary border-3"
                                                />
                                            ) : (
                                                <div
                                                    className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                                    style={{
                                                        width: 100,
                                                        height: 100,
                                                        fontSize: "2.5rem",
                                                    }}
                                                >
                                                    {initialLetter}
                                                </div>
                                            )}
                                        </div>
                                    </Col>
                                    <Col>
                                        <Form.Group controlId="imageFile">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                Change Avatar
                                            </Form.Label>
                                            <Form.Control
                                                type="file"
                                                accept="image/*"
                                                onChange={handleImageChange}
                                                className="bg-dark text-light border-secondary"
                                            />
                                            <Form.Text className="text-secondary">
                                                JPG, PNG or GIF. Max 5MB.
                                            </Form.Text>
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Group controlId="username">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                Username
                                            </Form.Label>
                                            <Form.Control
                                                type="text"
                                                value={username}
                                                onChange={(e) => setUsername(e.target.value)}
                                                required
                                                className="form-control-lg"
                                            />
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group controlId="email">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                Email Address
                                            </Form.Label>
                                            <Form.Control
                                                type="email"
                                                value={email}
                                                onChange={(e) => setEmail(e.target.value)}
                                                required
                                                className="form-control-lg"
                                            />
                                        </Form.Group>
                                    </Col>
                                </Row>
                            </Card>

                            <Card className="p-4 mb-4 glass-card border-0">
                                <h2 className="h5 fw-bold mb-4">Security</h2>
                                <p className="text-secondary small mb-4">
                                    Leave blank if you don't want to change your password.
                                </p>
                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Group controlId="password">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                New Password
                                            </Form.Label>
                                            <Form.Control
                                                type="password"
                                                value={password}
                                                onChange={(e) => setPassword(e.target.value)}
                                                placeholder="••••••••"
                                                className="form-control-lg"
                                            />
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group controlId="confirmPassword">
                                            <Form.Label className="text-secondary small text-uppercase fw-bold">
                                                Confirm New Password
                                            </Form.Label>
                                            <Form.Control
                                                type="password"
                                                value={confirmPassword}
                                                onChange={(e) => setConfirmPassword(e.target.value)}
                                                placeholder="••••••••"
                                                className="form-control-lg"
                                            />
                                        </Form.Group>
                                    </Col>
                                </Row>
                            </Card>

                            <div className="d-flex justify-content-end gap-3">
                                <Button
                                    variant="outline-secondary"
                                    onClick={() => navigate("/")}
                                    disabled={loading}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    variant="primary"
                                    type="submit"
                                    disabled={loading}
                                    className="px-4"
                                >
                                    {loading ? "Saving Changes..." : "Save Changes"}
                                </Button>
                            </div>
                        </Form>
                    </Col>
                </Row>
            </Container>
            <Footer />
        </div>
    );
}
