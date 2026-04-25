import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  fetchTournament,
  fetchTournamentSummaryById,
  extractTournamentFormat,
  formatTournamentDate,
  getTournamentAvailabilityMessage,
  isTournamentRegistrationOpen,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

export default function TournamentDetailRoute() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const session = useSessionState();
  const [tournament, setTournament] = useState<TournamentDetail | null>(null);
  const [summary, setSummary] = useState<TournamentListItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function loadTournament() {
      if (!id) {
        setErrorMessage("The tournament could not be loaded.");
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
  }, [id]);

  const registeredParticipants = summary?.registered ?? 0;
  const registrationOpen =
    tournament !== null && isTournamentRegistrationOpen(tournament, registeredParticipants);
  const availabilityMessage =
    tournament === null
      ? null
      : getTournamentAvailabilityMessage(
          tournament,
          registeredParticipants,
          session.logged,
          session.admin,
        );
  const joined = searchParams.get("joined") === "1";
  const joinError = searchParams.get("joinError");
  const showJoinButton = Boolean(
    tournament && session.logged && !session.admin && registrationOpen,
  );

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        {joined && (
          <div className="alert alert-success mb-4" role="alert">
            Bot registered successfully.
          </div>
        )}
        {joinError && (
          <div className="alert alert-danger mb-4" role="alert">
            {joinError}
          </div>
        )}
        {errorMessage && (
          <div className="alert alert-danger mb-4" role="alert">
            {errorMessage}
          </div>
        )}

        {isLoading && (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Loading tournament...</p>
          </div>
        )}

        {!isLoading && tournament && (
          <>
            <div className="tournament-hero mb-5">
              <div className="d-flex flex-column flex-md-row align-items-center gap-4">
                <div className="position-relative">
                  <TournamentThumbnail
                    tournamentId={tournament.id}
                    name={tournament.name}
                    imageUrl={tournament.imageUrl}
                    size={140}
                    className="shadow-lg"
                  />
                  {registrationOpen && (
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-success border border-dark">
                      OPEN
                    </span>
                  )}
                </div>

                <div className="text-center text-md-start flex-grow-1">
                  <p className="text-primary mb-1 small text-uppercase fw-bold">
                    {session.admin
                      ? "Admin Control Panel"
                      : registrationOpen
                        ? "Registration Phase"
                        : "Tournament Details"}
                  </p>
                  <h1 className="display-5 fw-bold mb-2">{tournament.name}</h1>
                  <div className="d-flex flex-wrap justify-content-center justify-content-md-start gap-3 mt-3">
                    {session.admin ? (
                      <>
                        <Link
                          to={`/admin/tournaments/edit/${tournament.id}`}
                          className="btn btn-primary px-4"
                        >
                          Edit Settings
                        </Link>
                        <Link to="/admin/tournaments" className="btn btn-outline-muted px-4">
                          Admin Dashboard
                        </Link>
                      </>
                    ) : (
                      <>
                        {showJoinButton && (
                          <Link
                            to={`/tournaments/join/${tournament.id}`}
                            className="btn btn-primary px-5 fw-bold"
                          >
                            JOIN NOW
                          </Link>
                        )}
                        {!session.logged && registrationOpen && (
                          <Link to="/login" className="btn btn-primary px-5 fw-bold">
                            LOG IN TO JOIN
                          </Link>
                        )}
                        <Link to="/tournaments" className="btn btn-outline-muted px-4">
                          All Tournaments
                        </Link>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {!session.admin && availabilityMessage && (
              <div className={`alert ${availabilityMessage.className} mb-4`} role="alert">
                {availabilityMessage.message}
              </div>
            )}

            <div className="row g-4">
              <div className="col-lg-8">
                <div className="card p-4 border-secondary h-100 shadow-sm">
                  <h2 className="h5 fw-bold mb-4 border-bottom border-secondary pb-2 text-uppercase small">
                    About this Tournament
                  </h2>
                  <div className="tournament-description-copy text-secondary mb-4">
                    {tournament.description || "No description available."}
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
                        <span className="h4 fw-bold">{registeredParticipants}</span>
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

                  <div className="card p-4 border-secondary shadow-sm">
                    <h2 className="h6 fw-bold mb-3 text-uppercase">Tournament Rules</h2>
                    <div className="small tournament-rule-list">
                      <div className="d-flex gap-2 mb-2">
                        <span className="text-primary fw-bold">+</span>
                        <span>Best of 20 rounds per match.</span>
                      </div>
                      <div className="d-flex gap-2 mb-2">
                        <span className="text-primary fw-bold">+</span>
                        <span>Bots 24h before start.</span>
                      </div>
                      <div className="d-flex gap-2 mb-2">
                        <span className="text-primary fw-bold">+</span>
                        <span>No external API calls.</span>
                      </div>
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
