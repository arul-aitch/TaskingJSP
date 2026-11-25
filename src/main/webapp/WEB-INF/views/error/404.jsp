<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Not Found"/>
<div class="error-wrap">
    <div class="error-code">404</div>
    <p class="error-msg">The page you are looking for could not be found.</p>
    <a href="${pageContext.request.contextPath}/onboarding" class="btn btn-primary">Go Home</a>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
