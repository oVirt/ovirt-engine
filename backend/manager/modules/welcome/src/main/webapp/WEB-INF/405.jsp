<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="methodnotallowed" />
<!DOCTYPE html>
<html class="login-pf">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="methodnotallowed.method_not_allowed" bundle="${methodnotallowed}" /></title>
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
