import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import { useSessionState } from "~/hooks/use-session-state";
import { fetchBotPage, type BotListItem } from "~/services/bot-service";
import {
  extractRegistrationOpenDate,
  extractTournamentFormat,
  fetchTournament,
  fetchTournamentSummaryById,
  formatTournamentDate,
  getTournamentAvailabilityMessage,
  isTournamentRegistrationOpen,
  joinTournament,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

export default function TournamentJoinRoute() {
  const { id } = useParams();
  const navigate = useNavigate();
  const session = useSessionState();
  const [tournament, setTournament] = useState<TournamentDetail | null>(null);
  const [summary, setSummary] = useState<TournamentListItem | null>(null);
  const [bots, setBots] = useState<BotListItem[]>([]);
  const [selectedBotId, setSelectedBotId] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (session.resolved && session.admin && id) {
      navigate(`/tournaments/detail/${id}`, { replace: true });
    }
  }, [id, navigate, session.admin, session.resolved]);

  useEffect(() => {
    let active = true;

    async function loadJoinData() {
      if (!id) {
        setErrorMessage("The tournament could not be loaded.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setErrorMessage("");
      setSuccessMessage("");

      try {
        const [nextTournament, nextSummary, botPage] = await Promise.all([
          fetchTournament(id),
          fetchTournamentSummaryById(id),
          session.logged && !session.admin
            ? fetchBotPage({ page: 0, size: 20 })
            : Promise.resolve({ content: [] as BotListItem[] }),
        ]);

        if (!active) {
          return;
        }

        const sortedBots = [...botPage.content].sort((firstBot, secondBot) => {
          return secondBot.elo - firstBot.elo;
        });

        setTournament(nextTournament);
        setSummary(nextSummary);
        setBots(sortedBots);
        setSelectedBotId(sortedBots[0]?.id ? String(sortedBots[0].id) : "");
      } catch (error) {
        if (!active) {
          return;
        }

        setTournament(null);
        setSummary(null);
        setBots([]);
        setSelectedBotId("");
        setErrorMessage(
          error instanceof Error ? error.message : "Unable to load tournament registration.",
        );
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    if (session.resolved) {
      void loadJoinData();
    }

    return () => {
      active = false;
    };
  }, [id, session.admin, session.logged, session.resolved]);

  const registeredParticipants = summary?.registered ?? 0;
  const registrationOpen = useMemo(() => {
    return tournament !== null && isTournamentRegistrationOpen(tournament, registeredParticipants);
  }, [registeredParticipants, tournament]);
  const availabilityMessage =
    tournament === null
      ? null
      : getTournamentAvailabilityMessage(
          tournament,
          registeredParticipants,
          session.logged,
          session.admin,
        );
  const canSubmit =
    Boolean(session.logged && !session.admin && registrationOpen && selectedBotId) && bots.length > 0;

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!id || !selectedBotId || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const result = await joinTournament(id, selectedBotId);

      if (result.status === "JOINED") {
        setSuccessMessage(result.message || "Bot registered successfully.");
        navigate(`/tournaments/detail/${id}?joined=1`);
        return;
      }

      setErrorMessage(result.message || "Unable to join this tournament right now.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to join this tournament right now.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        {isLoading && (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Loading registration...</p>
          </div>
        )}

        {!isLoading && tournament && (
          <>
            <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
              <div>
                <p className="text-secondary mb-1 small text-uppercase">Tournament Registration</p>
                <h1 className="h3 fw-bold mb-0">{tournament.name}</h1>
              </div>
              <div className="d-flex flex-wrap gap-2">
                <Link
                  to={`/tournaments/detail/${tournament.id}`}
                  className="btn btn-outline-muted btn-sm"
                >
                  Back to Details
                </Link>
              </div>
            </div>

            {errorMessage && (
              <div className="alert alert-danger mb-4" role="alert">
                {errorMessage}
              </div>
            )}
            {successMessage && (
              <div className="alert alert-success mb-4" role="alert">
                {successMessage}
              </div>
            )}

            <div className="row g-4">
              <div className="col-lg-7">
                <div className="card p-4">
                  <h2 className="h5 fw-bold mb-3">Select Your Bot</h2>

                  {availabilityMessage && (
                    <div className={`alert ${availabilityMessage.className} text-start`} role="alert">
                      {availabilityMessage.message}
                    </div>
                  )}

                  {canSubmit ? (
                    <form onSubmit={onSubmit}>
                      <div className="mb-4">
                        <label htmlFor="botId" className="form-label text-uppercase small">
                          Bot
                        </label>
                        <select
                          className="form-select form-control-lg"
                          id="botId"
                          name="botId"
                          required
                          value={selectedBotId}
                          onChange={(event) => setSelectedBotId(event.target.value)}
                        >
                          {bots.map((bot) => (
                            <option value={bot.id} key={bot.id}>
                              {bot.name} - ELO {bot.elo}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="d-flex flex-wrap gap-2">
                        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                          {isSubmitting ? "Registering..." : "Confirm Registration"}
                        </button>
                        <Link
                          to={`/tournaments/detail/${tournament.id}`}
                          className="btn btn-outline-muted"
                        >
                          Cancel
                        </Link>
                      </div>
                    </form>
                  ) : (
                    <div className="d-flex flex-wrap gap-2">
                      <Link
                        to={`/tournaments/detail/${tournament.id}`}
                        className="btn btn-outline-muted"
                      >
                        Back to Details
                      </Link>
                      {session.logged && !session.admin && bots.length === 0 && (
                        <a href="/bots/create" className="btn btn-primary">
                          Create Bot
                        </a>
                      )}
                      {!session.logged && (
                        <Link to="/login" className="btn btn-primary">
                          Log In
                        </Link>
                      )}
                    </div>
                  )}
                </div>
              </div>

              <div className="col-lg-5">
                <div className="card p-4 mb-4">
                  <h2 className="h5 fw-bold mb-3">Tournament Info</h2>
                  <ul className="list-group list-group-flush">
                    <li className="list-group-item d-flex justify-content-between align-items-center">
                      Format
                      <span className="badge bg-secondary rounded-pill">
                        {extractTournamentFormat(tournament.description)}
                      </span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center">
                      Slots
                      <span className="badge bg-secondary rounded-pill">
                        {registeredParticipants}/{tournament.slots}
                      </span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center">
                      Registration Opens
                      <span className="badge bg-secondary rounded-pill">
                        {formatTournamentDate(extractRegistrationOpenDate(tournament.description))}
                      </span>
                    </li>
                    <li className="list-group-item d-flex justify-content-between align-items-center">
                      Start Date
                      <span className="badge bg-secondary rounded-pill">
                        {formatTournamentDate(tournament.startDate)}
                      </span>
                    </li>
                  </ul>
                </div>

                <div className="card p-4">
                  <h2 className="h5 fw-bold mb-3">Description</h2>
                  <p className="text-secondary mb-0">
                    {tournament.description || "No description available."}
                  </p>
                </div>
              </div>
            </div>
          </>
        )}

        {!isLoading && !tournament && errorMessage && (
          <div className="alert alert-danger" role="alert">
            {errorMessage}
          </div>
        )}
      </main>

      <Footer />
    </>
  );
}
