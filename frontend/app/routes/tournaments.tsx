import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";
import TournamentStatusBadge from "~/components/tournamentStatusBadge";
import TournamentThumbnail from "~/components/tournamentThumbnail";
import { useSessionState } from "~/hooks/use-session-state";
import {
  TOURNAMENT_PAGE_SIZE,
  fetchTournament,
  fetchTournamentPage,
  getTournamentActionMeta,
  type TournamentDetail,
  type TournamentListItem,
} from "~/services/tournament-service";

interface EnrichedTournament extends TournamentListItem {
  detail: TournamentDetail | null;
}

export default function Tournaments() {
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
  }, [urlQuery]);

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

  const fromItem = tournaments.length === 0 ? 0 : 1;
  const toItem = tournaments.length;

  return (
    <>
      <Header logged={session.logged} admin={session.admin} />

      <main className="container py-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
          <div>
            <p className="text-secondary mb-1 small text-uppercase">Season 2026</p>
            <h1 className="h3 fw-bold mb-0">Tournaments</h1>
          </div>
          {session.logged && !session.admin && (
            <Link to="/tournaments/my-tournaments" className="btn btn-outline-muted btn-sm">
              My Tournaments
            </Link>
          )}
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
                onChange={(event) => queueLiveSearch(event.target.value)}
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
                        Action
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {tournaments.map((tournament) => {
                      const actionMeta = getTournamentActionMeta(tournament.status);

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
                            {actionMeta.actionDisabled ? (
                              <button type="button" className="btn btn-outline-muted btn-sm" disabled>
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
