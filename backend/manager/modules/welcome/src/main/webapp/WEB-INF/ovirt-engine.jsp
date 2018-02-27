<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="languages" var="lang" />
<!DOCTYPE html>
<html class="login-pf">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title><fmt:message key="obrand.welcome.title" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
    <script src="welcome-locale-selector.js" type="text/javascript"></script>
</head>
<body>
    <div class="obrand_landingBgTop"></div>
    <div class="obrand_landingBgBottom"></div>

    <a href="<obrand:messages key="obrand.common.vendor_url"/>" class="obrand_loginPageLogoLink">
        <span class="obrand_loginPageLogo"></span>
    </a>
    <div class="ovirt-container">
        <obrand:header />
        <div class="container">
            <div id="welcome-section">
                <div id="welcome-title"><fmt:message key="obrand.welcome.welcome.text" /></div>
                <div class="obrand_middleLogoName"></div>
                <div id="welcome-version-text"><fmt:message key="obrand.welcome.version"><fmt:param value="${requestScope['version']}" /></fmt:message></div>
            </div>

            <div class="row">
                <noscript>
                    <div class="well col-sm-11 well-sm" id="well-error">
                        <span class="label label-default" id="well-error-label">
                            <b><fmt:message key="obrand.welcome.browser.javascript1" /></b>
                            <fmt:message key="obrand.welcome.browser.javascript2" />
                        </span>
                    </div>
                    <div style="clear: both;"></div>
                </noscript>

                <div class="col-sm-7">
                    <c:if test="${sessionScope.error_description != null && sessionScope.error_description != '' }">
                        <div class="alert alert-warning alert-dismissable">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
                                <span class="pficon pficon-close"></span>
                            </button>
                            <span class="pficon pficon-warning-triangle-o"></span>
                            ${sessionScope.error_description}
                        </div>
                        <c:remove var="error" scope="session"/>
                        <c:remove var="error_description" scope="session"/>
                    </c:if>
                </div>

                <div style="clear: both;"></div>

                <div id="welcome-band-top" class="col-sm-12">
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
