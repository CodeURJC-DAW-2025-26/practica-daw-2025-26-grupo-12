import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route("/login", "routes/login.tsx"),
  route("/sign-up", "routes/sign-up.tsx"),
  route("/tournaments/create", "routes/tournament-create.tsx"),
  route("/tournaments/detail/:id", "routes/tournament-detail.tsx"),
  route("/tournaments/join/:id", "routes/tournament-join.tsx"),
  route("/tournaments/my-tournaments", "routes/my-tournaments.tsx"),
  route("/tournaments", "routes/tournaments.tsx"),
] satisfies RouteConfig;
