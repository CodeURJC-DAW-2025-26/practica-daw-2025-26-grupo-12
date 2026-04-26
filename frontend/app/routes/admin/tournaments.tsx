import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import type { Route } from "./+types/tournaments";
import { adminTournamentService } from "../../services/admin-tournament-service";
import type { Page, TournamentSummary } from "~/types";
import { Table, Button, Form, Pagination, Badge, Row, Col } from "react-bootstrap";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get("page") || "0");
    const query = url.searchParams.get("query") || "";

    try {
        const data = await adminTournamentService.getTournaments(page, 10, query);
        return { data };
    } catch (error) {
        throw new Error("Failed to load tournaments");
    }
}

export default function AdminTournaments({ loaderData }: Route.ComponentProps) {
    const initialData = (loaderData as { data: Page<TournamentSummary> }).data;
    const [tournaments, setTournaments] = useState(initialData.content);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(searchParams.get("query") || "");

    useEffect(() => {
        setTournaments(initialData.content);
    }, [initialData.content]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setSearchParams((prev) => {
            prev.set("query", query);
            prev.set("page", "0");
            return prev;
        });
    };

    const handlePageChange = (page: number) => {
        setSearchParams((prev) => {
            prev.set("page", page.toString());
            return prev;
        });
    };

    const getStatusBadge = (status: string) => {
        switch (status.toLowerCase()) {
            case "open":
                return "badge-soft-success";
            case "started":
                return "badge-soft-primary";
            case "finished":
                return "badge-soft-secondary";
            default:
                return "badge-soft-warning";
        }
    };

    return (
        <div className="admin-tournaments-page animate__animated animate__fadeIn">
            <div className="d-flex justify-content-between align-items-center mb-5 mt-2">
                <div>
                    <h2 className="fw-bold m-0">Tournaments Management</h2>
                    <p className="text-muted m-0">Create and oversee competitive events</p>
                </div>
                <Link
                    to="/admin/tournaments/new"
                    className="btn btn-gradient-primary rounded-pill px-4 py-2"
                >
                    + New Tournament
                </Link>
            </div>

            <div className="glass-card p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3">
                        <Col md={8}>
                            <Form.Control
                                type="text"
                                placeholder="Search by tournament name..."
                                className="form-control-lg"
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                            />
                        </Col>
                        <Col md={4} className="d-grid">
                            <Button
                                variant="primary"
                                type="submit"
                                className="btn-gradient-primary btn-lg"
                            >
                                Search Tournaments
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </div>

            <div className="glass-card overflow-hidden">
                <Table hover responsive className="m-0 align-middle">
                    <thead className="bg-dark text-uppercase small letter-spacing-lg">
                        <tr>
                            <th className="border-0 ps-4 py-3">Tournament</th>
                            <th className="border-0 py-3">Status</th>
                            <th className="border-0 py-3">Slots</th>
                            <th className="border-0 py-3">Starts</th>
                            <th className="border-0 pe-4 py-3 text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="border-top-0">
                        {tournaments.map((t) => (
                            <tr key={t.id} className="border-bottom border-secondary">
                                <td className="ps-4 py-3">
                                    <div className="d-flex align-items-center gap-3">
                                        {t.imageUrl ? (
                                            <img
                                                src={`/api/v1/images/tournaments/${t.id}`}
                                                alt={t.name}
                                                className="rounded-3 border border-secondary"
                                                style={{
                                                    width: 50,
                                                    height: 40,
                                                    objectFit: "cover",
                                                }}
                                            />
                                        ) : (
                                            <div
                                                className="bg-secondary rounded-3 d-flex align-items-center justify-content-center text-white small"
                                                style={{ width: 50, height: 40 }}
                                            >
                                                🏆
                                            </div>
                                        )}
                                        <div>
                                            <div className="fw-bold text-white">{t.name}</div>
                                            <div className="small text-muted">ID: {t.id}</div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <Badge className={getStatusBadge(t.status)} pill>
                                        {t.status}
                                    </Badge>
                                </td>
                                <td>
                                    <span className="fw-bold text-primary">{t.participants}</span>
                                    <span className="text-muted small"> / {t.slots}</span>
                                </td>
                                <td className="text-muted small">
                                    {new Date(t.startDate).toLocaleDateString()}
                                </td>
                                <td className="pe-4 py-3 text-end">
                                    <Link
                                        to={`/admin/tournaments/${t.id}`}
                                        className="btn btn-outline-info btn-sm me-2 rounded-pill px-3"
                                    >
                                        Edit
                                    </Link>
                                    <Button
                                        variant="outline-danger"
                                        size="sm"
                                        className="rounded-pill px-3"
                                        onClick={() => {
                                            // TODO delete tournament logic
                                        }}
                                    >
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            </div>

            {initialData.totalPages > 1 && (
                <Pagination className="mt-4">
                    <Pagination.First
                        onClick={() => handlePageChange(0)}
                        disabled={initialData.number === 0}
                    />
                    <Pagination.Prev
                        onClick={() => handlePageChange(initialData.number - 1)}
                        disabled={initialData.number === 0}
                    />
                    {[...Array(initialData.totalPages)].map((_, i) => (
                        <Pagination.Item
                            key={i}
                            active={i === initialData.number}
                            onClick={() => handlePageChange(i)}
                        >
                            {i + 1}
                        </Pagination.Item>
                    ))}
                    <Pagination.Next
                        onClick={() => handlePageChange(initialData.number + 1)}
                        disabled={initialData.number === initialData.totalPages - 1}
                    />
                    <Pagination.Last
                        onClick={() => handlePageChange(initialData.totalPages - 1)}
                        disabled={initialData.number === initialData.totalPages - 1}
                    />
                </Pagination>
            )}
        </div>
    );
}
