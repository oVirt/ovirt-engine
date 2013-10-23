<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setBundle basename="messages" var="docs"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="docs.missing.title" bundle="${docs}">
               <fmt:param value="${requestScope['locale'].getDisplayLanguage()}" />
           </fmt:message>
    </title>
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
        <div>
            <p>
                <fmt:message key="docs.missing.appears" bundle="${docs}">
                    <fmt:param value="${requestScope['locale'].getDisplayLanguage()}" />
                </fmt:message>
                <fmt:message key="docs.missing.admin" bundle="${docs}" />
            </p>
            <p>
                <a href="${requestScope['englishHref']}"><fmt:message key="docs.missing.click_here" bundle="${docs}" /></a>
                <fmt:message key="docs.missing.appear_once" bundle="${docs}" />
            </p>
        </div>
    </div>
</body>
</html>