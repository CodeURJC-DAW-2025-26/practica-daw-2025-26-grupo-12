import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  TOURNAMENT_PAGE_SIZE,
  extractTournamentFormat,
  fetchMyTournamentPage,
  fetchTournament,
  formatTournamentDate,
  getTournamentActionMeta,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

interface EnrichedTournament extends TournamentListItem {
  detail: TournamentDetail | null;
}

export default function MyTournamentsRoute() {
  const session = useSessionState();
  const [searchParams, setSearchParams] = useSearchParams();
  const urlQuery = searchParams.get("q")?.trim() ?? "";
  const [searchValue, setSearchValue] = useState(urlQuery);
  const [tournaments, setTournaments] = useState<EnrichedTournament[]>([]);
  const [nextPage, setNextPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setSearchValue(urlQuery);
  }, [urlQuery]);

  useEffect(() => {
    let active = true;

    async function enrichTournamentItems(items: TournamentListItem[]): Promise<EnrichedTournament[]> {
      const details = await Promise.all(
        items.map((item) => {
          return fetchTournament(item.id).catch(() => null);
        }),
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

      if (!session.logged) {
        setTournaments([]);
        setNextPage(0);
        setTotalElements(0);
        setHasMore(false);
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setErrorMessage("");

      try {
        const tournamentPage = await fetchMyTournamentPage({
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
          error instanceof Error ? error.message : "Unable to load your tournaments right now.",
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
  }, [session.logged, session.resolved, urlQuery]);

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
      const tournamentPage = await fetchMyTournamentPage({
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

  const fromItem = tournaments.length === 0 ? 0 : 1;
  const toItem = tournaments.length;

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
          <div>
            <p className="text-secondary mb-1 small text-uppercase">Season 2026</p>
            <h1 className="h3 fw-bold mb-0">My Tournaments</h1>
          </div>
          <Link to="/tournaments" className="btn btn-outline-muted btn-sm">
            All Tournaments
          </Link>
        </div>

        <div className="card p-4 mb-4">
          <form onSubmit={onSearch} className="row g-3 align-items-center">
            <div className="col-lg-9">
              <label htmlFor="my-tournament-search" className="form-label text-uppercase small mb-1">
                Search tournaments
              </label>
              <input
                id="my-tournament-search"
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
                to="/tournaments/my-tournaments"
                className="btn btn-outline-muted btn-sm px-3 mt-lg-4"
              >
                Clear
              </Link>
            </div>
          </form>
        </div>

        {!session.resolved && (
          <div className="card p-5 text-center">
            <div className="spinner-border text-primary mb-3 mx-auto" role="status" />
            <p className="text-secondary mb-0">Checking your session...</p>
          </div>
        )}

        {session.resolved && !session.logged && (
          <div className="card p-4 text-center">
            <h2 className="h5 fw-bold mb-2">Log in required</h2>
            <p className="text-secondary mb-3">
              You need an account to view your tournament registrations.
            </p>
            <div>
              <Link to="/login" className="btn btn-primary btn-sm">
                Log In
              </Link>
            </div>
          </div>
        )}

        {session.resolved && session.logged && (
          <>
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
                    <table className="table tournament-table mb-0 align-middle text-nowrap">
                      <thead>
                        <tr className="small text-uppercase">
                          <th scope="col" className="ps-3" style={{ width: "60px" }}>
                            Image
                          </th>
                          <th scope="col">Tournament</th>
                          <th scope="col">Date</th>
                          <th scope="col">Format</th>
                          <th scope="col">Status</th>
                          <th scope="col" className="text-end pe-3">
                            Action
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {tournaments.map((tournament) => {
                          const actionMeta = getTournamentActionMeta(tournament.status);
                          const date = formatTournamentDate(tournament.detail?.startDate);
                          const format = extractTournamentFormat(tournament.detail?.description);

                          return (
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
                              </td>
                              <td className="tournament-muted-cell">{date}</td>
                              <td className="tournament-muted-cell">{format}</td>
                              <td>
                                <TournamentStatusBadge status={tournament.status} />
                              </td>
                              <td className="text-end pe-3">
                                {actionMeta.actionDisabled ? (
                                  <button
                                    type="button"
                                    className="btn btn-outline-muted btn-sm"
                                    disabled
                                  >
                                    {actionMeta.actionLabel}
                                  </button>
                                ) : (
                                  <Link
                                    to={`/tournaments/detail/${tournament.id}`}
                                    className="btn btn-outline-muted btn-sm"
                                  >
                                    {actionMeta.actionLabel}
                                  </Link>
                                )}
                              </td>
                            </tr>
                          );
                        })}
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

              {!isLoading && !errorMessage && tournaments.length === 0 && (
                <div className="empty-state-panel text-center py-5 px-3">
                  <h2 className="h5 fw-bold mb-2">You are not registered in any tournament yet</h2>
                  <p className="text-secondary mb-3">
                    Browse open tournaments and register a bot when registration is available.
                  </p>
                  <Link to="/tournaments" className="btn btn-outline-muted btn-sm">
                    Browse tournaments
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
