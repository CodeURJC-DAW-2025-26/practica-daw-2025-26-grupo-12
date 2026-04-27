import { Links, Meta, Outlet, Scripts, ScrollRestoration, useLoaderData, useNavigation } from "react-router";

import "bootstrap/dist/css/bootstrap.min.css";
import type { Route } from "./+types/root";
import "./app.css";
import { Spinner } from "react-bootstrap";
import GlobalNotification from "./components/global-notification";
import { useAuthStore } from "./stores/auth-store";
import { useEffect } from "react";
import { getMe } from "./services/auth-service";

export const links: Route.LinksFunction = () => [
    {
        rel: "stylesheet",
        href: "https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap",
    },
    {
        rel: "stylesheet",
        href: "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css",
    },
];

export function Layout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
            <head>
                <meta charSet="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <Meta />
                <Links />
            </head>
            <body>
                {children}
                <ScrollRestoration />
                <Scripts />
            </body>
        </html>
    );
}

function GlobalSpinner() {
    const navigation = useNavigation();
    const isLoading = navigation.state === "loading";
    if (!isLoading) return null;
    return (
        <div
            style={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "100%",
                zIndex: 9999,
                display: "flex",
                justifyContent: "center",
                paddingTop: "0.5rem",
            }}
        >
            <Spinner
                animation="border"
                variant="primary"
                size="sm"
                role="status"
                aria-label="Loading"
            />
        </div>
    );
}
export async function clientLoader() {
    const user = await getMe();
    return { user };
}

export default function App() {
    const { user: loadedUser } = useLoaderData<typeof clientLoader>();

    const { setUser, setInitialized } = useAuthStore();

    useEffect(() => {
        setUser(loadedUser);
        setInitialized(true);
    }, [loadedUser, setUser, setInitialized]);

    return (
        <>
            <GlobalSpinner />
            <GlobalNotification />
            <Outlet />
        </>
    );
}


export function ErrorBoundary({ error }: { error: unknown }) {
    const message = error instanceof Error ? error.message : "An unexpected error occurred.";
    return (
        <div className="centered-layout min-vh-100">
            <div className="text-center">
                <h1 className="error-code">Error</h1>
                <p className="text-secondary">{message}</p>
                <a href="/" className="btn btn-primary px-4 py-2">
                    Go Home
                </a>
            </div>
        </div>
    );
}
