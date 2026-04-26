import { type RouteConfig, index, route } from "@react-router/dev/routes";

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

    route("*", "routes/error-page.tsx"),
] satisfies RouteConfig;
