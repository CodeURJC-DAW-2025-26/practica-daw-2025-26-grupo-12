import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { Form, Button, Row, Col } from "react-bootstrap";
import { adminTournamentService } from "../../services/admin-tournament-service";

export default function AdminTournamentNew() {
    const navigate = useNavigate();
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [slots, setSlots] = useState("");
    const [price, setPrice] = useState("");
    const [startDate, setStartDate] = useState("");
    const [registrationStarts, setRegistrationStarts] = useState("");
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);
    const [validated, setValidated] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setImageFile(file);
            const objectUrl = URL.createObjectURL(file);
            setImagePreview(objectUrl);
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const form = e.currentTarget;
        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        setIsSubmitting(true);
        try {
            await adminTournamentService.createTournament({
                name,
                description,
                slots: Number(slots),
                price: Number(price),
                registrationStarts: new Date(registrationStarts).toISOString(),
                startDate: new Date(startDate).toISOString(),
                imageFile: imageFile || undefined,
            });
            navigate("/admin/tournaments");
        } catch (error) {
            alert("Error creating tournament");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="admin-tournament-new animate__animated animate__fadeIn">
            <div className="mb-4">
                <Link
                    to="/admin/tournaments"
                    className="text-decoration-none text-muted small hover-link"
                >
                    &larr; Back to Tournaments
                </Link>
            </div>

            <div className="d-flex justify-content-between align-items-center mb-5 mt-2">
                <div>
                    <h2 className="fw-bold m-0">Create Tournament</h2>
                    <p className="text-muted m-0">Setup a new competitive event</p>
                </div>
            </div>

            <div className="glass-card p-5">
                <Form noValidate validated={validated} onSubmit={handleSubmit}>
                    <Row className="g-4 mb-4">
                        <Col md={8}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Tournament Name *
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="text"
                                    placeholder="Enter tournament name..."
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    maxLength={30}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Name is required (max 30 characters).
                                </Form.Control.Feedback>
                            </Form.Group>

                            <Form.Group className="mt-4">
                                <Form.Label className="fw-bold text-light">
                                    Description *
                                </Form.Label>
                                <Form.Control
                                    required
                                    as="textarea"
                                    rows={4}
                                    placeholder="Describe the tournament rules, format, etc..."
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Description is required.
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>

                        <Col md={4}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Tournament Header Image
                                </Form.Label>
                                <div className="mb-3 text-center">
                                    {imagePreview ? (
                                        <img
                                            src={imagePreview}
                                            alt="Preview"
                                            className="img-fluid rounded-4 border border-secondary"
                                            style={{ maxHeight: 200, objectFit: "cover" }}
                                        />
                                    ) : (
                                        <div
                                            className="bg-dark rounded-4 d-flex align-items-center justify-content-center text-muted"
                                            style={{ height: 150, border: "2px dashed #495057" }}
                                        >
                                            No image selected
                                        </div>
                                    )}
                                </div>
                                <Form.Control
                                    type="file"
                                    accept="image/png, image/jpeg"
                                    onChange={handleImageChange}
                                    className="bg-dark text-light border-secondary small"
                                />
                                <Form.Text className="text-muted">
                                    Recommended: 16:9 ratio, Max 2MB.
                                </Form.Text>
                            </Form.Group>
                        </Col>
                    </Row>

                    <Row className="g-4 border-top border-secondary pt-4 mt-2">
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Available Slots *
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="number"
                                    min="2"
                                    max="128"
                                    value={slots}
                                    onChange={(e) => setSlots(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Must be between 2 and 128.
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Entry Price (Coins) *
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="number"
                                    min="0"
                                    value={price}
                                    onChange={(e) => setPrice(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Enter a valid price (0 for free).
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Registration Date *
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="datetime-local"
                                    value={registrationStarts}
                                    onChange={(e) => setRegistrationStarts(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Select registration start date.
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">
                                    Tournament Start Date *
                                </Form.Label>
                                <Form.Control
                                    required
                                    type="datetime-local"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
                                <Form.Control.Feedback type="invalid">
                                    Select tournament start date.
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                    </Row>

                    <div className="mt-5 text-end border-top border-secondary pt-4">
                        <Link
                            to="/admin/tournaments"
                            className="btn btn-secondary me-3 px-4 rounded-pill"
                        >
                            Cancel
                        </Link>
                        <Button
                            type="submit"
                            variant="primary"
                            className="btn-gradient-primary rounded-pill px-5"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? "Creating..." : "Create Tournament"}
                        </Button>
                    </div>
                </Form>
            </div>
        </div>
    );
}
