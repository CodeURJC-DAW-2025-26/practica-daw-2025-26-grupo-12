import { useEffect, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  TOURNAMENT_PAGE_SIZE,
  fetchTournamentPage,
  type TournamentPageResponse,
} from "~/services/tournament-service";

function sanitizePage(rawPage: string | null): number {
  const parsedPage = Number(rawPage ?? "0");
  if (!Number.isFinite(parsedPage) || parsedPage < 0) {
    return 0;
  }

  return Math.floor(parsedPage);
}

export default function Tournaments() {
  const session = useSessionState();
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchValue, setSearchValue] = useState(searchParams.get("q") ?? "");
  const [tournamentPage, setTournamentPage] = useState<TournamentPageResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const query = searchParams.get("q")?.trim() ?? "";
  const page = sanitizePage(searchParams.get("page"));

  useEffect(() => {
    setSearchValue(query);
  }, [query]);

  useEffect(() => {
    let active = true;

    async function loadTournaments() {
      setIsLoading(true);
      setErrorMessage("");

      try {
        const nextTournamentPage = await fetchTournamentPage({
          query,
          page,
          size: TOURNAMENT_PAGE_SIZE,
        });

        if (!active) {
          return;
        }

        setTournamentPage(nextTournamentPage);
      } catch (error) {
        if (!active) {
          return;
        }

        setTournamentPage(null);
        setErrorMessage(
          error instanceof Error ? error.message : "Unable to load tournaments right now.",
        );
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void loadTournaments();

    return () => {
      active = false;
    };
  }, [page, query]);

  function updatePage(nextPage: number) {
    const nextParams = new URLSearchParams(searchParams);
    if (nextPage <= 0) {
      nextParams.delete("page");
    } else {
      nextParams.set("page", String(nextPage));
    }

    setSearchParams(nextParams);
  }

  function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextParams = new URLSearchParams(searchParams);
    const trimmedValue = searchValue.trim();

    if (trimmedValue) {
      nextParams.set("q", trimmedValue);
    } else {
      nextParams.delete("q");
    }

    nextParams.delete("page");
    setSearchParams(nextParams);
  }

  const tournaments = tournamentPage?.content ?? [];
  const fromItem = tournaments.length === 0 ? 0 : page * TOURNAMENT_PAGE_SIZE + 1;
  const toItem = tournaments.length === 0 ? 0 : fromItem + tournaments.length - 1;
  const totalPages = tournamentPage?.totalPages ?? 0;
  const totalElements = tournamentPage?.totalElements ?? 0;
  const hasPreviousPage = page > 0;
  const hasNextPage = totalPages > 0 && page < totalPages - 1;

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
          <div>
            <p className="text-secondary mb-1 small text-uppercase">Season 2026</p>
            <h1 className="h3 fw-bold mb-0">Tournaments</h1>
          </div>
        </div>

        <div className="card p-4 mb-4">
          <form onSubmit={onSearch} className="row g-3 align-items-center">
            <div className="col-lg-9">
              <label htmlFor="tournament-search" className="form-label text-uppercase small mb-1">
                Search tournaments
              </label>
              <input
                id="tournament-search"
                type="search"
                className="form-control"
                placeholder="Search by tournament name..."
                autoComplete="off"
                value={searchValue}
                onChange={(event) => setSearchValue(event.target.value)}
              />
            </div>
            <div className="col-lg-3 d-grid d-lg-flex justify-content-lg-end gap-2">
              <button type="submit" className="btn btn-primary btn-sm px-3 mt-lg-4">
                Search
              </button>
              <Link to="/tournaments" className="btn btn-outline-muted btn-sm px-3 mt-lg-4">
                Clear
              </Link>
            </div>
          </form>
        </div>

        {errorMessage && (
          <div className="alert alert-danger mb-4" role="alert">
            {errorMessage}
          </div>
        )}

        <div className="card p-4">
          <div className="d-flex justify-content-between align-items-center gap-3 mb-3 flex-wrap">
            <h2 className="h5 fw-bold mb-0">Tournament List</h2>
            <span className="text-secondary small">
              Showing {fromItem}-{toItem} of {totalElements}
            </span>
          </div>

          {isLoading && (
            <div className="py-5 text-center">
              <div className="spinner-border text-primary mb-3" role="status" />
              <p className="text-secondary mb-0">Loading tournaments...</p>
            </div>
          )}

          {!isLoading && tournaments.length > 0 && (
            <>
              <div className="table-responsive">
                <table className="table mb-0 align-middle text-nowrap">
                  <thead>
                    <tr className="text-secondary small text-uppercase">
                      <th scope="col" className="ps-3" style={{ width: "60px" }}>
                        Image
                      </th>
                      <th scope="col">Tournament</th>
                      <th scope="col">Slots</th>
                      <th scope="col">Status</th>
                      <th scope="col" className="text-end pe-3">
                        Action
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {tournaments.map((tournament) => {
                      return (
                        <tr key={tournament.id}>
                          <td className="ps-3">
                            <TournamentThumbnail
                              tournamentId={tournament.id}
                              name={tournament.name}
                              size={48}
                            />
                          </td>
                          <td>
                            <div className="fw-medium">{tournament.name}</div>
                            <div className="text-secondary small">
                              {tournament.registered} registered participants
                            </div>
                          </td>
                          <td className="text-secondary">
                            {tournament.registered}/{tournament.slots}
                          </td>
                          <td>
                            <TournamentStatusBadge status={tournament.status} />
                          </td>
                          <td className="text-end pe-3">
                            <a
                              href={`/tournaments/detail/${tournament.id}`}
                              className="btn btn-outline-muted btn-sm"
                            >
                              View
                            </a>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {totalPages > 1 && (
                <div className="d-flex justify-content-between align-items-center gap-2 mt-4 flex-wrap">
                  <span className="text-secondary small">
                    Page {page + 1} of {totalPages}
                  </span>
                  <div className="d-flex gap-2">
                    <button
                      type="button"
                      className="btn btn-outline-muted btn-sm"
                      onClick={() => updatePage(page - 1)}
                      disabled={!hasPreviousPage}
                    >
                      Previous
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline-muted btn-sm"
                      onClick={() => updatePage(page + 1)}
                      disabled={!hasNextPage}
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </>
          )}

          {!isLoading && !errorMessage && tournaments.length === 0 && (
            <div className="empty-state-panel text-center py-5 px-3">
              <h2 className="h5 fw-bold mb-2">No tournaments found</h2>
              <p className="text-secondary mb-3">
                Try a different search term or clear the current filters.
              </p>
              <Link to="/tournaments" className="btn btn-outline-muted btn-sm">
                Reset search
              </Link>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </>
  );
}
