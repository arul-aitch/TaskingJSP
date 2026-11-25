<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Register"/>
<div class="guest-card">
    <div class="guest-head">
        <h2 class="guest-title">Create account</h2>
        <p class="guest-subtitle">Sign up to start organizing your tasks.</p>
    </div>

    <c:if test="${not empty status}">
        <div class="status-banner">${status}</div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/register">
        <input type="hidden" name="csrf_token" value="${csrfToken}">
        <div class="form-row">
            <label class="label" for="name">Name</label>
            <input id="name" name="name" type="text" class="input ${not empty errors['name'] ? 'input-error' : ''}"
                   value="${oldName}" required autofocus autocomplete="name" placeholder="Your name">
            <c:if test="${not empty errors['name']}"><p class="error-text">${errors['name']}</p></c:if>
        </div>
        <div class="form-row">
            <label class="label" for="email">Email</label>
            <input id="email" name="email" type="email" class="input ${not empty errors['email'] ? 'input-error' : ''}"
                   value="${oldEmail}" required autocomplete="username" placeholder="you@example.com">
            <c:if test="${not empty errors['email']}"><p class="error-text">${errors['email']}</p></c:if>
        </div>
        <div class="form-row">
            <label class="label" for="password">Password</label>
            <input id="password" name="password" type="password" class="input ${not empty errors['password'] ? 'input-error' : ''}"
                   required autocomplete="new-password" placeholder="At least 8 characters">
            <c:if test="${not empty errors['password']}"><p class="error-text">${errors['password']}</p></c:if>
        </div>
        <div class="form-row">
            <label class="label" for="password_confirmation">Confirm Password</label>
            <input id="password_confirmation" name="password_confirmation" type="password"
                   class="input" required autocomplete="new-password" placeholder="Repeat your password">
        </div>
        <button type="submit" class="btn btn-primary btn-block">Create account</button>
    </form>

    <div class="auth-switch">
        <span>Already have an account?</span>
        <a href="${pageContext.request.contextPath}/login" class="forgot-link">Log in</a>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
