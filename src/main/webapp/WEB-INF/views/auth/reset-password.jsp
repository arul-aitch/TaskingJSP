<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Reset Password"/>
<div class="guest-card">
    <div class="guest-head">
        <h2 class="guest-title">Reset password</h2>
        <p class="guest-subtitle">Enter your new password below.</p>
    </div>

    <form method="POST" action="${pageContext.request.contextPath}/reset-password">
        <input type="hidden" name="csrf_token" value="${csrfToken}">
        <input type="hidden" name="token" value="${param.token}">
        <div class="form-row">
            <label class="label" for="email">Email</label>
            <input id="email" name="email" type="email" class="input" required value="${resetEmail}" placeholder="you@example.com">
        </div>
        <div class="form-row">
            <label class="label" for="password">New Password</label>
            <input id="password" name="password" type="password" class="input ${not empty errors['password'] ? 'input-error' : ''}"
                   required autocomplete="new-password" placeholder="At least 8 characters">
            <c:if test="${not empty errors['password']}"><p class="error-text">${errors['password']}</p></c:if>
        </div>
        <div class="form-row">
            <label class="label" for="password_confirmation">Confirm Password</label>
            <input id="password_confirmation" name="password_confirmation" type="password" class="input"
                   required autocomplete="new-password">
        </div>
        <c:if test="${not empty errors['token']}"><p class="error-text">${errors['token']}</p></c:if>
        <button type="submit" class="btn btn-primary btn-block">Reset password</button>
    </form>

    <div class="auth-switch">
        <a href="${pageContext.request.contextPath}/login" class="forgot-link">Back to login</a>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
