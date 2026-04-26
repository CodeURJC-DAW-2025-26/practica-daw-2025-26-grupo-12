import { useState, useMemo, useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import type { Route } from "./+types/users";
import { adminUserService, type UserPageResponse } from "../../services/admin-user-service";
import { Table, Button, Form, Modal, Pagination, Badge, Row, Col } from "react-bootstrap";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get("page") || "0");
    const query = url.searchParams.get("query") || "";

    try {
        const data = await adminUserService.getUsers(page, 10, query);
        return { data };
    } catch (error) {
        throw new Error("Failed to load users");
    }
}

export default function AdminUsers({ loaderData }: Route.ComponentProps) {
    const initialData = (loaderData as { data: UserPageResponse }).data;
    const [users, setUsers] = useState(initialData.content);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(searchParams.get("query") || "");

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showBlockModal, setShowBlockModal] = useState(false);
    const [targetUser, setTargetUser] = useState<{
        id: number;
        username: string;
        blocked: boolean;
    } | null>(null);

    useEffect(() => {
        setUsers(initialData.content);
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

    const onBlockToggle = async () => {
        if (!targetUser) return;
        try {
            const updatedUser = targetUser.blocked
                ? await adminUserService.unblockUser(targetUser.id)
                : await adminUserService.blockUser(targetUser.id);

            setUsers((prev) => prev.map((u) => (u.id === updatedUser.id ? updatedUser : u)));
            setShowBlockModal(false);
        } catch (error) {
            alert("Error updating user status");
        }
    };

    const onDelete = async () => {
        if (!targetUser) return;
        try {
            await adminUserService.deleteUser(targetUser.id);
            setUsers((prev) => prev.filter((u) => u.id !== targetUser.id));
            setShowDeleteModal(false);
        } catch (error) {
            alert("Error deleting user");
        }
    };

    return (
        <div className="admin-users-page animate__animated animate__fadeIn">
            <div className="d-flex justify-content-between align-items-center mb-5 mt-2">
                <div>
                    <h2 className="fw-bold m-0">User Moderation</h2>
                    <p className="text-muted m-0">Manage and monitor application users</p>
                </div>
            </div>

            <div className="glass-card p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3">
                        <Col md={8}>
                            <Form.Control
                                type="text"
                                placeholder="Search by username or email..."
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
                                Search Users
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </div>

            <div className="glass-card overflow-hidden">
                <Table hover responsive className="m-0 align-middle">
                    <thead className="bg-dark text-uppercase small letter-spacing-lg">
                        <tr>
                            <th className="border-0 ps-4 py-3">User</th>
                            <th className="border-0 py-3">Status</th>
                            <th className="border-0 py-3">Joined</th>
                            <th className="border-0 pe-4 py-3 text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="border-top-0">
                        {users.map((user) => (
                            <tr key={user.id} className="border-bottom border-secondary">
                                <td className="ps-4 py-3">
                                    <div className="d-flex align-items-center gap-3">
                                        <div
                                            className="avatar-placeholder rounded-circle bg-primary d-flex align-items-center justify-content-center text-white"
                                            style={{ width: 40, height: 40 }}
                                        >
                                            {user.username.charAt(0).toUpperCase()}
                                        </div>
                                        <div>
                                            <Link
                                                to={`/profile/${user.id}`}
                                                className="fw-bold text-white text-decoration-none hover-link"
                                            >
                                                {user.username}
                                            </Link>
                                            <div className="small text-muted">{user.email}</div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <Badge
                                        className={
                                            user.blocked
                                                ? "badge-soft-danger"
                                                : "badge-soft-success"
                                        }
                                        pill
                                    >
                                        {user.blocked ? "Blocked" : "Active"}
                                    </Badge>
                                </td>
                                <td className="text-muted small">
                                    {new Date(user.createdAt).toLocaleDateString(undefined, {
                                        year: "numeric",
                                        month: "short",
                                        day: "numeric",
                                    })}
                                </td>
                                <td className="pe-4 py-3 text-end">
                                    <Button
                                        variant={user.blocked ? "success" : "warning"}
                                        size="sm"
                                        className="me-2 rounded-pill px-3"
                                        onClick={() => {
                                            setTargetUser({
                                                id: user.id,
                                                username: user.username,
                                                blocked: user.blocked,
                                            });
                                            setShowBlockModal(true);
                                        }}
                                    >
                                        {user.blocked ? "Unblock" : "Block"}
                                    </Button>
                                    <Button
                                        variant="outline-danger"
                                        size="sm"
                                        className="rounded-pill px-3"
                                        onClick={() => {
                                            setTargetUser({
                                                id: user.id,
                                                username: user.username,
                                                blocked: user.blocked,
                                            });
                                            setShowDeleteModal(true);
                                        }}
                                    >
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {users.length === 0 && (
                            <tr>
                                <td colSpan={4} className="text-center py-5 text-muted">
                                    <div className="mb-2 fs-2">🔍</div>
                                    No users found matching your search.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </Table>
            </div>

            {initialData.totalPages > 1 && (
                <Pagination>
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

            <Modal show={showBlockModal} onHide={() => setShowBlockModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>{targetUser?.blocked ? "Unblock" : "Block"} User</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to {targetUser?.blocked ? "unblock" : "block"}{" "}
                    <strong>{targetUser?.username}</strong>?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowBlockModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant={targetUser?.blocked ? "success" : "warning"}
                        onClick={onBlockToggle}
                    >
                        Confirm
                    </Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Delete User</Modal.Title>
                </Modal.Header>
                <Modal.Body className="text-danger">
                    <strong>Warning!</strong> You are about to delete user{" "}
                    <strong>{targetUser?.username}</strong>. This action is usually irreversible in
                    this system (logical delete).
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={onDelete}>
                        Delete Account
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
