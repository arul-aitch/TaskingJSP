<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Welcome"/>
<div class="onboarding-wrap">
    <div class="onboarding-logo">T</div>
    <h1 class="onboarding-title">Welcome to Tasking</h1>
    <p class="onboarding-sub">
        A simple, focused task manager. Organize your work with categories,
        deadlines and subtasks — and get things done.
    </p>
    <div class="onboarding-actions">
        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-lg">Get Started</a>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline btn-lg">Log in</a>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
