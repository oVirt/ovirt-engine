<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="baseTheme" value="${requestScope['brandingStyle'][0]}" />
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/bundled/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/bundled/jquery-ui/jquery-ui.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/bundled/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/bundled/patternfly/js/patternfly.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}${initParam['obrandThemePath']}${baseTheme.path}/bundled/insertion-query/insQ.min.js"></script>
