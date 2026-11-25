<%@ include file="/WEB-INF/jsp/app-layout-start.jspf" %>
<%@ include file="/WEB-INF/jsp/sidebar.jspf" %>

<div class="main-area">
    <%-- Mobile topbar --%>
    <div class="topbar">
        <button type="button" class="icon-btn" id="menu-toggle" aria-label="Menu">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/></svg>
        </button>
        <div class="avatar">${fn:substring(user.name, 0, 1)}</div>
        <div class="topbar-name">${user.name}</div>
        <form method="POST" action="${pageContext.request.contextPath}/logout">
            <input type="hidden" name="csrf_token" value="${csrfToken}">
            <button type="submit" class="icon-btn" aria-label="Log out">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15m3 0 3-3m0 0-3-3m3 3H9"/></svg>
            </button>
        </form>
    </div>

    <div class="content">
        <div class="content-inner">

            <%-- Page head --%>
            <div class="page-head">
                <h1 class="page-title">${isArchive ? 'Archived Tasks' : 'All Tasks'}</h1>
                <div style="display:flex; align-items:center; gap:12px;">
                    <span id="task-count" class="task-count">${fn:length(tasks)} ${isArchive ? 'Archived Tasks' : 'Active Tasks'}</span>
                    <div class="progress-pill">
                        <div class="bar"><span style="width:${progress}%"></span></div>
                        ${progress}%
                    </div>
                </div>
            </div>

            <%-- Search + sort --%>
            <div class="filter-row">
                <div class="search-box">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                    <input type="text" id="search-input" value="${searchValue}" placeholder="Search tasks...">
                </div>
                <select name="sort" id="sort-select" class="select-sort">
                    <option value="deadline" ${sortValue == 'deadline' ? 'selected' : ''}>Nearest Deadline</option>
                    <option value="newest" ${sortValue == 'newest' ? 'selected' : ''}>Newest</option>
                </select>
            </div>

            <%-- Task list --%>
            <div id="task-list" class="task-list">
                <c:choose>
                    <c:when test="${empty tasks}">
                        <div class="empty-state">
                            <div class="icon">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                            </div>
                            <h3>${isArchive ? 'No archived tasks' : (not empty searchValue ? 'No matching tasks' : 'No tasks yet')}</h3>
                            <p>${isArchive ? 'Completed tasks will appear here.' : (not empty searchValue ? 'Try a different search term.' : 'Click the Add Task button to create your first task.')}</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="task" items="${tasks}">
                            <div class="task-card ${task.completed ? 'completed' : ''}" data-task-id="${task.id}">
                                <div class="task-main">

                                    <%-- Completion toggle --%>
                                    <form method="POST" class="ajax-toggle-form toggle-form"
                                          action="${pageContext.request.contextPath}/tasks/${task.id}/toggle">
                                        <input type="hidden" name="csrf_token" value="${csrfToken}">
                                        <input type="hidden" name="_method" value="PATCH">
                                        <input type="hidden" name="search" value="${searchValue}">
                                        <input type="hidden" name="category" value="${param.category}">
                                        <input type="hidden" name="sort" value="${sortValue}">
                                        <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                        <button type="submit" class="toggle-btn ${task.completed ? 'done' : ''}"
                                                aria-label="Toggle completion">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="m4.5 12.75 6 6 9-13.5"/></svg>
                                        </button>
                                    </form>

                                    <div class="task-body">
                                        <div class="task-title-row">
                                            <c:if test="${task.pinned}">
                                                <svg class="pin-icon" fill="currentColor" viewBox="0 0 24 24"><path d="M16 3v2l-1 1v5l3 2v2h-6v6l-1 1-1-1v-6H4v-2l3-2V6L6 5V3h10z"/></svg>
                                            </c:if>
                                            <span class="task-title">${task.title}</span>
                                        </div>

                                        <c:if test="${not empty task.description}">
                                            <p class="task-desc">${task.description}</p>
                                        </c:if>

                                        <div class="task-meta">
                                            <c:if test="${not empty task.categoryName}">
                                                <span class="meta-chip cat">${task.categoryName}</span>
                                            </c:if>
                                            <c:if test="${not empty task.deadline}">
                                                <span class="meta-chip deadline-${task.deadlineStatus}">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                                                    ${task.deadlineStatus == 'overdue' ? 'Overdue' : (task.deadlineStatus == 'urgent' ? 'Urgent' : 'Due')} &middot; ${task.deadlineLabel}
                                                </span>
                                            </c:if>
                                        </div>

                                        <%-- Subtasks --%>
                                        <div class="subtask-section">
                                            <c:if test="${task.subtaskTotal > 0}">
                                                <div class="subtask-progress-row">
                                                    <span>${task.subtaskCompleted}/${task.subtaskTotal} subtasks</span>
                                                    <div class="bar"><span style="width:${task.progressPercent}%"></span></div>
                                                </div>
                                                <div class="subtask-list">
                                                    <c:forEach var="sub" items="${task.subtasks}">
                                                        <div class="subtask-item ${sub.completed ? 'done' : ''}">
                                                            <form method="POST" class="ajax-toggle-form"
                                                                  action="${pageContext.request.contextPath}/subtasks/${sub.id}/toggle">
                                                                <input type="hidden" name="csrf_token" value="${csrfToken}">
                                                                <input type="hidden" name="_method" value="PATCH">
