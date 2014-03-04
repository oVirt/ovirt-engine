<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="pagenotfound" />
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8" />
<obrand:favicon />
<title><fmt:message key="pagenotfound.page_not_found" bundle="${pagenotfound}" /></title>
<obrand:stylesheets />
</head>
<body>
    <div>
        <div class="obrand_left">
            <div class="obrand_header_nav">
                <fmt:message key="obrand.welcome.header.main" />
            </div>
        </div>
        <div class="obrand_right">
        </div>
        <div class="obrand_center">
        </div>
    </div>
    <div class="obrand_main">
        <h2><span class="obrand_section_header"><fmt:message key="pagenotfound.page_not_found" bundle="${pagenotfound}" /></span></h2>
        <a href="/ovirt-engine"><fmt:message key="pagenotfound.link" bundle="${pagenotfound}" /></a>
    </div>
</body>
</html>
