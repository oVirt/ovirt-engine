<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="baseTheme" value="${requestScope['brandingStyle'][0]}" />
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/patternfly/components/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/patternfly/components/bootstrap/dist/js/bootstrap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/patternfly/js/patternfly.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/insQ-1.0.3.min.js"></script>
