<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="reportsnotinstalled" />
<!DOCTYPE html>
<html class="obrand_background">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="reportsnotinstalled.reports_not_installed" bundle="${reportsnotinstalled}" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body>
    <a href="<obrand:messages key="obrand.common.vendor_url"/>" class="obrand_loginPageLogoImageLink">
         <span class="obrand_loginPageLogoImage"></span>
    </a>
    <div class="ovirt-container">
        <div class="container">
            <div class="row">

                <div class="col-sm-12">
                    <div id="brand">
                        <div class="obrand_loginFormLogoImage"></div>
                    </div>
                </div>

                <div class="col-sm-12 welcome-title-wrapper">
                    <span class="welcome-title"><fmt:message key="reportsnotinstalled.reports_not_installed" bundle="${reportsnotinstalled}" /></span>
                </div>

                <div class="col-sm-12">
                    <a id="link-not-installed" href="${pageContext.request.contextPath}/"><fmt:message key="reportsnotinstalled.link" bundle="${reportsnotinstalled}" /></a>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
