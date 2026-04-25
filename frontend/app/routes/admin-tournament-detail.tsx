import { useEffect, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  deleteTournament,
  extractRegistrationOpenDate,
  extractTournamentFormat,
  extractTournamentPrize,
  fetchTournament,
  fetchTournamentSummaryById,
  formatTournamentDate,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

export default function AdminTournamentDetailRoute() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const session = useSessionState();
  const [tournament, setTournament] = useState<TournamentDetail | null>(null);
  const [summary, setSummary] = useState<TournamentListItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function loadTournament() {
      if (!id || !session.resolved) {
        return;
      }

      if (!session.admin) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setErrorMessage("");

      try {
        const [nextTournament, nextSummary] = await Promise.all([
          fetchTournament(id),
          fetchTournamentSummaryById(id),
        ]);

        if (!active) {
          return;
        }

        setTournament(nextTournament);
        setSummary(nextSummary);
      } catch (error) {
        if (!active) {
          return;
        }

        setTournament(null);
        setSummary(null);
        setErrorMessage(
          error instanceof Error ? error.message : "The tournament could not be loaded.",
        );
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void loadTournament();

    return () => {
      active = false;
    };
  }, [id, session.admin, session.resolved]);

  async function removeTournament() {
    if (!id || !tournament) {
      return;
    }

    const confirmed = window.confirm(`Delete ${tournament.name}?`);
    if (!confirmed) {
      return;
    }

    setIsDeleting(true);
    setErrorMessage("");

    try {
      await deleteTournament(id);
      navigate("/admin/tournaments?deleted=1");
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to delete this tournament right now.",
      );
    } finally {
      setIsDeleting(false);
    }
  }

  const created = searchParams.get("created") === "1";
  const updated = searchParams.get("updated") === "1";
  const participants = summary?.registered ?? 0;

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        {created && (
          <div className="alert alert-success px-3 py-2 small mb-4" role="alert">
            Tournament created successfully.
          </div>
        )}
        {updated && (
          <div className="alert alert-success px-3 py-2 small mb-4" role="alert">
            Tournament updated successfully.
          </div>
        )}
        {errorMessage && (
          <div className="alert alert-danger px-3 py-2 small mb-4" role="alert">
            {errorMessage}
          </div>
        )}

        {!session.resolved || isLoading ? (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Loading tournament...</p>
          </div>
        ) : null}

        {session.resolved && !session.admin && (
          <div className="card glass-card p-4 text-center">
            <h1 className="h5 fw-bold mb-2">Admin access required</h1>
            <p className="text-secondary mb-3">
              Log in with an administrator account to inspect this tournament.
            </p>
            <Link to="/login" className="btn btn-gradient-primary btn-sm">
              Log In
            </Link>
          </div>
        )}

        {session.resolved && session.admin && !isLoading && tournament && (
          <>
            <div className="tournament-hero mb-5">
              <div className="d-flex flex-column flex-md-row align-items-center gap-4">
                <TournamentThumbnail
                  tournamentId={tournament.id}
                  name={tournament.name}
                  imageUrl={tournament.imageUrl}
                  size={140}
                  className="shadow-lg"
                />

                <div className="text-center text-md-start flex-grow-1">
                  <p className="text-primary mb-1 small text-uppercase fw-bold">
                    Admin Control Panel
                  </p>
                  <h1 className="display-5 fw-bold mb-2">{tournament.name}</h1>
                  <div className="d-flex flex-wrap justify-content-center justify-content-md-start gap-2 mt-3">
                    <Link
                      to={`/admin/tournaments/edit/${tournament.id}`}
                      className="btn btn-gradient-primary px-4"
                    >
                      Edit Settings
                    </Link>
                    <Link to="/admin/tournaments" className="btn btn-outline-muted px-4">
                      Admin Dashboard
                    </Link>
                    <button
                      type="button"
                      className="btn btn-outline-danger px-4"
                      disabled={isDeleting}
                      onClick={() => void removeTournament()}
                    >
                      {isDeleting ? "Deleting..." : "Delete"}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="row g-4">
              <div className="col-lg-8">
                <div className="card glass-card p-4 h-100">
                  <h2 className="h5 fw-bold mb-4 border-bottom border-secondary pb-2 text-uppercase small">
                    Tournament Setup
                  </h2>
                  <p className="tournament-description-copy text-secondary mb-4">
                    {tournament.description || "No description available."}
                  </p>
                  <div className="row g-3">
                    <div className="col-md-6">
                      <div className="admin-metric-panel">
                        <span className="text-secondary small text-uppercase">Registration Opens</span>
                        <strong>{formatTournamentDate(extractRegistrationOpenDate(tournament.description))}</strong>
                      </div>
                    </div>
                    <div className="col-md-6">
                      <div className="admin-metric-panel">
                        <span className="text-secondary small text-uppercase">Prize</span>
                        <strong>{extractTournamentPrize(tournament.description) || "No prize"}</strong>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="col-lg-4">
                <div className="d-flex flex-column gap-4">
                  <div className="card p-4 border-primary tournament-info-card">
                    <div className="mb-4">
                      <label className="text-secondary small text-uppercase d-block mb-1">
                        Status
                      </label>
                      <div className="d-grid">
                        <TournamentStatusBadge status={tournament.status} />
                      </div>
                    </div>

                    <div className="row g-3">
                      <div className="col-6 text-center border-end border-secondary">
                        <label className="text-secondary small text-uppercase d-block mb-1">
                          Players
                        </label>
                        <span className="h4 fw-bold">{participants}</span>
                        <span className="text-secondary small">/ {tournament.slots}</span>
                      </div>
                      <div className="col-6 text-center">
                        <label className="text-secondary small text-uppercase d-block mb-1">
                          Format
                        </label>
                        <span className="fw-bold">
                          {extractTournamentFormat(tournament.description)}
                        </span>
                      </div>
                    </div>

                    <div className="mt-4 pt-4 border-top border-secondary">
                      <label className="text-secondary small text-uppercase d-block">
                        Start Date
                      </label>
                      <span className="fw-bold">{formatTournamentDate(tournament.startDate)}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
      </main>

      <Footer />
    </>
  );
}
