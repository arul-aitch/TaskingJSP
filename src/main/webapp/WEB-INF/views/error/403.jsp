<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Forbidden"/>
<div class="error-wrap">
    <div class="error-code">403</div>
    <p class="error-msg">You do not have permission to perform that action.</p>
    <a href="${pageContext.request.contextPath}/onboarding" class="btn btn-primary">Go Home</a>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
