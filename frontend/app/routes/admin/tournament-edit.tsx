import { useState } from "react";
import { Link, useNavigate } from "react-router";
import type { Route } from "./+types/tournament-edit";
import { Form, Button, Row, Col } from "react-bootstrap";
import { adminTournamentService } from "../../services/admin-tournament-service";
import type { TournamentDetail } from "~/types";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const { id } = params;
    try {
        const tournament = await adminTournamentService.getTournament(id);
        return { tournament };
    } catch (error) {
        throw new Error("Tournament not found");
    }
}

export default function AdminTournamentEdit({ loaderData }: Route.ComponentProps) {
    const { tournament } = loaderData as { tournament: TournamentDetail };
    const navigate = useNavigate();

    const formatDateTime = (isoString?: string) =>
        isoString ? new Date(isoString).toISOString().slice(0, 16) : "";

    const [name, setName] = useState(tournament.name);
    const [description, setDescription] = useState(tournament.description || "");
    const [slots, setSlots] = useState(tournament.slots.toString());
    const [status, setStatus] = useState(tournament.status);
    const [startDate, setStartDate] = useState(formatDateTime(tournament.startDate));
    const [registrationStarts, setRegistrationStarts] = useState(
        formatDateTime(tournament.startDate)
    );
    const [price, setPrice] = useState("0");

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(tournament.imageUrl || null);
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
            await adminTournamentService.updateTournament(tournament.id, {
                name,
                description,
                status,
                slots: Number(slots),
                price: Number(price),
                registrationStarts: new Date(registrationStarts).toISOString(),
                startDate: new Date(startDate).toISOString(),
                imageFile: imageFile || undefined,
            });
            navigate("/admin/tournaments");
        } catch (error) {
            alert("Error updating tournament");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="admin-tournament-edit animate__animated animate__fadeIn">
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
                    <h2 className="fw-bold m-0">Edit Tournament: {tournament.name}</h2>
                    <p className="text-muted m-0">Update tournament details and status</p>
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
                                    Leave empty to keep current image.
                                </Form.Text>
                            </Form.Group>
                        </Col>
                    </Row>

                    <Row className="g-4 border-top border-secondary pt-4 mt-2">
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">Status *</Form.Label>
                                <Form.Select
                                    value={status}
                                    onChange={(e) => setStatus(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                >
                                    <option value="Open">Open</option>
                                    <option value="Started">Started</option>
                                    <option value="Finished">Finished</option>
                                </Form.Select>
                            </Form.Group>
                        </Col>
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
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label className="fw-bold text-light">Start Date *</Form.Label>
                                <Form.Control
                                    required
                                    type="datetime-local"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    className="bg-dark text-light border-secondary"
                                />
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
                            {isSubmitting ? "Saving..." : "Save Changes"}
                        </Button>
                    </div>
                </Form>
            </div>
        </div>
    );
}
