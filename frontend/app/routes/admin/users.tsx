import { useState, useEffect } from "react";
import { useSearchParams, Link, useLoaderData } from "react-router";
import {
    Container,
    Row,
    Col,
    Card,
    Table,
    Form,
    Button,
    Badge,
    Modal,
    Spinner,
} from "react-bootstrap";
import type { Route } from "./+types/users";
import { adminUserService, type UserPageResponse } from "../../services/admin-user-service";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
    const url = new URL(request.url);
    const query = url.searchParams.get("query") || "";
    try {
        const data = await adminUserService.getUsers(0, 10, query);
        return { data, query };
    } catch (error) {
        throw new Error("Failed to load users");
    }
}

export default function AdminUsers() {
    const { data: initialData, query: initialQuery } = useLoaderData<typeof clientLoader>();
    const [users, setUsers] = useState(initialData.content);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(!initialData.last);
    const [searchParams, setSearchParams] = useSearchParams();
    const [query, setQuery] = useState(initialQuery);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showBlockModal, setShowBlockModal] = useState(false);
    const [targetUser, setTargetUser] = useState<{
        id: number;
        username: string;
        blocked: boolean;
    } | null>(null);

    useEffect(() => {
        setUsers(initialData.content);
        setPage(0);
        setHasMore(!initialData.last);
        setQuery(initialQuery);
    }, [initialData, initialQuery]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setSearchParams({ query });
    };

    const [loadingMore, setLoadingMore] = useState(false);

    const handleLoadMore = async () => {
        setLoadingMore(true);
        const nextPage = page + 1;
        try {
            const data = await adminUserService.getUsers(nextPage, 10, query);
            setUsers((prev) => [...prev, ...data.content]);
            setPage(nextPage);
            setHasMore(!data.last);
        } catch (error) {
            alert("Error loading more users");
        } finally {
            setLoadingMore(false);
        }
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
        <Container className="py-5">
            <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
                <div>
                    <p className="text-secondary mb-1 small text-uppercase">Administration</p>
                    <h1 className="h3 fw-bold mb-0">User Management</h1>
                </div>
            </div>

            <Card className="p-4 mb-4">
                <Form onSubmit={handleSearch}>
                    <Row className="g-3 align-items-center">
                        <Col lg={7}>
                            <Form.Label className="text-uppercase small mb-1">
                                Search users
                            </Form.Label>
                            <Form.Control
                                type="search"
                                placeholder="Search by username or email"
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                            />
                        </Col>
                        <Col lg={2}>
                            <Form.Label className="text-uppercase small mb-1">Status</Form.Label>
                            <Form.Select>
                                <option value="all">All</option>
                                <option value="active">Active</option>
                                <option value="blocked">Blocked</option>
                            </Form.Select>
                        </Col>
                        <Col lg={3} className="d-grid d-lg-flex justify-content-lg-end gap-2">
                            <Button
                                type="submit"
                                variant="primary"
                                size="sm"
                                className="px-3 mt-lg-4"
                            >
                                Search
                            </Button>
                            <Button
                                variant="outline-muted"
                                size="sm"
                                className="px-3 mt-lg-4"
                                onClick={() => {
                                    setQuery("");
                                    setSearchParams({});
                                }}
                            >
                                Clear
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card>

            <Card className="p-4 mb-4">
                <div className="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-3">
                    <h2 className="h5 fw-bold mb-0">Matching Users</h2>
                    <Badge bg="secondary">
                        Showing {users.length} of {initialData.totalElements}
                    </Badge>
                </div>
                <div className="table-responsive">
                    <Table className="mb-0 align-middle text-nowrap">
                        <thead>
                            <tr className="text-secondary small text-uppercase border-secondary">
                                <th scope="col" className="ps-3">
                                    Photo
                                </th>
                                <th scope="col">Username</th>
                                <th scope="col">Email</th>
                                <th scope="col">Provider</th>
                                <th scope="col">Status</th>
                                <th scope="col" className="text-end pe-3">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((user) => (
                                <tr key={user.id} className="border-secondary">
                                    <td className="ps-3">
                                        <div
                                            className="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
                                            style={{ width: 40, height: 40 }}
                                        >
                                            {user.imageUrl ? (
                                                <img
                                                    src={user.imageUrl}
                                                    style={{ width: 40, height: 40 }}
                                                />
                                            ) : (
                                                user.username.charAt(0).toUpperCase()
                                            )}
                                        </div>
                                    </td>
                                    <td className="fw-semibold text-white">{user.username}</td>
                                    <td className="text-secondary">{user.email}</td>
                                    <td className="text-secondary">SYSTEM</td>
                                    <td>
                                        {user.blocked ? (
                                            <Badge bg="danger">Blocked</Badge>
                                        ) : (
                                            <Badge bg="success">Active</Badge>
                                        )}
                                    </td>
                                    <td className="text-end pe-3">
                                        <Button
                                            as={Link as any}
                                            to={`/profile/${user.id}`}
                                            variant="outline-muted"
                                            size="sm"
                                            className="me-2"
                                        >
                                            View
                                        </Button>
                                        <Button
                                            variant={user.blocked ? "success" : "warning"}
                                            size="sm"
                                            className="me-2"
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
                                    <td colSpan={6} className="text-center text-secondary py-4">
                                        No users found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </div>
                {hasMore && (
                    <div className="d-flex justify-content-center mt-3">
                        <Button
                            variant="outline-secondary"
                            size="sm"
                            onClick={handleLoadMore}
                            disabled={loadingMore}
                        >
                            {loadingMore ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Loading…
                                </>
                            ) : (
                                "Show more"
                            )}
                        </Button>
                    </div>
                )}
            </Card>

            <Modal show={showBlockModal} onHide={() => setShowBlockModal(false)} centered>
                <Modal.Header closeButton className="border-secondary">
                    <Modal.Title className="h5">Confirm action</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="mb-0 text-secondary">
                        Are you sure you want to {targetUser?.blocked ? "unblock" : "block"}{" "}
                        {targetUser?.username}?
                    </p>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="outline-muted" onClick={() => setShowBlockModal(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant={targetUser?.blocked ? "success" : "danger"}
                        onClick={onBlockToggle}
                    >
                        {targetUser?.blocked ? "Unblock" : "Block"}
                    </Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton className="border-secondary">
                    <Modal.Title className="h5">Delete User</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="mb-0 text-danger">
                        You are about to delete user <strong>{targetUser?.username}</strong>. This
                        action is irreversible.
                    </p>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="outline-muted" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={onDelete}>
                        Delete Account
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}
