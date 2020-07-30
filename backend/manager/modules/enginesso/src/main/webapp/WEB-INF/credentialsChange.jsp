<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="sso-messages" var="changepasswordpage" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title>
        <fmt:message key="product" />
        <fmt:message key="changepasswordpage.title" bundle="${changepasswordpage}" />
    </title>
    <obrand:stylesheets />
    <obrand:javascripts />
</head>
<body class="ovirt-container">
    <c:set var="ssoSession" value="${sessionScope['ovirt-ssoSession']}" />

    <c:if test="${ssoSession.clientId == null}">
        <c:redirect url="${applicationScope['ovirt-ssoContext'].engineUrl}" />
    </c:if>

    <c:choose>
        <c:when test="${ssoSession.status == 'authenticated'}">
            <c:set var="ssoUserName" value="${ssoSession.userId}" />
            <c:set var="ssoUserProfile" value="${ssoSession.profile}" />
        </c:when>
        <c:when test="${ssoSession.changePasswdCredentials != null && ssoSession.changePasswdCredentials.username != null }">
            <c:set var="ssoUserName" value="${ssoSession.changePasswdCredentials.username}" />
            <c:set var="ssoUserProfile" value="${ssoSession.changePasswdCredentials.profile}" />
        </c:when>
    </c:choose>

    <obrand:background-image />

    <div class="pf-c-login">
        <div class="pf-c-login__container">
            <header class="pf-c-login__header">
                <a href="${applicationScope['ovirt-ssoContext'].engineUrl}" class="pf-c-brand obrand_loginPageLogoLink">
                    <div class="obrand_loginPageLogo"></div>
                </a>
            </header>

            <main class="pf-c-login__main">
                <header class="pf-c-login__main-header">
                    <h1 class="pf-c-title pf-m-3xl">
                        <fmt:message key="changepasswordpage.usermessage" bundle="${changepasswordpage}" />
                        <b>${ssoUserName}@${ssoUserProfile}</b>
                    </h1>
                </header>

                <div class="pf-c-login__main-body">
                    <form
                        novalidate class="pf-c-form"
                        method="post"
                        action="${pageContext.request.contextPath}/interactive-change-passwd"
                        enctype="application/x-www-form-urlencoded"
                    >
                        <p class="pf-c-form__helper-text pf-m-error">
                            <c:if test="${ssoSession.changePasswdMessage != null && ssoSession.changePasswdMessage != ''}">
                                <i class="fas fa-exclamation-circle pf-c-form__helper-text-icon"></i>
                                <c:out value="${ssoSession.changePasswdMessage}"/>
                                <c:set target="${ssoSession}" property="changePasswdMessage" value="" />
                            </c:if>
                        </p>

                        <input type="hidden" class="form-control" id="username" placeholder="username" name="username" value="${ssoUserName}">
                        <input type="hidden" class="form-control" id="profile" placeholder="profile" name="profile" value="${ssoUserProfile}">

                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="oldPassword">
                                <fmt:message key="changepasswordpage.oldpassword" bundle="${changepasswordpage}" />
                            </label>
                            <input type="password" id="credentials" name="credentials" class="pf-c-form-control" autofocus tabIndex="1">
                        </div>
                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="username">
                                <fmt:message key="changepasswordpage.newpassword" bundle="${changepasswordpage}" />
                            </label>
                            <input type="password" id="credentialsNew1" name="credentialsNew1" class="pf-c-form-control" tabIndex="2">
                        </div>
                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="username">
                                <fmt:message key="changepasswordpage.retypepassword" bundle="${changepasswordpage}" />
                            </label>
                            <input type="password" id="credentialsNew2" name="credentialsNew2" class="pf-c-form-control" tabIndex="3">
                        </div>

                        <div class="pf-c-form__group pf-m-action">
                            <button class="pf-c-button pf-m-primary pf-m-block" type="submit" tabIndex="4">
                                <fmt:message key="changepasswordpage.changepassword" bundle="${changepasswordpage}" />
                            </button>
                        </div>
                    </form>
                </div>
            </main>

            <footer class="pf-c-login__footer">
                <p class="obrand_loginPageSubtitle"></p>
            </footer>
        </div>
    </div>
</body>
</html>
