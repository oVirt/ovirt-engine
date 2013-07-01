<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="languages" var="lang" />
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <title><fmt:message key="title" /></title>
    <c:if test="${requestScope['brandingStyle'] != null}">
        <c:forEach items="${requestScope['brandingStyle']}" var="theme">
            <c:if test="${theme.getThemeStyleSheet(requestScope['applicationType']) != null}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/theme${theme.path}/${theme.getThemeStyleSheet(requestScope['applicationType'])}">
            </c:if>
        </c:forEach>
    </c:if>
    <script src="engineVersion.js" type="text/javascript"></script>
    <script src="splash.js" type="text/javascript"></script>
</head>
<body onload="pageLoaded()">
    <div>
        <div class="left">
            <div class="header_nav">
                <fmt:message key="header.main" />
            </div>
        </div>
        <div class="right">
        </div>
        <div class="center">
        </div>
    </div>
    <div class="main">
        <div class="welcome"><fmt:message key="welcome.text" /></div>
        <div class="welcome">
             <script type="text/JavaScript">
            <!--
            document.write('<fmt:message key="version"><fmt:param value="${requestScope[\'version\']}" /> </fmt:message>')
            //-->
            </script>
        </div>

        <noscript id="warningMessage" class="warningMessage">
            <b><fmt:message key="browser.javascript1" /></b>
            <fmt:message key="browser.javascript2" />
        </noscript>
        <div id='dynamicLinksSection' style="display: none;">
            ${requestScope['sections'].toString()}
        </div>
        <div class="locale_select_panel">
            <select class="gwt-ListBox locale_list_box" onchange="localeSelected(this)">
            <c:forEach items="${requestScope['localeKeys']}" var="localeKey">
                <c:choose>
                <c:when test="${requestScope['locale'].toString() == localeKey}">
                    <c:set var="selectedLocale" value="${localeKey}"/>
                    <option value="${localeKey}" selected="selected"><fmt:message key="${localeKey}" bundle="${lang}"/></option>
                </c:when>
                <c:otherwise>
                    <option value="${localeKey}"><fmt:message key="${localeKey}" bundle="${lang}"/></option>
                </c:otherwise>
                </c:choose>
            </c:forEach>
            </select>
            <div class="gwt-Label locale_selected"><fmt:message key="${selectedLocale}" bundle="${lang}"/></div>
        </div>
    </div>
</body>
</html>
