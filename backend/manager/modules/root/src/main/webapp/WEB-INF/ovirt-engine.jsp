<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.ovirt.engine.core.languages" var="lang" />
<fmt:setLocale value="${requestScope['locale']}" />
<fmt:setBundle basename="org.ovirt.engine.core.messages"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <title><fmt:message key="splash.title" /></title>
    <link rel="stylesheet" href="ovirt-engine-style.css" type="text/css" media="screen, projection"/>
    <script src="engineVersion.js" type="text/javascript"></script>
    <script src="splash.js" type="text/javascript"></script>
</head>
<body onload="pageLoaded()">
    <div>
        <div class="left">
        </div>
        <div class="center">
        </div>
    </div>
    <div class="main">
        <div class="welcome"><fmt:message key="splash.welcome.text" /></div>
        <div class="welcome">
             <script type="text/JavaScript">
            <!--
            document.write('<fmt:message key="splash.version" />' + myVersion)
            //-->
            </script>
        </div>

        <noscript id="warningMessage" class="warningMessage">
            <b><fmt:message key="splash.browser.javascript1" /></b>
            <fmt:message key="splash.browser.javascript2" />
        </noscript>

        <div id='dynamicLinksSection' style="display: none;">
            <div>
            <h2>
                <span class="fakeH2"><fmt:message key="splash.menu.main" /></span>
            </h2>
                <c:set var="localeParam" value="?locale=${requestScope['locale'].toString()}"/>
                <c:if test="${requestScope['locale'].toString() == 'en_US'}">
                    <c:set var="localeParam" value=""/>
                </c:if>
                <div><a href="/UserPortal/org.ovirt.engine.ui.userportal.UserPortal/UserPortal.html${localeParam}"><fmt:message key="splash.menu.userportal" /></a></div>
                <div><a href="/webadmin/webadmin/WebAdmin.html${localeParam}"><fmt:message key="splash.menu.adminportal" /></a></div>
                <div><a href="/OvirtEngineWeb/RedirectServlet?Page=Reports"><fmt:message key="splash.menu.reportsportal" /></a></div>
            </div>
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