<input type="hidden" name="search" value="${searchValue}">
                                                                <input type="hidden" name="category" value="${param.category}">
                                                                <input type="hidden" name="sort" value="${sortValue}">
                                                                <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                                                <button type="submit" class="toggle-btn ${sub.completed ? 'done' : ''}" aria-label="Toggle subtask">
                                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="m4.5 12.75 6 6 9-13.5"/></svg>
                                                                </button>
                                                            </form>
                                                            <span class="st-title">${sub.title}</span>
                                                            <form method="POST" action="${pageContext.request.contextPath}/subtasks/${sub.id}">
                                                                <input type="hidden" name="csrf_token" value="${csrfToken}">
                                                                <input type="hidden" name="_method" value="DELETE">
<input type="hidden" name="search" value="${searchValue}">
                                                                <input type="hidden" name="category" value="${param.category}">
                                                                <input type="hidden" name="sort" value="${sortValue}">
                                                                <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                                                <button type="submit" class="st-del" aria-label="Delete subtask">
                                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:if>

                                            <%-- add subtask (hidden until button) --%>
                                            <button type="button" class="add-subtask-btn">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                                                Add subtask
                                            </button>
                                            <form method="POST" class="subtask-form"
                                                  action="${pageContext.request.contextPath}/subtasks/${task.id}"
                                                  style="display:none">
                                                <input type="hidden" name="csrf_token" value="${csrfToken}">
                                                <input type="hidden" name="search" value="${searchValue}">
                                                <input type="hidden" name="category" value="${param.category}">
                                                <input type="hidden" name="sort" value="${sortValue}">
                                                <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                                <input type="text" name="title" maxlength="255" placeholder="New subtask..." required>
                                                <button type="submit">Add</button>
                                            </form>
                                        </div>
                                    </div>
                                </div>

                                <%-- Actions dropdown --%>
                                <div class="task-actions">
                                    <button type="button" class="actions-btn" aria-label="Task actions">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5ZM12 12.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5ZM12 18.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5Z"/></svg>
                                    </button>
                                    <div class="actions-menu">
                                        <button type="button" data-edit-task='{"id": ${task.id}, "title": "${fn:escapeXml(task.title)}", "description": "${task.descriptionForJs}", "deadline": "${task.deadline}", "categoryId": "${task.categoryId}"}'>
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Z"/></svg>
                                            Edit Task
                                        </button>
                                        <form method="POST" action="${pageContext.request.contextPath}/tasks/${task.id}/pin">
                                            <input type="hidden" name="csrf_token" value="${csrfToken}">
                                            <input type="hidden" name="_method" value="PATCH">
<input type="hidden" name="search" value="${searchValue}">
                                            <input type="hidden" name="category" value="${param.category}">
                                            <input type="hidden" name="sort" value="${sortValue}">
                                            <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                            <button type="submit">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 3v2l-1 1v5l3 2v2h-6v6l-1 1-1-1v-6H4v-2l3-2V6L6 5V3h10z"/></svg>
                                                ${task.pinned ? 'Unpin Task' : 'Pin Task'}
                                            </button>
                                        </form>
                                        <form method="POST" action="${pageContext.request.contextPath}/tasks/${task.id}">
                                            <input type="hidden" name="csrf_token" value="${csrfToken}">
                                            <input type="hidden" name="_method" value="DELETE">
<input type="hidden" name="search" value="${searchValue}">
                                            <input type="hidden" name="category" value="${param.category}">
                                            <input type="hidden" name="sort" value="${sortValue}">
                                            <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
                                            <button type="submit" class="danger">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-1.9-2.164H5.9c-.99 0-1.9.984-1.9 2.164v.916"/></svg>
                                                Delete Task
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%-- Mobile FAB --%>
<button type="button" class="mobile-fab" onclick="openModal('modal_add_task')" aria-label="Add task">
    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
</button>

<%-- ============ Add Task modal ============ --%>
<dialog class="modal" id="modal_add_task">
    <div class="modal-head">
        <h2>Add Task</h2>
        <button type="button" class="modal-close" data-close-modal aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
        </button>
    </div>
    <div class="modal-body">
        <form method="POST" action="${pageContext.request.contextPath}/tasks">
            <input type="hidden" name="csrf_token" value="${csrfToken}">
            <input type="hidden" name="search" value="${searchValue}">
            <input type="hidden" name="category" value="${param.category}">
            <input type="hidden" name="sort" value="${sortValue}">
            <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
            <div class="form-row">
                <label class="label" for="add_title">Title <span style="color:var(--danger)">*</span></label>
                <input id="add_title" name="title" type="text" class="input" required maxlength="255" placeholder="Task title">
            </div>
            <div class="form-row">
                <label class="label" for="add_description">Description</label>
                <textarea id="add_description" name="description" class="input" placeholder="Optional description..."></textarea>
            </div>
            <div class="form-row">
                <label class="label" for="add_deadline">Deadline</label>
                <input id="add_deadline" name="deadline" type="date" class="input">
            </div>
            <div class="form-row">
                <label class="label" for="add_category">Category</label>
                <select id="add_category" name="category_id" class="select-sort" style="height:auto; padding:8px 32px 8px 12px;">
                    <option value="">No category</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}" ${param.category == cat.id ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-outline" data-close-modal>Cancel</button>
                <button type="submit" class="btn btn-primary">Add Task</button>
            </div>
        </form>
    </div>
