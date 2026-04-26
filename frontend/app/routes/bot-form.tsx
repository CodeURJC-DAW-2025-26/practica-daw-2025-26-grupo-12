import React, { useState, useRef } from "react";
import { Container, Row, Col, Card, Form, Button, Modal, ListGroup, Image } from "react-bootstrap";
import { Link, useNavigate } from "react-router";
import { createBot } from "~/services/bot-service";

export default function CreateBot() {
    const navigate = useNavigate();
    const [showImageModal, setShowImageModal] = useState(false);

    const [formData, setFormData] = useState({
        name: "",
        isPublic: "true",
        tags: "",
        description: "",
        code: "",
    });

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [tempFile, setTempFile] = useState<File | null>(null);
    const [tempPreview, setTempPreview] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
    ) => {
        const { id, value } = e.target;
        setFormData((prev) => ({ ...prev, [id]: value }));
    };

    const handleModalFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setTempFile(file);
            const reader = new FileReader();
            reader.onload = (event) => setTempPreview(event.target?.result as string);
            reader.readAsDataURL(file);
        }
    };

    const confirmImage = () => {
        if (tempFile && tempPreview) {
            setImageFile(tempFile);
            setPreviewUrl(tempPreview);
            setShowImageModal(false);
        }
    };

    const resetForm = () => {
        setFormData({ name: "", isPublic: "true", tags: "", description: "", code: "" });
        setImageFile(null);
        setPreviewUrl(null);
        setTempFile(null);
        setTempPreview(null);
        if (fileInputRef.current) fileInputRef.current.value = "";
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            const tagsArray = formData.tags
                .split(",")
                .map((t) => t.trim())
                .filter((t) => t !== "");
            await createBot({
                name: formData.name,
                description: formData.description,
                code: formData.code,
                tags: tagsArray,
                isPublic: formData.isPublic === "true",
                imageFile: imageFile || undefined,
            });
            navigate("/bots/user-bots");
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-5 mt-5">
            <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                <div>
                    <p className="text-secondary mb-1 small text-uppercase">My Bots</p>
                    <h1 className="h3 fw-bold mb-0">Create Bot</h1>
                </div>
                <div className="d-flex flex-wrap gap-2">
                    <Link to="/bots/user-bots" className="btn btn-outline-secondary btn-sm">
                        Back to My Bots
                    </Link>
                </div>
            </div>

            <Row className="g-4">
                <Col lg={8}>
                    <Card className="p-4 shadow-sm">
                        <h2 className="h5 fw-bold mb-3">Bot Details</h2>
                        <Form onSubmit={handleSubmit}>
                            <Row className="g-3">
                                <Col md={7}>
                                    <Form.Group controlId="name">
                                        <Form.Label className="text-uppercase small">
                                            Bot Name
                                        </Form.Label>
                                        <Form.Control
                                            size="lg"
                                            type="text"
                                            placeholder="New challenger"
                                            required
                                            maxLength={60}
                                            value={formData.name}
                                            onChange={handleInputChange}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={5}>
                                    <Form.Group controlId="isPublic">
                                        <Form.Label className="text-uppercase small">
                                            Visibility
                                        </Form.Label>
                                        <Form.Select
                                            size="lg"
                                            value={formData.isPublic}
                                            onChange={handleInputChange}
                                        >
                                            <option value="true">Public</option>
                                            <option value="false">Private</option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                                <Col xs={12}>
                                    <Form.Group controlId="tags">
                                        <Form.Label className="text-uppercase small">
                                            Tags
                                        </Form.Label>
                                        <Form.Control
                                            size="lg"
                                            type="text"
                                            placeholder="Add multiple tags separated by commas"
                                            maxLength={300}
                                            value={formData.tags}
                                            onChange={handleInputChange}
                                        />
                                        <Form.Text className="text-secondary">
                                            Example: aggressive, random, counter
                                        </Form.Text>
                                    </Form.Group>
                                </Col>
                                <Col xs={12}>
                                    <Form.Group controlId="description">
                                        <Form.Label className="text-uppercase small">
                                            Description
                                        </Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            size="lg"
                                            placeholder="Describe your bot strategy."
                                            maxLength={500}
                                            value={formData.description}
                                            onChange={handleInputChange}
                                        />
                                    </Form.Group>
                                </Col>

                                <Col xs={12}>
                                    <Form.Group controlId="code">
                                        <Form.Label className="text-uppercase small">
                                            Bot Code (Logic)
                                        </Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={8}
                                            size="lg"
                                            className="font-monospace"
                                            placeholder="def move(gameState):..."
                                            value={formData.code}
                                            onChange={handleInputChange}
                                        />
                                    </Form.Group>
                                </Col>

                                <Col xs={12}>
                                    <Form.Label className="text-uppercase small">
                                        Bot Image
                                    </Form.Label>
                                    <div className="d-flex flex-wrap gap-3">
                                        {previewUrl && (
                                            <Image
                                                src={previewUrl}
                                                roundedCircle
                                                className="border border-secondary"
                                                style={{
                                                    width: "60px",
                                                    height: "60px",
                                                    objectFit: "cover",
                                                }}
                                            />
                                        )}
                                        <div className="d-flex flex-column justify-content-center">
                                            <div className="d-flex gap-2">
                                                <Button
                                                    variant="outline-secondary"
                                                    onClick={() => setShowImageModal(true)}
                                                >
                                                    {imageFile ? "Change Image" : "Upload Image"}
                                                </Button>
                                                {imageFile && (
                                                    <span className="text-info small align-self-center">
                                                        Selected: {imageFile.name}
                                                    </span>
                                                )}
                                            </div>
                                            <span className="text-secondary small mt-1">
                                                PNG, JPG up to 10MB.
                                            </span>
                                        </div>
                                    </div>
                                </Col>

                                <Col xs={12} className="d-flex flex-wrap gap-2 mt-4">
                                    <Button type="submit" variant="primary" disabled={loading}>
                                        {loading ? "Creating..." : "Create and Edit"}
                                    </Button>
                                    <Button variant="outline-secondary" onClick={resetForm}>
                                        Clear
                                    </Button>
                                </Col>
                            </Row>
                        </Form>
                    </Card>
                </Col>

                <Col lg={4}>
                    <Card className="p-4 mb-4 shadow-sm">
                        <h2 className="h5 fw-bold mb-3">Guidelines</h2>
                        <ListGroup variant="flush">
                            <ListGroup.Item className="bg-transparent px-0 border-secondary">
                                Keep code deterministic and safe.
                            </ListGroup.Item>
                            <ListGroup.Item className="bg-transparent px-0 border-secondary">
                                Avoid external network calls.
                            </ListGroup.Item>
                            <ListGroup.Item className="bg-transparent px-0 border-secondary">
                                Test locally before uploading.
                            </ListGroup.Item>
                        </ListGroup>
                    </Card>
                </Col>
            </Row>

            <Modal show={showImageModal} onHide={() => setShowImageModal(false)} centered>
                <Modal.Header closeButton className="border-secondary bg-dark text-white">
                    <Modal.Title className="h5">Upload Bot Image</Modal.Title>
                </Modal.Header>
                <Modal.Body className="text-center bg-dark text-white">
                    {tempPreview && (
                        <div className="mb-3">
                            <Image src={tempPreview} thumbnail style={{ maxHeight: "200px" }} />
                        </div>
                    )}
                    <Form.Control
                        type="file"
                        accept="image/*"
                        onChange={handleModalFileChange}
                        ref={fileInputRef}
                    />
                </Modal.Body>
                <Modal.Footer className="border-secondary bg-dark">
                    <Button variant="outline-secondary" onClick={() => setShowImageModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="primary" onClick={confirmImage} disabled={!tempFile}>
                        Confirm
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}
