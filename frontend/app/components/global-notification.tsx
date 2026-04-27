import { useEffect, useState } from "react";
import { Toast, ToastContainer } from "react-bootstrap";
import { useAuthStore } from "~/stores/auth-store";

interface NotificationPayload {
    id: string;
    type: string;
    message: string;
    actionLabel?: string;
    actionUrl?: string;
    redirectUrl?: string;
    autoRedirect?: boolean;
}

export default function GlobalNotification() {
    const { isLoggedIn, user } = useAuthStore();
    const [notifications, setNotifications] = useState<NotificationPayload[]>([]);

    useEffect(() => {
        if (!isLoggedIn()) {
            console.log("Not logged in, skipping notification subscription");
            return;
        }

        console.log("Subscribing to notifications for user:", user?.username);
        const eventSource = new EventSource("/api/v1/notifications/subscribe", {
            withCredentials: true,
        });

        eventSource.onopen = () => {
            console.log("SSE connection opened");
        };

        eventSource.addEventListener("connected", (event) => {
            console.log("SSE connected event received:", event.data);
        });

        eventSource.addEventListener("notification", (event) => {
            console.log("Notification received:", event.data);
            try {
                const data = JSON.parse(event.data);
                const id = Date.now().toString() + Math.random().toString(36).substring(2, 9);
                const notification = { ...data, id };

                setNotifications((prev) => [...prev, notification]);

                // Auto-remove after 5s unless it has an action button
                if (!data.actionLabel) {
                    setTimeout(() => {
                        setNotifications((prev) => prev.filter((n) => n.id !== id));
                    }, 5000);
                }

                if (data.autoRedirect && data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                }
            } catch (error) {
                console.error("Failed to parse notification", error);
            }
        });

        eventSource.onerror = (error) => {
            console.error("SSE error detected:", error);
            eventSource.close();
        };

        return () => {
            console.log("Closing SSE connection");
            eventSource.close();
        };
    }, [isLoggedIn, user]);

    const removeNotification = (id: string) => {
        setNotifications((prev) => prev.filter((n) => n.id !== id));
    };

    if (notifications.length === 0) return null;

    return (
        <div
            className="notification-stack"
            style={{
                position: "fixed",
                top: "1.5rem",
                right: "1.5rem",
                zIndex: 9999,
            }}
        >
            {notifications.map((n) => (
                <div
                    key={n.id}
                    className={`notification-toast notification-toast--${
                        n.type === "error" ? "error" : n.actionLabel ? "action" : "info"
                    }`}
                >
                    <div className="notification-toast-inner">
                        <div className="notification-toast-icon">
                            {n.type === "error" ? (
                                <i className="bi bi-exclamation-triangle-fill"></i>
                            ) : n.actionLabel ? (
                                <i className="bi bi-bell-fill"></i>
                            ) : (
                                <i className="bi bi-info-circle-fill"></i>
                            )}
                        </div>
                        <div className="flex-grow-1">
                            <div className="notification-toast-title">
                                {n.type === "error" ? "System Error" : "Notification"}
                            </div>
                            <div className="notification-toast-message">{n.message}</div>
                            {(n.actionLabel || n.redirectUrl) && (
                                <div className="notification-toast-actions">
                                    {n.actionLabel && n.actionUrl && (
                                        <a
                                            href={n.actionUrl}
                                            className="btn btn-sm btn-primary notification-toast-button"
                                        >
                                            {n.actionLabel}
                                        </a>
                                    )}
                                    {!n.actionLabel && n.redirectUrl && (
                                        <a
                                            href={n.redirectUrl}
                                            className="notification-toast-chip text-decoration-none"
                                        >
                                            View Details <i className="bi bi-arrow-right ms-1"></i>
                                        </a>
                                    )}
                                </div>
                            )}
                        </div>
                        <button
                            type="button"
                            className="btn-close btn-close-white notification-toast-close"
                            onClick={() => removeNotification(n.id)}
                            aria-label="Close"
                        ></button>
                    </div>
                    <div
                        className="notification-toast-progress"
                        style={{ "--notification-delay": "5000ms" } as any}
                    ></div>
                </div>
            ))}
        </div>
    );
}
