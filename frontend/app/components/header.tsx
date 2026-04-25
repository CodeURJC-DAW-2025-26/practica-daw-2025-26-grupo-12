import { Link } from "react-router";
import { logoutUser } from "~/services/auth-service";

interface HeaderProps {
    logged: boolean;
    admin: boolean;
}

export default function Header({ logged, admin }: HeaderProps) {
    admin = logged && admin;

    async function onLogout() {
        await logoutUser();
        window.location.assign("/");
    }

    return (
        <>
            <nav className="navbar navbar-expand-lg navbar-dark sticky-top">
                <div className="container">
                    <Link className="navbar-brand" to="/">✂️ Scissors, Please</Link>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav me-auto">
                            <li className="nav-item"><Link className="nav-link" to="/">Home</Link></li>
                            <li className="nav-item"><a className="nav-link" href="/matches/list">Best Matches</a></li>
                            {admin && <>
                                <li className="nav-item"><a className="nav-link" href="/admin/tournaments">Tournaments</a></li>
                                <li className="nav-item"><a className="nav-link" href="/admin/users">Users</a></li>
                                <li className="nav-item"><a className="nav-link" href="/admin/bots">Bots</a></li>
                            </>}
                            {!admin && <li className="nav-item"><Link className="nav-link" to="/tournaments">Tournaments</Link></li>}
                        </ul>
                        <div className="d-flex gap-2 align-items-center">
                            {logged && admin && <span className="badge bg-primary me-2">Admin</span>}

                            {logged && !admin && <a href="/user/profile" className="btn btn-outline-muted btn-sm px-3">Profile</a>}

                            {logged && <><button type="button" onClick={onLogout} className="btn btn-primary btn-sm px-3">Log Out</button></>}

                            {!logged && <><Link to="/login" className="btn btn-outline-muted btn-sm px-3">Log In</Link>
                                <Link to="/sign-up" className="btn btn-primary btn-sm px-3">Sign Up</Link> </>}
                        </div>
                    </div>
                </div>
            </nav>
        </>
    );
}
