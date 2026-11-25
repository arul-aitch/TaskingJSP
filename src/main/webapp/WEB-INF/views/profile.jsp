<%@ include file="/WEB-INF/jsp/app-layout-start.jspf" %>
<%@ include file="/WEB-INF/jsp/sidebar.jspf" %>

<div class="main-area">
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
        <div class="profile-wrap">
            <h1 class="profile-title">Profile Settings</h1>

            <c:if test="${param.status == 'profile-updated'}">
                <div class="status-banner">Profile updated successfully.</div>
            </c:if>
            <c:if test="${param.status == 'password-updated'}">
                <div class="status-banner">Password updated successfully.</div>
            </c:if>

            <%-- Profile information --%>
            <div class="profile-card">
                <h2>Profile Information</h2>
                <p class="sub">Update your account's profile information and email address.</p>
                <form method="POST" action="${pageContext.request.contextPath}/profile">
                    <input type="hidden" name="csrf_token" value="${csrfToken}">
                    <input type="hidden" name="_method" value="PUT">
                    <div class="form-row">
                        <label class="label" for="name">Name</label>
                        <input id="name" name="name" type="text" class="input ${not empty profileErrors['name'] ? 'input-error' : ''}"
                               value="${not empty profileErrors['name'] ? param.name : user.name}" required autocomplete="name">
                        <c:if test="${not empty profileErrors['name']}"><p class="error-text">${profileErrors['name']}</p></c:if>
                    </div>
                    <div class="form-row">
                        <label class="label" for="email">Email</label>
                        <input id="email" name="email" type="email" class="input ${not empty profileErrors['email'] ? 'input-error' : ''}"
                               value="${not empty profileErrors['email'] ? param.email : user.email}" required autocomplete="username">
                        <c:if test="${not empty profileErrors['email']}"><p class="error-text">${profileErrors['email']}</p></c:if>
                        <c:if test="${empty user.emailVerifiedAt}">
                            <p style="font-size:13px; margin-top:8px; color:var(--ink-secondary);">
                                Your email address is unverified.
                            </p>
                        </c:if>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Save Changes</button>
                    </div>
                </form>
            </div>

            <%-- Update password --%>
            <div class="profile-card">
                <h2>Update Password</h2>
                <p class="sub">Ensure your account is using a long, random password to stay secure.</p>
                <form method="POST" action="${pageContext.request.contextPath}/password">
                    <input type="hidden" name="csrf_token" value="${csrfToken}">
                    <input type="hidden" name="_method" value="PUT">
                    <div class="form-row">
                        <label class="label" for="current_password">Current Password</label>
                        <input id="current_password" name="current_password" type="password"
                               class="input ${not empty passwordErrors['current_password'] ? 'input-error' : ''}"
                               required autocomplete="current-password">
                        <c:if test="${not empty passwordErrors['current_password']}"><p class="error-text">${passwordErrors['current_password']}</p></c:if>
                    </div>
                    <div class="form-row">
                        <label class="label" for="password">New Password</label>
                        <input id="password" name="password" type="password"
                               class="input ${not empty passwordErrors['password'] ? 'input-error' : ''}"
                               required autocomplete="new-password">
                        <c:if test="${not empty passwordErrors['password']}"><p class="error-text">${passwordErrors['password']}</p></c:if>
                    </div>
                    <div class="form-row">
                        <label class="label" for="password_confirmation">Confirm Password</label>
                        <input id="password_confirmation" name="password_confirmation" type="password" class="input" required autocomplete="new-password">
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Update Password</button>
                    </div>
                </form>
            </div>

            <%-- Delete account --%>
            <div class="profile-card" style="border-color: rgba(239,68,68,0.3);">
                <h2 style="color:var(--danger)">Delete Account</h2>
                <p class="sub">Once your account is deleted, all of its resources and data will be permanently deleted. Please enter your password to confirm you would like to permanently delete your account.</p>
                <c:if test="${not empty deleteError}"><p class="error-text">${deleteError}</p></c:if>
                <form method="POST" action="${pageContext.request.contextPath}/profile"
                      onsubmit="return confirm('Are you sure you want to delete your account? This cannot be undone.')">
                    <input type="hidden" name="csrf_token" value="${csrfToken}">
                    <input type="hidden" name="_method" value="DELETE">
                    <div class="form-row">
                        <label class="label" for="delete_password">Password</label>
                        <input id="delete_password" name="password" type="password" class="input" required autocomplete="current-password">
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-danger">Delete Account</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