</dialog>

<%-- ============ Edit Task modal ============ --%>
<dialog class="modal" id="modal_edit_task">
    <div class="modal-head">
        <h2>Edit Task <span id="edit_task_id_display" style="color:var(--ink-tertiary); font-size:13px;"></span></h2>
        <button type="button" class="modal-close" data-close-modal aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
        </button>
    </div>
    <div class="modal-body">
        <form method="POST" id="form_task_edit" data-base-action="${pageContext.request.contextPath}/tasks/__ID__">
            <input type="hidden" name="csrf_token" value="${csrfToken}">
            <input type="hidden" name="_method" value="PUT">
<input type="hidden" name="search" value="${searchValue}">
            <input type="hidden" name="category" value="${param.category}">
            <input type="hidden" name="sort" value="${sortValue}">
            <c:if test="${isArchive}"><input type="hidden" name="archive" value="1"></c:if>
            <div class="form-row">
                <label class="label" for="edit_title">Title <span style="color:var(--danger)">*</span></label>
                <input id="edit_title" name="title" type="text" class="input" required maxlength="255">
            </div>
            <div class="form-row">
                <label class="label" for="edit_description">Description</label>
                <textarea id="edit_description" name="description" class="input"></textarea>
            </div>
            <div class="form-row">
                <label class="label" for="edit_deadline">Deadline</label>
                <input id="edit_deadline" name="deadline" type="date" class="input">
            </div>
            <div class="form-row">
                <label class="label" for="edit_category">Category</label>
                <select id="edit_category" name="category_id" class="select-sort" style="height:auto; padding:8px 32px 8px 12px;">
                    <option value="">No category</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}">${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-outline" data-close-modal>Cancel</button>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </div>
        </form>
    </div>
</dialog>

<%-- ============ Add Category modal ============ --%>
<dialog class="modal" id="modal_add_category">
    <div class="modal-head">
        <h2>Add Category</h2>
        <button type="button" class="modal-close" data-close-modal aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
        </button>
    </div>
    <div class="modal-body">
        <form method="POST" action="${pageContext.request.contextPath}/categories">
            <input type="hidden" name="csrf_token" value="${csrfToken}">
            <div class="form-row">
                <label class="label" for="new_category_name">Name <span style="color:var(--danger)">*</span></label>
                <input id="new_category_name" name="name" type="text" class="input" required maxlength="255" placeholder="e.g. Work">
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-outline" data-close-modal>Cancel</button>
                <button type="submit" class="btn btn-primary">Add Category</button>
            </div>
        </form>
    </div>
</dialog>

<%-- ============ Edit Category modal ============ --%>
<dialog class="modal" id="modal_edit_category">
    <div class="modal-head">
        <h2>Edit Category</h2>
        <button type="button" class="modal-close" data-close-modal aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
        </button>
    </div>
    <div class="modal-body">
        <form method="POST" id="form_category_edit" data-base-action="${pageContext.request.contextPath}/categories/__ID__">
            <input type="hidden" name="csrf_token" value="${csrfToken}">
            <input type="hidden" name="_method" value="PUT">
            <div class="form-row">
                <label class="label" for="edit_category_name">Name <span style="color:var(--danger)">*</span></label>
                <input id="edit_category_name" name="name" type="text" class="input" required maxlength="255">
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-outline" data-close-modal>Cancel</button>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </div>
        </form>
    </div>
</dialog>

<%-- ============ Confirm delete modal ============ --%>
<dialog class="modal" id="modal_confirm_delete" style="width:min(400px, calc(100vw - 32px))">
    <div class="modal-head">
        <h2>Are you sure?</h2>
        <button type="button" class="modal-close" data-close-modal aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18 18 6M6 6l12 12"/></svg>
        </button>
    </div>
    <div class="modal-body">
        <p id="confirm_message" style="font-size:14px; color:var(--ink-secondary);"></p>
        <div class="form-actions">
            <button type="button" class="btn btn-outline" data-close-modal>Cancel</button>
            <button type="button" class="btn btn-danger" id="confirm_delete_btn">Delete</button>
        </div>
    </div>
</dialog>

<script>
    function confirmDelete(e, msg) {
        e.preventDefault();
        var form = e.target.closest('form');
        document.getElementById('confirm_message').textContent = msg.replace(/&quot;/g, '"');
        document.getElementById('confirm_delete_btn').onclick = function () {
            form.submit();
        };
        openModal('modal_confirm_delete');
        return false;
    }
</script>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
