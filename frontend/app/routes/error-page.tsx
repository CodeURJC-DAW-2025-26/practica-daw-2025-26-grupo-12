import { Link, useRouteError, isRouteErrorResponse } from "react-router";
import { Container, Row, Col, Button } from "react-bootstrap";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export default function ErrorPage() {
    const error = useRouteError();

    let status = 500;
    let message = "The page you're looking for doesn't exist or has been moved.";

    if (isRouteErrorResponse(error)) {
        status = error.status;
        message = error.statusText || message;
        if (error.status === 404) message = "Page not found.";
        if (error.status === 403) message = "You do not have permission to view this page.";
    } else if (error instanceof Error) {
        message = error.message;
    }

    return (
        <div className="d-flex flex-column min-vh-100">
            <AppNavbar />
            <Container className="py-5 flex-grow-1 d-flex align-items-center">
                <Row className="justify-content-center w-100">
                    <Col md={8} lg={6} className="text-center">
                        <h1 className="error-code mb-4">{status}</h1>
                        <h2 className="h3 fw-bold mb-3">{message}</h2>
                        <div className="d-flex gap-2 justify-content-center">
                            <Button as={Link as any} to="/" variant="primary" className="px-4 py-2">
                                Go Home
                            </Button>
                            <Button
                                onClick={() => window.history.back()}
                                variant="outline-secondary"
                                className="px-4 py-2"
                            >
                                Go Back
                            </Button>
                        </div>
                    </Col>
                </Row>
            </Container>
            <Footer />
        </div>
    );
}
