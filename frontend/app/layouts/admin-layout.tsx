import { Outlet } from "react-router";
import { RoleGuard } from "../components/auth/role-guard";
import AppNavbar from "~/components/header";
import Footer from "~/components/footer";

export default function AdminLayout() {
    return (
        <RoleGuard requireAdmin>
            <div className="d-flex flex-column min-vh-100">
                <AppNavbar />
                <Outlet />
                <Footer />
            </div>
        </RoleGuard>
    );
}
