<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="languages" var="lang" />
<!DOCTYPE html>
<html class="obrand_background">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="obrand.welcome.title" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
    <script src="welcome-locale-selector.js" type="text/javascript"></script>
</head>
<body>
    <a href="<obrand:messages key="obrand.common.vendor_url"/>" class="obrand_loginPageLogoImageLink">
         <span class="obrand_loginPageLogoImage"></span>
    </a>
    <div class="ovirt-container">
        <obrand:header />
        <div class="container">
            <div class="row">

                <div class="col-sm-12">
                    <div id="brand">
                        <div class="obrand_loginFormLogoImage"></div>
                    </div>
                </div>
                <noscript>
                    <div class="well col-sm-11 well-sm" id="well-error">
                        <span class="label label-default" id="well-error-label">
                            <b><fmt:message key="obrand.welcome.browser.javascript1" /></b>
                            <fmt:message key="obrand.welcome.browser.javascript2" />
                        </span>
                    </div>
                    <div style="clear: both;"></div>
                </noscript>

                <div class="col-sm-12 welcome-title-wrapper">
                    <span class="welcome-title"><fmt:message key="obrand.welcome.welcome.text" /></span>
                    <script type="text/JavaScript">
                    <!--
                    document.write('<span class="version-text"><fmt:message key="obrand.welcome.version"><fmt:param value="${requestScope[\'version\']}" /> </fmt:message></span>')
                    //-->
                    </script>
                </div>

                <div style="clear: both;"></div>

                <div class="col-sm-12">
                    ${requestScope['sections'].toString()}
                </div>

                <div style="clear: both;"></div>
                <div class="col-sm-12 locale-div">
                    <select class="gwt-ListBox obrand_locale_list_box" onchange="localeSelected(this)" id="localeBox">
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
                </div>
            </div>
        </div>
    </div>
</body>
</html>
