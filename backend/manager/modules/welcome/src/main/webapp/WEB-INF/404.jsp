<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="welcome-messages" var="pagenotfound" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title><fmt:message key="pagenotfound.page_not_found" bundle="${pagenotfound}" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body class="pf-c-page ovirt-container">
    <obrand:background-image />

    <header role="banner" class="pf-c-page__header obrand_ssoHeader"><!-- spacer / place holder --></header>
    <main class="pf-c-page__main">
        <section class="pf-c-page__main-section error-section">
            <div class="pf-l-split">
                <div class="pf-l-split__item">
                    <a href="${pageContext.request.contextPath}/" class="obrand_welcomePageLogoLink">
                        <span class="obrand_welcomePageLogo"></span>
                    </a>
                </div>
                <%--
                  NOTE: WelcomeServlet fetches the version info for ovirt-engine.jsp and
                        it is not available here in a pure JSP servlet.
                --%>
            </div>

            <div class="pf-l-stack obrand_errorPageSection">
                <div class="pf-l-stack__item obrand_errorPageSectionTitle">
                    <fmt:message key="pagenotfound.page_not_found" bundle="${pagenotfound}" />
                </div>
                <div class="pf-l-stack__item obrand_errorPageSectionAction">
                    <a href="${pageContext.request.contextPath}/"><fmt:message key="pagenotfound.link" bundle="${pagenotfound}" /></a>
                </div>
            </div>
        </section>
    </main>
</body>
</html>
