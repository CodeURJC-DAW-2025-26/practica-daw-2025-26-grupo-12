(() => {
  const initLoadMore = (button) => {
    const endpoint = button.dataset.endpoint;
    const bodyId = button.dataset.bodyId;
    const counterId = button.dataset.counterId;
    const metaClass = button.dataset.metaClass;
    const size = Number(button.dataset.size || 5);

    const tableBody = document.getElementById(bodyId);
    const counter = document.getElementById(counterId);

    if (!endpoint || !metaClass || !tableBody || !counter) {
      return;
    }

    let nextPage = 0;
    let loading = false;

    const setLoadingState = (isLoading) => {
      button.disabled = isLoading;
      button.textContent = isLoading ? "Loading..." : "Show more";
    };

    const updateCounter = (metaRow) => {
      const fromItem = metaRow.dataset.fromItem || "0";
      const toItem = metaRow.dataset.toItem || "0";
      const totalElements = metaRow.dataset.totalElements || "0";
      counter.textContent = `Showing ${fromItem}-${toItem} of ${totalElements}`;
    };

    const appendRowsFromServerHtml = (html) => {
      const tempBody = document.createElement("tbody");
      tempBody.innerHTML = html;

      const metaRow = tempBody.querySelector(`.${metaClass}`);
      if (!metaRow) {
        throw new Error("Missing pagination metadata row");
      }

      metaRow.remove();
      const rows = tempBody.querySelectorAll("tr");
      rows.forEach((row) => tableBody.appendChild(row));
      return metaRow;
    };

    const loadNextPage = async () => {
      if (loading) {
        return;
      }

      loading = true;
      setLoadingState(true);

      try {
        const response = await fetch(`${endpoint}?page=${nextPage}&size=${size}`, {
          headers: { "X-Requested-With": "XMLHttpRequest" },
        });

        if (!response.ok) {
          throw new Error(`Request failed: ${response.status}`);
        }

        const html = await response.text();
        const metaRow = appendRowsFromServerHtml(html);
        updateCounter(metaRow);

        const hasMore = metaRow.dataset.hasMore === "true";
        nextPage = Number(metaRow.dataset.nextPage || nextPage + 1);
        button.hidden = !hasMore;
        button.disabled = false;
        button.textContent = "Show more";
      } catch (error) {
        button.disabled = false;
        button.textContent = "Retry";
      } finally {
        loading = false;
      }
    };

    button.addEventListener("click", loadNextPage);
    loadNextPage();
  };

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-load-more='tournaments']").forEach(initLoadMore);
  });
})();
