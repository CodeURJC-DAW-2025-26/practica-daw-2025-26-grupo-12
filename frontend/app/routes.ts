import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route("/login", "routes/login.tsx"),
  route("/sign-up", "routes/sign-up.tsx"),
  route("/admin/tournaments/create", "routes/admin-tournament-create.tsx"),
  route("/admin/tournaments/detail/:id", "routes/admin-tournament-detail.tsx"),
  route("/admin/tournaments/edit/:id", "routes/admin-tournament-edit.tsx"),
  route("/admin/tournaments", "routes/admin-tournaments.tsx"),
  route("/tournaments/create", "routes/tournament-create.tsx"),
  route("/tournaments/detail/:id", "routes/tournament-detail.tsx"),
  route("/tournaments/join/:id", "routes/tournament-join.tsx"),
  route("/tournaments/my-tournaments", "routes/my-tournaments.tsx"),
  route("/tournaments", "routes/tournaments.tsx"),
] satisfies RouteConfig;
