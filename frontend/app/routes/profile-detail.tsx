import { useLoaderData } from "react-router";
import { Container, Row, Col, Card, Image, Badge } from "react-bootstrap";
import type { Route } from "./+types/profile-detail";
import { getUserById } from "~/services/user-service";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const user = await getUserById(params.id);
    if (!user) throw new Response("User Not Found", { status: 404 });
    return { user };
}

export default function ProfileDetail() {
    const { user } = useLoaderData<typeof clientLoader>();
    const initialLetter = user.username.charAt(0).toUpperCase();

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 flex-grow-1">
                <Row className="justify-content-center">
                    <Col lg={8}>
                        <Card className="p-4 mb-4 glass-card border-0 text-center">
                            <div className="mb-4 d-flex justify-content-center">
                                <div
                                    className="position-relative"
                                    style={{ width: 120, height: 120 }}
                                >
                                    {user.imageUrl ? (
                                        <Image
                                            src={`/api/v1/images/users/${user.id}`}
                                            roundedCircle
                                            style={{
                                                width: 120,
                                                height: 120,
                                                objectFit: "cover",
                                            }}
                                            className="border border-primary border-3"
                                        />
                                    ) : (
                                        <div
                                            className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold mx-auto"
                                            style={{
                                                width: 120,
                                                height: 120,
                                                fontSize: "3rem",
                                            }}
                                        >
                                            {initialLetter}
                                        </div>
                                    )}
                                </div>
                            </div>
                            <h2 className="fw-bold mb-1">{user.username}</h2>
                            <p className="text-secondary">{user.email}</p>

                            <div className="mt-4">
                                <h5>Status</h5>
                                {user.blocked ? (
                                    <Badge bg="danger" className="px-3 py-2">
                                        Blocked
                                    </Badge>
                                ) : (
                                    <Badge bg="success" className="px-3 py-2">
                                        Active
                                    </Badge>
                                )}
                            </div>

                            <div className="mt-4 text-start">
                                <h5>Details</h5>
                                <p>
                                    <strong>Joined:</strong>{" "}
                                    {new Date(user.createdAt).toLocaleDateString()}
                                </p>
                            </div>
                        </Card>
                    </Col>
                </Row>
            </Container>
            <Footer />
        </div>
    );
}
