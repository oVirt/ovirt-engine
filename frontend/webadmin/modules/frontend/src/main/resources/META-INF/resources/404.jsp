<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8" />
<title><fmt:message key="obrand.pagenotfound.page_not_found" /></title>
    <c:if test="${requestScope['brandingStyle'] != null}">
        <c:forEach items="${requestScope['brandingStyle']}" var="theme">
            <c:if test="${theme.getThemeStyleSheet(requestScope['applicationType']) != null}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/theme${theme.path}/${theme.getThemeStyleSheet(requestScope['applicationType'])}">
            </c:if>
        </c:forEach>
    </c:if>
</head>
<body>
    <div>
        <div class="obrand_left">
            <div class="obrand_header_nav">
                <fmt:message key="obrand.pagenotfound.header.main" />
            </div>
        </div>
        <div class="obrand_right">
        </div>
        <div class="obrand_center">
        </div>
    </div>
    <div class="obrand_main">
        <h2><span class="obrand_section_header"><fmt:message key="obrand.pagenotfound.page_not_found" /></span></h2>
        <a href="/ovirt-engine"><fmt:message key="obrand.pagenotfound.link" /></a>
    </div>
</body>
</html>
