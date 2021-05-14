<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="languages" var="lang" />

<!DOCTYPE html>
<html>
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
<body class="pf-c-page ovirt-container">
    <obrand:background-image />

    <obrand:header />
    <main class="pf-c-page__main">
        <section class="pf-c-page__main-section welcome-section">
            <div class="pf-l-split">
                <div class="pf-l-split__item">
                    <a href="<obrand:messages key="obrand.common.vendor_url" />" class="obrand_welcomePageLogoLink">
                        <span class="obrand_welcomePageLogo"></span>
                    </a>
                </div>
                <div class="pf-l-split__item obrand_welcomePageVersionText">
                    <fmt:message key="obrand.welcome.version">
                        <fmt:param value="${requestScope['version']}" />
                    </fmt:message>
                </div>
            </div>

            <div class="pf-l-stack">
                <noscript>
                    <div class="pf-l-stack__item noscript-error">
                        <div class="pf-c-alert pf-m-danger pf-m-inline" aria-label="Inline danger alert">
                            <div class="pf-c-alert__icon">
                                <i class="fas fa-exclamation-circle" aria-hidden="true"></i>
                            </div>
                            <h4 class="pf-c-alert__title">
                                <span class="pf-screen-reader">Success alert:</span><fmt:message key="obrand.welcome.browser.javascript1" />
                            </h4>
                            <div class="pf-c-alert__description">
                                <p><fmt:message key="obrand.welcome.browser.javascript2" /></p>
                            </div>
                        </div>
                    </div>
                </noscript>

                <c:if test="${sessionScope.error_description != null && sessionScope.error_description != ''}">
                    <div class="pf-l-stack__item session-error">
                        <div class="pf-c-alert pf-m-warning pf-m-inline" aria-label="Inline warning alert">
                            <div class="pf-c-alert__icon">
                                <i class="fas fa-exclamation-triangle" aria-hidden="true"></i>
                            </div>
                            <h4 class="pf-c-alert__title">
                                <span class="pf-screen-reader">Warning alert:</span><c:out value="${sessionScope.error_description}"/>
                            </h4>
                            <div class="pf-c-alert__action">
                                <button class="pf-c-button pf-m-plain" type="button" aria-label="Close warning alert">
                                    <i class="fas fa-times" aria-hidden="true"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                    <c:remove var="error" scope="session"/>
                    <c:remove var="error_description" scope="session"/>
                </c:if>

                <div class="pf-l-stack__item welcome-preamble-template">
                    <%-- This is where the __welcome-preamble__ template is inserted --%>
                    ${requestScope['preamble'].toString()}
                </div>

                <div class="pf-l-stack__item welcome-template">
                    <%-- This is where the __welcome__ template is inserted --%>
                    ${requestScope['sections'].toString()}
                </div>

                <div class="pf-l-stack__item locale-select">
                    <select class="pf-c-form-control" onchange="localeSelected(this)">
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
        </section>
    </main>
</body>
</html>
