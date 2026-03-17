(() => {
  document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("tournament-search-form");
    const searchInput = document.getElementById("tournament-search");
    const tableBody = document.getElementById("tournament-table-body");
    const resultCounter = document.getElementById("tournament-counter");
    const loadMoreBtn = document.getElementById("show-more-tournaments-btn");

    let debounceTimer;
    let activeController;
    let nextPage = 0;
    let loading = false;

    const initialMeta = document.querySelector(".tournament-meta-row");
    if (initialMeta) {
      nextPage = Number(initialMeta.dataset.nextPage || "0");
    }

    let lastRequestedQuery = initialMeta ? (initialMeta.dataset.searchQuery || "") : "";
    if (!lastRequestedQuery && searchInput) {
      lastRequestedQuery = searchInput.value.trim();
    }

    const updateUrlQuery = (query) => {
      const currentUrl = new URL(window.location.href);
      if (query) {
        currentUrl.searchParams.set("q", query);
      } else {
        currentUrl.searchParams.delete("q");
      }
      window.history.replaceState({}, "", `${currentUrl.pathname}${currentUrl.search}`);
    };

    const parseRowsPayload = (html) => {
      const tempBody = document.createElement("tbody");
      tempBody.innerHTML = html;
      const metaRow = tempBody.querySelector(".tournament-meta-row");
      if (!metaRow) {
        throw new Error("Missing tournament metadata row");
      }

      const payload = {
        rowsHtml: "",
        searchQuery: metaRow.dataset.searchQuery || "",
        hasMore: metaRow.dataset.hasMore === "true",
        nextPage: Number(metaRow.dataset.nextPage || "0"),
        fromItem: metaRow.dataset.fromItem || "0",
        toItem: metaRow.dataset.toItem || "0",
        totalElements: metaRow.dataset.totalElements || "0",
      };

      metaRow.remove();
      payload.rowsHtml = tempBody.innerHTML;
      return payload;
    };

    const loadTournaments = async (query, page = 0, append = false) => {
      if (!tableBody || !resultCounter) {
        return;
      }

      if (activeController && !append) {
        activeController.abort();
      }

      const controller = new AbortController();
      if (!append) {
        activeController = controller;
      }

      try {
        const url = new URL("/tournaments/page", window.location.origin);
        if (query) {
          url.searchParams.set("q", query);
        }
        url.searchParams.set("page", page);

        const response = await fetch(url.toString(), {
          headers: { "X-Requested-With": "XMLHttpRequest" },
          signal: append ? null : controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Request failed: ${response.status}`);
        }

        const html = await response.text();
        const payload = parseRowsPayload(html);

        if (append) {
          const tempBody = document.createElement("tbody");
          tempBody.innerHTML = payload.rowsHtml;
          Array.from(tempBody.querySelectorAll("tr")).forEach((row) => tableBody.appendChild(row));
        } else {
          tableBody.innerHTML = payload.rowsHtml;
        }

        resultCounter.textContent = `Showing ${payload.fromItem}-${payload.toItem} of ${payload.totalElements}`;
        lastRequestedQuery = payload.searchQuery;
        nextPage = payload.nextPage;

        if (loadMoreBtn) {
          loadMoreBtn.hidden = !payload.hasMore;
          loadMoreBtn.disabled = false;
          loadMoreBtn.textContent = "Show more";
        }

        if (!append) {
          if (searchInput) {
            searchInput.value = payload.searchQuery;
          }
          updateUrlQuery(payload.searchQuery);
        }
      } catch (error) {
        if (error.name !== "AbortError") {
          console.error(error);
        }
      } finally {
        if (activeController === controller) {
          activeController = undefined;
        }
        loading = false;
      }
    };

    const refreshTournaments = (query) => {
      nextPage = 0;
      loadTournaments(query, 0, false);
    };

    const loadMoreTournaments = () => {
      if (loading) {
        return;
      }
      loading = true;
      if (loadMoreBtn) {
        loadMoreBtn.disabled = true;
        loadMoreBtn.textContent = "Loading...";
      }
      loadTournaments(lastRequestedQuery, nextPage, true);
    };

    if (loadMoreBtn) {
      loadMoreBtn.addEventListener("click", loadMoreTournaments);
    }

    const queueLiveSearch = () => {
      if (!searchInput) {
        return;
      }
      const query = searchInput.value.trim();
      if (query === lastRequestedQuery) {
        return;
      }

      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        refreshTournaments(query);
      }, 250);
    };

    if (searchInput) {
      searchInput.addEventListener("input", queueLiveSearch);
    }

    if (searchForm) {
      searchForm.addEventListener("submit", (event) => {
        event.preventDefault();
        if (!searchInput) {
          return;
        }

        clearTimeout(debounceTimer);
        refreshTournaments(searchInput.value.trim());
      });
    }
  });
})();
