import { useState } from "react";
import { Link, useNavigate } from "react-router";
import AdminTournamentForm, {
  type AdminTournamentFormValues,
} from "~/components/adminTournamentForm";
import Footer from "~/components/footer";
import Header from "~/components/header";
import { useSessionState } from "~/hooks/use-session-state";
import { createTournament, type TournamentMutationData } from "~/services/tournament-service";

const EMPTY_FORM_VALUES: AdminTournamentFormValues = {
  name: "",
  slots: "",
  registrationStarts: "",
  startDate: "",
  description: "",
  price: "",
  status: "Upcoming",
};

export default function AdminTournamentCreateRoute() {
  const session = useSessionState();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function submitTournament(data: TournamentMutationData) {
    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const tournament = await createTournament(data);
      navigate(`/admin/tournaments/detail/${tournament.id}?created=1`);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to create this tournament right now.",
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
            <h1 className="h3 fw-bold mb-0">Create Tournament</h1>
          </div>
          <div className="d-flex flex-wrap gap-2">
            <Link to="/admin/tournaments" className="btn btn-outline-muted btn-sm">
              Back to Admin
            </Link>
          </div>
        </div>

        {!session.resolved && (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Checking your session...</p>
          </div>
        )}

        {session.resolved && !session.admin && (
          <div className="card glass-card p-4 text-center">
            <h2 className="h5 fw-bold mb-2">Admin access required</h2>
            <p className="text-secondary mb-3">
              Log in with an administrator account to create tournaments.
            </p>
            <Link to="/login" className="btn btn-gradient-primary btn-sm">
              Log In
            </Link>
          </div>
        )}

        {session.resolved && session.admin && (
          <AdminTournamentForm
            mode="create"
            initialValues={EMPTY_FORM_VALUES}
            errorMessage={errorMessage}
            isSubmitting={isSubmitting}
            onSubmit={submitTournament}
          />
        )}
      </main>

      <Footer />
    </>
  );
}
