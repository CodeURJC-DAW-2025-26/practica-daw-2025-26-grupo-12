import { Link } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import { useSessionState } from "~/hooks/use-session-state";

export default function TournamentCreateRoute() {
  const session = useSessionState();

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="row g-4">
          <div className="col-lg-8 mx-auto">
            <div className="card p-4 text-center">
              <h1 className="h4 fw-bold mb-2">Admin Only</h1>
              <p className="text-secondary mb-3">
                Tournament creation is now available in the admin panel.
              </p>
              <div className="d-flex flex-wrap justify-content-center gap-2">
                {!session.logged && (
                  <Link to="/login" className="btn btn-outline-muted btn-sm">
                    Log In
                  </Link>
                )}
                <a href="/admin/tournaments" className="btn btn-primary btn-sm">
                  Go to Admin Panel
                </a>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </>
  );
}
