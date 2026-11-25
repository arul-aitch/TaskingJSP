/* ============================================================
   TaskingJSP — app.js
   ============================================================ */

(function () {
    "use strict";

    function closeAllMenus() {
        document
            .querySelectorAll(".actions-menu.open")
            .forEach(function (menu) {
                menu.classList.remove("open");
            });
    }

    document.addEventListener("click", function (e) {
        var btn = e.target.closest(".actions-btn");
        if (btn) {
            var menu = btn.parentElement.querySelector(".actions-menu");
            if (menu) {
                var wasOpen = menu.classList.contains("open");
                closeAllMenus();
                if (!wasOpen) menu.classList.add("open");
            }
            return;
        }
        if (!e.target.closest(".actions-menu")) closeAllMenus();

        var addBtn = e.target.closest(".add-subtask-btn");
        if (addBtn) {
            var form = addBtn.nextElementSibling;
            if (form) {
                form.classList.remove("hidden");
                form.style.display = "flex";
                addBtn.style.display = "none";
                if (form.querySelector("input"))
                    form.querySelector("input").focus();
            }
            return;
        }

        document.querySelectorAll(".subtask-form").forEach(function (form) {
            if (
                form.style.display !== "none" &&
                form.style.display !== "" &&
                !form.contains(e.target)
            ) {
                form.style.display = "none";
                var previous = form.previousElementSibling;
                if (previous && previous.classList.contains("add-subtask-btn"))
                    previous.style.display = "";
            }
        });
    });

    window.openModal = function (id) {
        var dialog = document.getElementById(id);
        if (!dialog) return;

        if (dialog.parentNode !== document.body) {
            document.body.appendChild(dialog);
        }

        if (dialog.open) {
            dialog.close();
        }
        dialog.showModal();
    };

    window.closeModal = function (id) {
        var dialog = document.getElementById(id);
        if (dialog && dialog.open) dialog.close();
    };

    document.querySelectorAll("[data-close-modal]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var dialog = btn.closest("dialog");
            if (dialog) dialog.close();
        });
    });

    document.addEventListener("click", function (e) {
        var editBtn = e.target.closest("[data-edit-task]");
        if (!editBtn) return;
        try {
            var data = JSON.parse(editBtn.getAttribute("data-edit-task"));
            var form = document.getElementById("form_task_edit");
            if (!form) return;
            form.action = form
                .getAttribute("data-base-action")
                .replace("__ID__", data.id);
            document.getElementById("edit_task_id_display").textContent =
                "#" + data.id;
            document.getElementById("edit_title").value = data.title || "";
            document.getElementById("edit_description").value =
                data.description || "";
            document.getElementById("edit_deadline").value =
                data.deadline || "";
            document.getElementById("edit_category").value =
                data.categoryId || "";
            openModal("modal_edit_task");
        } catch (error) {}
    });

    document.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-edit-category]");
        if (!btn) return;
        var id = btn.getAttribute("data-edit-category");
        var name = btn.getAttribute("data-category-name");
        var form = document.getElementById("form_category_edit");
        if (!form) return;
        form.action = form
            .getAttribute("data-base-action")
            .replace("__ID__", id);
        document.getElementById("edit_category_name").value = name || "";
        openModal("modal_edit_category");
    });

    var pendingDelete = null;
    document.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-confirm-delete]");
        if (!btn) return;
        e.preventDefault();
        pendingDelete = {
            form: btn.closest("form"),
            message:
                btn.getAttribute("data-confirm-message") || "Are you sure?",
        };
        var message = document.getElementById("confirm_message");
        if (message) message.textContent = pendingDelete.message;
        openModal("modal_confirm_delete");
    });

    var confirmBtn = document.getElementById("confirm_delete_btn");
    if (confirmBtn) {
        confirmBtn.addEventListener("click", function () {
            if (pendingDelete && pendingDelete.form)
                pendingDelete.form.submit();
        });
    }

    var searchInput = document.getElementById("search-input");
    if (searchInput) {
        var searchTimer = null;
        searchInput.addEventListener("input", function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                submitFilters();
            }, 600);
        });
    }

    var sortSelect = document.getElementById("sort-select");
    if (sortSelect) sortSelect.addEventListener("change", submitFilters);

    function submitFilters() {
        var params = new URLSearchParams(window.location.search);
        var search = document.getElementById("search-input");
        var sort = document.getElementById("sort-select");
        if (search && search.value) params.set("search", search.value);
        else params.delete("search");
        if (sort && sort.value) params.set("sort", sort.value);
        else params.delete("sort");
        window.location.href =
            window.location.pathname +
            (params.toString() ? "?" + params.toString() : "");
    }

    document.addEventListener("submit", async function (e) {
        var form = e.target;
        if (!form.matches("form.ajax-toggle-form")) return;
        e.preventDefault();
        var button = form.querySelector('button[type="submit"]');
        if (button) button.disabled = true;
        try {
            var response = await fetch(form.action, {
                method: "POST",
                body: new URLSearchParams(new FormData(form)),
                credentials: "same-origin",
                redirect: "follow",
                headers: { "X-Requested-With": "XMLHttpRequest" },
            });
            if (!response.ok) throw new Error("HTTP " + response.status);
            window.location.assign(response.url || window.location.href);
        } catch (error) {
            window.alert("Gagal mengubah status. " + error.message);
            if (button) button.disabled = false;
        }
    });

    var menuToggle = document.getElementById("menu-toggle");
    if (menuToggle) {
        menuToggle.addEventListener("click", function () {
            var sidebar = document.querySelector(".sidebar");
            if (sidebar) sidebar.classList.toggle("mobile-open");
        });
    }
})();
