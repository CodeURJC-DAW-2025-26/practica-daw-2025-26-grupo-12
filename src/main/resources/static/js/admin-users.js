(() => {
  document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("admin-user-search-form");
    const searchInput = document.getElementById("admin-user-search");
    const statusSelect = document.getElementById("admin-user-status-filter");
    const tableBody = document.getElementById("admin-users-table-body");
    const resultCounter = document.getElementById("admin-users-result-counter");

    let debounceTimer;
    let activeController;
    let nextPage = 0;
    let loading = false;

    const initialMeta = document.querySelector(".admin-user-meta-row");
    if (initialMeta) {
      nextPage = Number(initialMeta.dataset.nextPage || "0");
    }

    let lastRequestedQuery = searchInput ? searchInput.value.trim() : "";
    let lastRequestedStatus = statusSelect ? statusSelect.value : "all";

    const updateUrlQuery = (query, status) => {
      const currentUrl = new URL(window.location.href);
      if (query) {
        currentUrl.searchParams.set("q", query);
      } else {
        currentUrl.searchParams.delete("q");
      }
      if (status && status !== "all") {
        currentUrl.searchParams.set("status", status);
      } else {
        currentUrl.searchParams.delete("status");
      }
      window.history.replaceState({}, "", `${currentUrl.pathname}${currentUrl.search}`);
    };

    const parseRowsPayload = (html) => {
      const tempBody = document.createElement("tbody");
      tempBody.innerHTML = html;
      const metaRow = tempBody.querySelector(".admin-user-meta-row");
      if (!metaRow) {
        throw new Error("Missing user search metadata row");
      }

      const resultCount = metaRow.dataset.resultCount || "0";
      const searchQuery = metaRow.dataset.searchQuery || "";
      const statusFilter = metaRow.dataset.statusFilter || "all";
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
        statusFilter,
        hasMore,
        nextPage: next,
        fromItem,
        toItem,
        totalElements
      };
    };

    const loadUsers = async (query, status, page = 0, append = false) => {
      if (!tableBody || !resultCounter) {
        return;
      }

      if (activeController && !append) {
        activeController.abort();
      }

      const controller = new AbortController();
      if (!append) activeController = controller;

      try {
        const url = new URL("/admin/users/table", window.location.origin);
        if (query) url.searchParams.set("q", query);
        if (status && status !== "all") url.searchParams.set("status", status);
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
        lastRequestedStatus = payload.statusFilter;
        nextPage = payload.nextPage;

        const loadMoreBtn = document.getElementById("show-more-admin-users-btn");
        if (loadMoreBtn) {
          loadMoreBtn.hidden = !payload.hasMore;
          loadMoreBtn.disabled = false;
          loadMoreBtn.textContent = "Show more";
        }

        if (!append) {
          if (statusSelect) statusSelect.value = payload.statusFilter;
          updateUrlQuery(payload.searchQuery, payload.statusFilter);
        }
      } catch (error) {
        if (error.name !== "AbortError") console.error(error);
      } finally {
        if (activeController === controller) activeController = undefined;
        loading = false;
      }
    };

    const refreshUsers = (query, status) => {
      nextPage = 0;
      loadUsers(query, status, 0, false);
    };

    const loadMoreUsers = () => {
      if (loading) return;
      loading = true;
      const loadMoreBtn = document.getElementById("show-more-admin-users-btn");
      if (loadMoreBtn) {
        loadMoreBtn.disabled = true;
        loadMoreBtn.textContent = "Loading...";
      }
      loadUsers(lastRequestedQuery, lastRequestedStatus, nextPage, true);
    };

    const loadMoreBtn = document.getElementById("show-more-admin-users-btn");
    if (loadMoreBtn) {
      loadMoreBtn.addEventListener("click", loadMoreUsers);
    }

    const queueLiveSearch = () => {
      if (!searchInput) {
        return;
      }
      const query = searchInput.value.trim();
      const status = statusSelect ? statusSelect.value : "all";
      if (query === lastRequestedQuery && status === lastRequestedStatus) {
        return;
      }

      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        refreshUsers(query, status);
      }, 250);
    };

    const applyStatusFilter = () => {
      if (!searchInput) {
        return;
      }
      clearTimeout(debounceTimer);
      const query = searchInput.value.trim();
      const status = statusSelect ? statusSelect.value : "all";
      if (query === lastRequestedQuery && status === lastRequestedStatus) {
        return;
      }
      refreshUsers(query, status);
    };

    if (searchInput) {
      searchInput.addEventListener("input", queueLiveSearch);
    }
    if (statusSelect) {
      statusSelect.addEventListener("change", applyStatusFilter);
    }

    if (searchForm) {
      searchForm.addEventListener("submit", (event) => {
        event.preventDefault();
        if (!searchInput) {
          return;
        }

        clearTimeout(debounceTimer);
        const query = searchInput.value.trim();
        const status = statusSelect ? statusSelect.value : "all";
        refreshUsers(query, status);
      });
    }

    const modal = document.getElementById("userActionModal");
    if (!modal) {
      return;
    }

    const form = document.getElementById("user-action-form");
    const queryInput = document.getElementById("user-action-query");
    const statusInput = document.getElementById("user-action-status");
    const message = document.getElementById("user-action-message");
    const submitButton = document.getElementById("user-action-submit");

    modal.addEventListener("show.bs.modal", (event) => {
      const trigger = event.relatedTarget;
      if (!trigger) {
        return;
      }

      const userId = trigger.getAttribute("data-user-id");
      const userName = trigger.getAttribute("data-user-name");
      const userAction = trigger.getAttribute("data-user-action");
      const userBlocked = trigger.getAttribute("data-user-blocked") === "true";
      const searchQuery = trigger.getAttribute("data-search-query") || "";
      const statusFilter = trigger.getAttribute("data-status-filter") || "all";
      const action = userAction || (userBlocked ? "unblock" : "block");

      form.action = `/admin/users/${userId}/${action}`;
      queryInput.value = searchQuery;
      if (statusInput) {
        statusInput.value = statusFilter;
      }

      if (action === "delete") {
        message.textContent = `Do you want to delete ${userName}? This action cannot be undone.`;
        submitButton.textContent = "Delete user";
        submitButton.classList.remove("btn-primary");
        submitButton.classList.add("btn-danger");
      } else if (userBlocked) {
        message.textContent = `Do you want to unblock ${userName}? The user will be able to log in again.`;
        submitButton.textContent = "Unblock user";
        submitButton.classList.remove("btn-danger");
        submitButton.classList.add("btn-primary");
      } else {
        message.textContent = `Do you want to block ${userName}? All active sessions will be closed immediately.`;
        submitButton.textContent = "Block user";
        submitButton.classList.remove("btn-primary");
        submitButton.classList.add("btn-danger");
      }
    });
  });
})();
