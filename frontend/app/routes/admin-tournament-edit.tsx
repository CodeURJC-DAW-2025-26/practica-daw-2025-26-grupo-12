import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import AdminTournamentForm, {
  type AdminTournamentFormValues,
} from "~/components/adminTournamentForm";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  extractRegistrationOpenDate,
  extractTournamentPrize,
  fetchTournament,
  getTournamentStatusMeta,
  stripTournamentMetadata,
  updateTournament,
  type TournamentDetail,
  type TournamentMutationData,
} from "~/services/tournament-service";

function toFormValues(tournament: TournamentDetail): AdminTournamentFormValues {
  const registrationStarts =
    extractRegistrationOpenDate(tournament.description) ?? tournament.startDate ?? "";

  return {
    name: tournament.name,
    slots: String(tournament.slots),
    registrationStarts,
    startDate: tournament.startDate ?? "",
    description: stripTournamentMetadata(tournament.description),
    price: extractTournamentPrize(tournament.description),
    status: getTournamentStatusMeta(tournament.status).label,
  };
}

export default function AdminTournamentEditRoute() {
  const { id } = useParams();
  const session = useSessionState();
  const navigate = useNavigate();
  const [tournament, setTournament] = useState<TournamentDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
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
        const nextTournament = await fetchTournament(id);

        if (!active) {
          return;
        }

        setTournament(nextTournament);
      } catch (error) {
        if (!active) {
          return;
        }

        setTournament(null);
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

  async function submitTournament(data: TournamentMutationData) {
    if (!id) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      await updateTournament(id, data);
      navigate(`/admin/tournaments/detail/${id}?updated=1`);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to update this tournament right now.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
          <div>
            <p className="text-secondary mb-1 small text-uppercase">Tournament Admin</p>
            <h1 className="h3 fw-bold mb-0">
              {tournament ? `Edit: ${tournament.name}` : "Edit Tournament"}
            </h1>
          </div>
          <div className="d-flex flex-wrap gap-2">
            <Link to="/admin/tournaments" className="btn btn-outline-muted btn-sm">
              Back to Admin
            </Link>
            {id && (
              <Link to={`/admin/tournaments/detail/${id}`} className="btn btn-primary btn-sm">
                View Details
              </Link>
            )}
          </div>
        </div>

        {!session.resolved || isLoading ? (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Loading tournament...</p>
          </div>
        ) : null}

        {session.resolved && !session.admin && (
          <div className="card glass-card p-4 text-center">
            <h2 className="h5 fw-bold mb-2">Admin access required</h2>
            <p className="text-secondary mb-3">
              Log in with an administrator account to edit tournaments.
            </p>
            <Link to="/login" className="btn btn-gradient-primary btn-sm">
              Log In
            </Link>
          </div>
        )}

        {session.resolved && session.admin && !isLoading && tournament && (
          <div className="row g-4">
            <div className="col-lg-8">
              <AdminTournamentForm
                mode="edit"
                initialValues={toFormValues(tournament)}
                errorMessage={errorMessage}
                isSubmitting={isSubmitting}
                onSubmit={submitTournament}
              />
            </div>
            <div className="col-lg-4">
              <div className="card glass-card p-4 text-center h-100">
                <h2 className="h5 fw-bold mb-3">Tournament Image</h2>
                <div className="d-flex justify-content-center">
                  <TournamentThumbnail
                    tournamentId={tournament.id}
                    name={tournament.name}
                    imageUrl={tournament.imageUrl}
                    size={150}
                  />
                </div>
                <p className="text-secondary small mt-3 mb-0">
                  Uploading a file in the form will replace this image.
                </p>
              </div>
            </div>
          </div>
        )}

        {session.resolved && session.admin && !isLoading && !tournament && errorMessage && (
          <div className="alert alert-danger" role="alert">
            {errorMessage}
          </div>
        )}
      </main>

      <Footer />
    </>
  );
}
