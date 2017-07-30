<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="docs" />
<!DOCTYPE html>
<html class="login-pf">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="docs.missing.title" bundle="${docs}">
        <fmt:param value="${requestScope['locale'].getDisplayLanguage(requestScope['locale'])}" />
        </fmt:message>
    </title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body>
    <div class="landing-bg-bottom-left"></div>
    <div class="landing-bg-top-right"></div>

    <a href="<obrand:messages key="obrand.common.vendor_url"/>" class="obrand_loginPageLogoImageLink">
         <span class="obrand_loginPageLogoImage"></span>
    </a>

    <div class="ovirt-container">
        <div class="container container-pad">
            <div class="row" id="welcome-section">
                <div class="col-sm-12">
                    <div class="obrand_loginFormLogoImage"></div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <fmt:message key="docs.missing.appears" bundle="${docs}">
                        <fmt:param value="${requestScope['locale'].getDisplayLanguage(requestScope['locale'])}" />
                    </fmt:message>
                    <fmt:message key="docs.missing.admin" bundle="${docs}" />
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <a href="${requestScope['englishHref']}"><fmt:message key="docs.missing.click_here" bundle="${docs}" /></a>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <fmt:message key="docs.missing.appear_once" bundle="${docs}" />
                </div>
            </div>
        </div>
    </div>
</body>
</html>
