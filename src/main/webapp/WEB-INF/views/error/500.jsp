<%@ include file="/WEB-INF/jsp/guest-layout-start.jspf" %>
<c:set var="pageTitle" value="Error"/>
<div class="error-wrap">
    <div class="error-code">500</div>
    <p class="error-msg">Something went wrong on our side. Please try again.</p>
    <a href="${pageContext.request.contextPath}/onboarding" class="btn btn-primary">Go Home</a>
</div>
<%@ include file="/WEB-INF/jsp/layout-end.jspf" %>
