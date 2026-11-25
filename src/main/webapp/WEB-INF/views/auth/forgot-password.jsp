<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Forgot Password"/>
<div class="guest-card">
    <div class="guest-head">
        <h2 class="guest-title">Forgot password</h2>
        <p class="guest-subtitle">Enter your email and we will send you a reset link.</p>
    </div>

    <c:if test="${not empty status}">
        <div class="status-banner">If an account with that email exists, a password reset link has been sent.</div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/forgot-password">
        <input type="hidden" name="csrf_token" value="${csrfToken}">
        <div class="form-row">
            <label class="label" for="email">Email</label>
            <input id="email" name="email" type="email" class="input ${not empty errors['email'] ? 'input-error' : ''}"
                   value="${oldEmail}" required autofocus placeholder="you@example.com">
            <c:if test="${not empty errors['email']}"><p class="error-text">${errors['email']}</p></c:if>
        </div>
        <button type="submit" class="btn btn-primary btn-block">Email password reset link</button>
    </form>

    <div class="auth-switch">
        <a href="${pageContext.request.contextPath}/login" class="forgot-link">Back to login</a>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
