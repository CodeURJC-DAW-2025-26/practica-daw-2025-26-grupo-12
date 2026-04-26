import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { Button, Row, Col, Card, Modal, Form } from "react-bootstrap";
import type { Route } from "./+types/bot-edit";
import type { BotDetail } from "~/types";
import { getBotById, updateBot } from "~/services/bot-service";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
    const bot = await getBotById(Number(params.id));
    return { bot };
}

export default function BotEdit({ loaderData }: Route.ComponentProps) {
    const { bot } = loaderData as { bot: BotDetail };

    const [name, setName] = useState(bot.name);
    const [description, setDescription] = useState(bot.description ?? "");
    const [code, setCode] = useState(bot.code ?? "");
    const [tags, setTags] = useState((bot.tags ?? []).join(","));
    const [isPublic, setIsPublic] = useState(bot.public);

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(bot.imageUrl ?? null);

    const [showModal, setShowModal] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        if (!imageFile) return;
        const url = URL.createObjectURL(imageFile);
        setPreview(url);
        return () => URL.revokeObjectURL(url);
    }, [imageFile]);

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        await updateBot(bot.id, {
            name,
            description,
            code,
            tags: tags.split(",").map(t => t.trim()).filter(Boolean),
            isPublic,
            imageFile: imageFile ?? undefined
        });

        navigate(`/bots/${bot.id}`);
    };

    return (
        <div className="container py-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <p className="text-secondary small text-uppercase mb-1">My Bots</p>
                    <h1 className="h3 fw-bold mb-0">Edit Bot</h1>
                </div>

                <Link to={`/bots/${bot.id}`} className="btn btn-outline-secondary btn-sm">
                    Back to Bot
                </Link>
            </div>

            <Row className="g-4">
                <Col lg={8}>
                    <Card className="p-4">
                        <h5 className="fw-bold mb-3">Bot Settings</h5>

                        <Form onSubmit={onSubmit}>
                            <Row className="g-3">
                                <Col md={7}>
                                    <Form.Label>Bot Name</Form.Label>
                                    <Form.Control
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                        size="lg"
                                    />
                                </Col>

                                <Col md={5}>
                                    <Form.Label>Visibility</Form.Label>
                                    <Form.Select
                                        value={String(isPublic)}
                                        onChange={(e) => setIsPublic(e.target.value === "true")}
                                        size="lg"
                                    >
                                        <option value="true">Public</option>
                                        <option value="false">Private</option>
                                    </Form.Select>
                                </Col>

                                <Col xs={12}>
                                    <Form.Label>Tags</Form.Label>
                                    <Form.Control
                                        value={tags}
                                        onChange={(e) => setTags(e.target.value)}
                                        placeholder="tag1, tag2, tag3"
                                        size="lg"
                                    />
                                </Col>

                                <Col xs={12}>
                                    <Form.Label>Description</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={4}
                                        value={description}
                                        onChange={(e) => setDescription(e.target.value)}
                                        size="lg"
                                    />
                                </Col>

                                <Col xs={12}>
                                    <Form.Label>Bot Code (Python)</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={12}
                                        value={code}
                                        onChange={(e) => setCode(e.target.value)}
                                        className="font-monospace text-info bg-dark"
                                    />
                                </Col>

                                <Col xs={12} className="d-flex gap-2 mt-3">
                                    <Button type="submit" variant="primary">
                                        Save Changes
                                    </Button>
                                    <Link to="/bots/user-bots" className="btn btn-outline-secondary">
                                        Discard
                                    </Link>
                                </Col>
                            </Row>
                        </Form>
                    </Card>
                </Col>

                <Col lg={4}>
                    <Card className="p-4 mb-4 text-center">
                        <h5 className="fw-bold text-start mb-3">Bot Image</h5>

                        <div
                            className="mx-auto mb-3 rounded-circle overflow-hidden border border-primary"
                            style={{ width: 120, height: 120 }}
                        >
                            {preview ? (
                                <img
                                    src={preview}
                                    alt="bot"
                                    className="w-100 h-100"
                                    style={{ objectFit: "cover" }}
                                />
                            ) : (
                                <div className="w-100 h-100 bg-primary d-flex align-items-center justify-content-center text-white fs-2 fw-bold">
                                    {bot.name[0]}
                                </div>
                            )}
                        </div>

                        <Button variant="outline-secondary" onClick={() => setShowModal(true)}>
                            Change Avatar
                        </Button>
                    </Card>

                    <Card className="p-4 bg-primary bg-opacity-10 border-primary">
                        <h5 className="fw-bold">Import Script</h5>
                        <p className="text-secondary small">
                            Upload a .py file to replace code.
                        </p>

                        <Form.Control
                            type="file"
                            accept=".py"
                            onChange={(e) => {
                                const file = e.target.files?.[0];
                                if (!file) return;
                                const reader = new FileReader();
                                reader.onload = (ev) => {
                                    setCode(ev.target?.result as string);
                                };
                                reader.readAsText(file);
                            }}
                        />
                    </Card>
                </Col>
            </Row>

            <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Change Bot Image</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Control
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                            const file = e.target.files?.[0];
                            if (file) setImageFile(file);
                        }}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <Button onClick={() => setShowModal(false)}>Close</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}