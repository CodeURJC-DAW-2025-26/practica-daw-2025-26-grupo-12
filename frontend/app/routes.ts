import { type RouteConfig, index, route, layout } from "@react-router/dev/routes";

export default [
    index("routes/home.tsx"),
    route("/login", "routes/login.tsx"),
    route("/signup", "routes/signup.tsx"),

    route("/tournaments", "routes/tournaments.tsx"),
    route("/tournaments/:id", "routes/tournament-detail.tsx"),

    route("/matches", "routes/matches.tsx"),
    route("/matches/:id/stats", "routes/match-stats.tsx"),

    route("/bots", "routes/bots.tsx"),
    route("/bots/:id", "routes/bot-detail.tsx"),

    layout("layouts/admin-layout.tsx", [
        route("/admin/users", "routes/admin/users.tsx"),
        route("/admin/bots", "routes/admin/bots.tsx"),
        route("/admin/bots/:id", "routes/admin/bot-detail.tsx"),
        route("/admin/tournaments", "routes/admin/tournaments.tsx"),
        route("/admin/tournaments/new", "routes/admin/tournament-new.tsx"),
        route("/admin/tournaments/:id", "routes/admin/tournament-edit.tsx"),
        route("/admin/notifications", "routes/admin/notifications.tsx"),
    ]),

    route("*", "routes/error-page.tsx"),
] satisfies RouteConfig;
