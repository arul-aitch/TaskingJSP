<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Login"/>
<div class="guest-card">
    <div class="guest-head">
        <h2 class="guest-title">Welcome back</h2>
        <p class="guest-subtitle">Please enter your details to sign in.</p>
    </div>

    <c:if test="${not empty status}">
        <div class="status-banner">${status}</div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/login">
        <input type="hidden" name="csrf_token" value="${csrfToken}">
        <div class="form-row">
            <label class="label" for="email">Email</label>
            <input id="email" name="email" type="email" class="input ${not empty errors['email'] ? 'input-error' : ''}"
                   value="${oldEmail}" required autofocus autocomplete="username" placeholder="you@example.com">
            <c:if test="${not empty errors['email']}"><p class="error-text">${errors['email']}</p></c:if>
        </div>
        <div class="form-row">
            <div class="form-row-flex">
                <label class="label" for="password" style="margin-bottom:0">Password</label>
                <a class="forgot-link" href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
            </div>
            <input id="password" name="password" type="password" class="input ${not empty errors['password'] ? 'input-error' : ''}"
                   required autocomplete="current-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;">
            <c:if test="${not empty errors['password']}"><p class="error-text">${errors['password']}</p></c:if>
        </div>
        <label class="checkbox-row">
            <input id="remember_me" type="checkbox" name="remember">
            <span>Remember me</span>
        </label>
        <button type="submit" class="btn btn-primary btn-block">Log in</button>
    </form>

    <div class="auth-switch">
        <span>Don't have an account?</span>
        <a href="${pageContext.request.contextPath}/register" class="forgot-link">Sign up</a>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
