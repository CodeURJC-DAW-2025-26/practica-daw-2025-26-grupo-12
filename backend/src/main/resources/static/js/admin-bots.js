(() => {
    document.addEventListener("DOMContentLoaded", () => {
        const searchForm = document.getElementById("admin-bot-search-form");
        const searchInput = document.getElementById("admin-bot-search");
        const visibilitySelect = document.getElementById("admin-bot-visibility-filter");
        const tableBody = document.getElementById("admin-bots-table-body");
        const resultCounter = document.getElementById("admin-bots-result-counter");

        let debounceTimer;
        let activeController;
        let nextPage = 0;
        let loading = false;

        const initialMeta = document.querySelector(".admin-bot-meta-row");
        if (initialMeta) {
            nextPage = Number(initialMeta.dataset.nextPage || "0");
        }

        let lastRequestedQuery = searchInput ? searchInput.value.trim() : "";
        let lastRequestedVisibility = visibilitySelect ? visibilitySelect.value : "all";

        const updateUrlQuery = (query, visibility) => {
            const currentUrl = new URL(window.location.href);
            if (query) {
                currentUrl.searchParams.set("q", query);
            } else {
                currentUrl.searchParams.delete("q");
            }
            if (visibility && visibility !== "all") {
                currentUrl.searchParams.set("visibility", visibility);
            } else {
                currentUrl.searchParams.delete("visibility");
            }
            window.history.replaceState({}, "", `${currentUrl.pathname}${currentUrl.search}`);
        };

        const parseRowsPayload = (html) => {
            const tempBody = document.createElement("tbody");
            tempBody.innerHTML = html;
            const metaRow = tempBody.querySelector(".admin-bot-meta-row");
            if (!metaRow) {
                throw new Error("Missing bot search metadata row");
            }

            const resultCount = metaRow.dataset.resultCount || "0";
            const searchQuery = metaRow.dataset.searchQuery || "";
            const visibilityFilter = metaRow.dataset.visibilityFilter || "all";
            const hasMore = metaRow.dataset.hasMore === "true";
            const next = Number(metaRow.dataset.nextPage || "0");
            const fromItem = metaRow.dataset.fromItem || "0";
            const toItem = metaRow.dataset.toItem || "0";
            const totalElements = metaRow.dataset.totalElements || "0";

            metaRow.remove();

            return {
                rowsHtml: tempBody.innerHTML,
                resultCount,
                searchQuery,
                visibilityFilter,
                hasMore,
                nextPage: next,
                fromItem,
                toItem,
                totalElements
            };
        };

        const loadBots = async (query, visibility, page = 0, append = false) => {
            if (!tableBody || !resultCounter) {
                return;
            }

            if (activeController && !append) {
                activeController.abort();
            }

            const controller = new AbortController();
            if (!append) activeController = controller;

            try {
                const url = new URL("/admin/bots/table", window.location.origin);
                if (query) url.searchParams.set("q", query);
                if (visibility && visibility !== "all") url.searchParams.set("visibility", visibility);
                url.searchParams.set("page", page);

                const response = await fetch(url.toString(), {
                    headers: { "X-Requested-With": "XMLHttpRequest" },
                    signal: append ? null : controller.signal,
                });

                if (!response.ok) throw new Error(`Request failed: ${response.status}`);

                const html = await response.text();
                const payload = parseRowsPayload(html);

                if (append) {
                    const tempBody = document.createElement("tbody");
                    tempBody.innerHTML = payload.rowsHtml;
                    Array.from(tempBody.querySelectorAll("tr")).forEach(row => tableBody.appendChild(row));
                } else {
                    tableBody.innerHTML = payload.rowsHtml;
                }

                resultCounter.textContent = `Showing ${payload.fromItem}-${payload.toItem} of ${payload.totalElements}`;

                lastRequestedQuery = payload.searchQuery;
                lastRequestedVisibility = payload.visibilityFilter;
                nextPage = payload.nextPage;

                const loadMoreBtn = document.getElementById("show-more-admin-bots-btn");
                if (loadMoreBtn) {
                    loadMoreBtn.hidden = !payload.hasMore;
                    loadMoreBtn.disabled = false;
                    loadMoreBtn.textContent = "Show more";
                }

                if (!append) {
                    if (visibilitySelect) visibilitySelect.value = payload.visibilityFilter;
                    updateUrlQuery(payload.searchQuery, payload.visibilityFilter);
                }
            } catch (error) {
                if (error.name !== "AbortError") console.error(error);
            } finally {
                if (activeController === controller) activeController = undefined;
                loading = false;
            }
        };

        const refreshBots = (query, visibility) => {
            nextPage = 0;
            loadBots(query, visibility, 0, false);
        };

        const loadMoreBots = () => {
            if (loading) return;
            loading = true;
            const loadMoreBtn = document.getElementById("show-more-admin-bots-btn");
            if (loadMoreBtn) {
                loadMoreBtn.disabled = true;
                loadMoreBtn.textContent = "Loading...";
            }
            loadBots(lastRequestedQuery, lastRequestedVisibility, nextPage, true);
        };

        const loadMoreBtn = document.getElementById("show-more-admin-bots-btn");
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener("click", loadMoreBots);
        }

        const queueLiveSearch = () => {
            if (!searchInput) {
                return;
            }
            const query = searchInput.value.trim();
            const visibility = visibilitySelect ? visibilitySelect.value : "all";
            if (query === lastRequestedQuery && visibility === lastRequestedVisibility) {
                return;
            }

            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                refreshBots(query, visibility);
            }, 250);
        };

        const applyVisibilityFilter = () => {
            if (!searchInput) {
                return;
            }
            clearTimeout(debounceTimer);
            const query = searchInput.value.trim();
            const visibility = visibilitySelect ? visibilitySelect.value : "all";
            if (query === lastRequestedQuery && visibility === lastRequestedVisibility) {
                return;
            }
            refreshBots(query, visibility);
        };

        if (searchInput) {
            searchInput.addEventListener("input", queueLiveSearch);
        }
        if (visibilitySelect) {
            visibilitySelect.addEventListener("change", applyVisibilityFilter);
        }

        if (searchForm) {
            searchForm.addEventListener("submit", (event) => {
                event.preventDefault();
                if (!searchInput) {
                    return;
                }

                clearTimeout(debounceTimer);
                const query = searchInput.value.trim();
                const visibility = visibilitySelect ? visibilitySelect.value : "all";
                refreshBots(query, visibility);
            });
        }

        const deleteModal = document.getElementById("deleteBotModal");
        if (deleteModal) {
            deleteModal.addEventListener("show.bs.modal", (event) => {
                const button = event.relatedTarget;
                const botId = button.getAttribute("data-bot-id");
                const botName = button.getAttribute("data-bot-name");

                const formDelete = deleteModal.querySelector("#deleteBotForm");
                const nameSpan = deleteModal.querySelector("#deleteBotName");

                formDelete.action = `/bots/${botId}/delete`;
                nameSpan.textContent = botName;
            });
        }
    });
})();
