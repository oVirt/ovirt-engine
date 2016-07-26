<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="errorpage" />
<!DOCTYPE html>
<html class="obrand_background">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="product" /> <fmt:message key="errorpage.title" bundle="${errorpage}" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body>
    <a href='${applicationScope["engine_url"]}' class="obrand_loginPageLogoImageLink">
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

                <div class="col-sm-12 welcome-title-wrapper">
                    <c:choose>
                        <c:when test="${sessionScope.error != null}">
                            <span class="welcome-title">
                                <c:out value="${sessionScope.error}" />
                            </span>
                            <c:remove var="error_code" scope="session"/>
                            <c:remove var="error" scope="session"/>
                        </c:when>
                        <c:otherwise>
                            <span class="welcome-title"><c:out value="${param.msg}" /></span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:set var="reauthenticate" value="0" scope="session"/>
                <div class="col-sm-12">
                    <a id="link-not-installed" href="${pageContext.request.contextPath}/"><fmt:message key="errorpage.link" bundle="${errorpage}" /></a>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
