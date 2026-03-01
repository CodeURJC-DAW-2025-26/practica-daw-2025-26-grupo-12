(() => {
  document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("admin-user-search-form");
    const searchInput = document.getElementById("admin-user-search");
    const statusSelect = document.getElementById("admin-user-status-filter");
    const tableBody = document.getElementById("admin-users-table-body");
    const resultCounter = document.getElementById("admin-users-result-counter");

    let debounceTimer;
    let activeController;
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
      metaRow.remove();

      return {
        rowsHtml: tempBody.innerHTML,
        resultCount,
        searchQuery,
        statusFilter,
      };
    };

    const refreshUsers = async (query, status) => {
      if (!tableBody || !resultCounter) {
        return;
      }

      if (activeController) {
        activeController.abort();
      }
      const controller = new AbortController();
      activeController = controller;

      try {
        const url = new URL("/admin/users/table", window.location.origin);
        if (query) {
          url.searchParams.set("q", query);
        }
        if (status && status !== "all") {
          url.searchParams.set("status", status);
        }

        const response = await fetch(url.toString(), {
          headers: { "X-Requested-With": "XMLHttpRequest" },
          signal: controller.signal,
        });
        if (!response.ok) {
          throw new Error(`Request failed: ${response.status}`);
        }

        const html = await response.text();
        const payload = parseRowsPayload(html);
        tableBody.innerHTML = payload.rowsHtml;
        resultCounter.textContent = `${payload.resultCount} results`;
        lastRequestedQuery = payload.searchQuery;
        lastRequestedStatus = payload.statusFilter;
        if (statusSelect) {
          statusSelect.value = payload.statusFilter;
        }
        updateUrlQuery(payload.searchQuery, payload.statusFilter);
      } catch (error) {
        if (error.name !== "AbortError") {
          console.error(error);
        }
      } finally {
        if (activeController === controller) {
          activeController = undefined;
        }
      }
    };

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

    const lockModal = document.getElementById("toggleUserLockModal");
    if (lockModal) {
      const form = document.getElementById("toggle-user-lock-form");
      const queryInput = document.getElementById("toggle-user-lock-query");
      const statusInput = document.getElementById("toggle-user-lock-status");
      const message = document.getElementById("toggle-user-lock-message");
      const submitButton = document.getElementById("toggle-user-lock-submit");

      lockModal.addEventListener("show.bs.modal", (event) => {
        const trigger = event.relatedTarget;
        if (!trigger) {
          return;
        }

        const userId = trigger.getAttribute("data-user-id");
        const userName = trigger.getAttribute("data-user-name");
        const userBlocked = trigger.getAttribute("data-user-blocked") === "true";
        const searchQuery = trigger.getAttribute("data-search-query") || "";
        const statusFilter = trigger.getAttribute("data-status-filter") || "all";
        const action = userBlocked ? "unblock" : "block";

        form.action = `/admin/users/${userId}/${action}`;
        queryInput.value = searchQuery;
        if (statusInput) {
          statusInput.value = statusFilter;
        }

        if (userBlocked) {
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
    }

    const deleteModal = document.getElementById("deleteUserModal");
    if (deleteModal) {
      const form = document.getElementById("delete-user-form");
      const queryInput = document.getElementById("delete-user-query");
      const statusInput = document.getElementById("delete-user-status");
      const message = document.getElementById("delete-user-message");

      deleteModal.addEventListener("show.bs.modal", (event) => {
        const trigger = event.relatedTarget;
        if (!trigger) {
          return;
        }

        const userId = trigger.getAttribute("data-user-id");
        const userName = trigger.getAttribute("data-user-name");
        const searchQuery = trigger.getAttribute("data-search-query") || "";
        const statusFilter = trigger.getAttribute("data-status-filter") || "all";

        form.action = `/admin/users/${userId}/delete`;
        queryInput.value = searchQuery;
        if (statusInput) {
          statusInput.value = statusFilter;
        }

        message.textContent = `Do you want to delete ${userName}? This will also remove the user's bots and match history.`;
      });
    }
  });
})();
