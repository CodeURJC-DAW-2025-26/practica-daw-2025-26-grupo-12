import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  TOURNAMENT_PAGE_SIZE,
  deleteTournament,
  fetchTournament,
  fetchTournamentPage,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

interface EnrichedTournament extends TournamentListItem {
  detail: TournamentDetail | null;
}

export default function AdminTournamentsRoute() {
  const session = useSessionState();
  const [searchParams, setSearchParams] = useSearchParams();
  const urlQuery = searchParams.get("q")?.trim() ?? "";
  const processed = searchParams.get("processed");
  const deleted = searchParams.get("deleted") === "1";
  const [searchValue, setSearchValue] = useState(urlQuery);
  const [tournaments, setTournaments] = useState<EnrichedTournament[]>([]);
  const [nextPage, setNextPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setSearchValue(urlQuery);
  }, [urlQuery]);

  useEffect(() => {
    if (processed === null) {
      return;
    }

    setSuccessMessage(
      Number(processed) > 0
        ? `Processed ${processed} tournament(s).`
        : "No upcoming tournaments to process.",
    );
  }, [processed]);

  useEffect(() => {
    if (deleted) {
      setSuccessMessage("Tournament deleted successfully.");
    }
  }, [deleted]);

  useEffect(() => {
    let active = true;

    async function enrichTournamentItems(items: TournamentListItem[]): Promise<EnrichedTournament[]> {
      const details = await Promise.all(
        items.map((item) => fetchTournament(item.id).catch(() => null)),
      );

      return items.map((item, index) => ({
        ...item,
        detail: details[index],
      }));
    }

    async function loadInitialPage() {
      if (!session.resolved) {
        return;
      }

      if (!session.admin) {
        setTournaments([]);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setErrorMessage("");

      try {
        const tournamentPage = await fetchTournamentPage({
          query: urlQuery,
          page: 0,
          size: TOURNAMENT_PAGE_SIZE,
        });
        const enrichedTournaments = await enrichTournamentItems(tournamentPage.content);

        if (!active) {
          return;
        }

        setTournaments(enrichedTournaments);
        setNextPage(tournamentPage.pageNumber + 1);
        setTotalElements(tournamentPage.totalElements);
        setHasMore(tournamentPage.pageNumber + 1 < tournamentPage.totalPages);
      } catch (error) {
        if (!active) {
          return;
        }

        setTournaments([]);
        setNextPage(0);
        setTotalElements(0);
        setHasMore(false);
        setErrorMessage(
          error instanceof Error ? error.message : "Unable to load tournaments right now.",
        );
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void loadInitialPage();

    return () => {
      active = false;
    };
  }, [session.admin, session.resolved, urlQuery]);

  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  function updateQuery(nextQuery: string) {
    const trimmedQuery = nextQuery.trim();
    const nextParams = new URLSearchParams(searchParams);

    if (trimmedQuery) {
      nextParams.set("q", trimmedQuery);
    } else {
      nextParams.delete("q");
    }

    nextParams.delete("processed");
    nextParams.delete("deleted");
    setSearchParams(nextParams, { replace: true });
  }

  function queueLiveSearch(nextValue: string) {
    setSearchValue(nextValue);

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      updateQuery(nextValue);
    }, 250);
  }

  function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    updateQuery(searchValue);
  }

  async function loadMoreTournaments() {
    if (isLoadingMore || !hasMore) {
      return;
    }

    setIsLoadingMore(true);
    setErrorMessage("");

    try {
      const tournamentPage = await fetchTournamentPage({
        query: urlQuery,
        page: nextPage,
        size: TOURNAMENT_PAGE_SIZE,
      });
      const details = await Promise.all(
        tournamentPage.content.map((item) => fetchTournament(item.id).catch(() => null)),
      );
      const enrichedTournaments = tournamentPage.content.map((item, index) => ({
        ...item,
        detail: details[index],
      }));

      setTournaments((currentTournaments) => [...currentTournaments, ...enrichedTournaments]);
      setNextPage(tournamentPage.pageNumber + 1);
      setTotalElements(tournamentPage.totalElements);
      setHasMore(tournamentPage.pageNumber + 1 < tournamentPage.totalPages);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to load more tournaments right now.",
      );
    } finally {
      setIsLoadingMore(false);
    }
  }

  async function confirmDelete(tournament: EnrichedTournament) {
    const confirmed = window.confirm(`Delete ${tournament.name}?`);
    if (!confirmed) {
      return;
    }

    setDeletingId(tournament.id);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await deleteTournament(tournament.id);
      setTournaments((currentTournaments) =>
        currentTournaments.filter((item) => item.id !== tournament.id),
      );
      setTotalElements((currentTotal) => Math.max(currentTotal - 1, 0));
      setSuccessMessage("Tournament deleted successfully.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Unable to delete this tournament right now.",
      );
    } finally {
      setDeletingId(null);
    }
  }

  const fromItem = tournaments.length === 0 ? 0 : 1;
  const toItem = tournaments.length;

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
          <div>
            <p className="text-secondary mb-1 small text-uppercase">Administration</p>
            <h1 className="h3 fw-bold mb-0">Tournament Admin</h1>
          </div>
          <div className="d-flex flex-wrap gap-2">
            <Link to="/admin/tournaments/create" className="btn btn-gradient-primary btn-sm">
              New Tournament
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
              Log in with an administrator account to manage tournaments.
            </p>
            <Link to="/login" className="btn btn-gradient-primary btn-sm">
              Log In
            </Link>
          </div>
        )}

        {session.resolved && session.admin && (
          <>
            {successMessage && (
              <div className="alert alert-success px-3 py-2 small mb-4" role="alert">
                {successMessage}
              </div>
            )}
            {errorMessage && (
              <div className="alert alert-danger px-3 py-2 small mb-4" role="alert">
                {errorMessage}
              </div>
            )}

            <div className="card glass-card p-4 mb-4">
              <form onSubmit={onSearch} className="row g-3 align-items-center">
                <div className="col-lg-9">
                  <label
                    htmlFor="admin-tournament-search"
                    className="form-label text-uppercase small mb-1"
                  >
                    Search tournaments
                  </label>
                  <input
                    id="admin-tournament-search"
                    type="search"
                    className="form-control"
                    placeholder="Search by tournament name..."
                    value={searchValue}
                    onChange={(event) => queueLiveSearch(event.target.value)}
                    autoComplete="off"
                  />
                </div>
                <div className="col-lg-3 d-grid d-lg-flex justify-content-lg-end gap-2">
                  <button type="submit" className="btn btn-primary btn-sm px-3 mt-lg-4">
                    Search
                  </button>
                  <Link
                    to="/admin/tournaments"
                    className="btn btn-outline-muted btn-sm px-3 mt-lg-4"
                  >
                    Clear
                  </Link>
                </div>
              </form>
            </div>

            <div className="card p-4">
              <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                <h2 className="h5 fw-bold mb-0">Managed Tournaments</h2>
                <span className="badge bg-secondary">
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
                    <table className="table tournament-table mb-0 align-middle text-nowrap">
                      <thead>
                        <tr className="small text-uppercase">
                          <th scope="col" className="ps-3" style={{ width: "60px" }}>
                            Image
                          </th>
                          <th scope="col">Tournament</th>
                          <th scope="col">Slots</th>
                          <th scope="col">Status</th>
                          <th scope="col" className="text-end pe-3">
                            Actions
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {tournaments.map((tournament) => (
                          <tr key={tournament.id}>
                            <td className="ps-3">
                              <TournamentThumbnail
                                tournamentId={tournament.id}
                                name={tournament.name}
                                imageUrl={tournament.detail?.imageUrl}
                                size={48}
                              />
                            </td>
                            <td className="tournament-title-cell">
                              <div className="tournament-list-name">{tournament.name}</div>
                              <div className="tournament-list-summary">
                                {tournament.detail?.description || "No description available."}
                              </div>
                            </td>
                            <td>
                              <span className="tournament-slot-badge">
                                {tournament.registered}/{tournament.slots}
                              </span>
                            </td>
                            <td>
                              <TournamentStatusBadge status={tournament.status} />
                            </td>
                            <td className="text-end pe-3">
                              <div className="d-flex flex-wrap gap-2 justify-content-end">
                                <Link
                                  to={`/admin/tournaments/detail/${tournament.id}`}
                                  className="btn btn-outline-muted btn-sm"
                                >
                                  View
                                </Link>
                                <Link
                                  to={`/admin/tournaments/edit/${tournament.id}`}
                                  className="btn btn-outline-muted btn-sm"
                                >
                                  Edit
                                </Link>
                                <button
                                  type="button"
                                  className="btn btn-outline-danger btn-sm"
                                  disabled={deletingId === tournament.id}
                                  onClick={() => void confirmDelete(tournament)}
                                >
                                  {deletingId === tournament.id ? "Deleting..." : "Delete"}
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="d-flex justify-content-center mt-3">
                    <button
                      type="button"
                      className="btn btn-outline-muted btn-sm"
                      onClick={loadMoreTournaments}
                      hidden={!hasMore}
                      disabled={isLoadingMore}
                    >
                      {isLoadingMore ? "Loading..." : "Show more"}
                    </button>
                  </div>
                </>
              )}

              {!isLoading && tournaments.length === 0 && (
                <div className="empty-state-panel text-center py-5 px-3">
                  <h2 className="h5 fw-bold mb-2">No tournaments available</h2>
                  <p className="text-secondary mb-3">
                    Create the first tournament or clear the current search.
                  </p>
                  <Link to="/admin/tournaments/create" className="btn btn-gradient-primary btn-sm">
                    New Tournament
                  </Link>
                </div>
              )}
            </div>
          </>
        )}
      </main>

      <Footer />
    </>
  );
}
