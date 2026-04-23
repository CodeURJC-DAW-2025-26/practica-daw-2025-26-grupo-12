import Header from "~/components/header";
import Footer from "~/components/footer";
import GuestHomeContent from "~/components/guestHomeContent";
import { useEffect } from "react";
import { useSessionState } from "~/hooks/use-session-state";

export default function Home() {
  const session = useSessionState();

  useEffect(() => {
    if (session.resolved && session.logged) {
      window.location.assign("/tournaments");
    }
  }, [session.logged, session.resolved]);

  return (
    <>
      <Header admin={session.admin} logged={session.logged} />
      {!session.resolved && (
        <main className="container py-5 text-center">
          <div className="card p-5">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Checking your session...</p>
          </div>
        </main>
      )}
      {session.resolved && !session.logged && <GuestHomeContent />}
      <Footer />
    </>
  );
}
