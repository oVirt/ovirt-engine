<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="methodnotallowed" />
<!DOCTYPE html>
<html class="login-pf">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title><fmt:message key="methodnotallowed.method_not_allowed" bundle="${methodnotallowed}" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body>
    <div class="obrand_landingBgTop"></div>
    <div class="obrand_landingBgBottom"></div>

    <a href="<obrand:messages key="obrand.common.vendor_url"/>" class="obrand_loginPageLogoLink">
        <span class="obrand_loginPageLogo"></span>
    </a>

    <div class="ovirt-container">
        <div class="container container-pad">
            <div class="row" id="welcome-section">
                <div class="col-sm-12">
                    <div class="obrand_middleLogoName"></div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <span class="welcome-title"><fmt:message key="methodnotallowed.method_not_allowed" bundle="${methodnotallowed}" /></span>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <a href="${pageContext.request.contextPath}/"><fmt:message key="methodnotallowed.link" bundle="${methodnotallowed}" /></a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
