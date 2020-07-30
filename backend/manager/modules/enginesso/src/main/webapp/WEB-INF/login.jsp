<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="sso-messages" var="loginpage" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title>
        <fmt:message key="product" />
        <fmt:message key="loginpage.title" bundle="${loginpage}" />
    </title>
    <obrand:stylesheets />
    <obrand:javascripts />
    <script src="retain-fragment.js" type="text/javascript"></script>
</head>
<body class="ovirt-container">
    <c:set var="ssoSession" value="${sessionScope['ovirt-ssoSession']}" />

    <c:if test="${ssoSession.status == 'authenticated'}">
        <c:redirect url="/interactive-login" />
    </c:if>

    <c:if test="${ssoSession.clientId == null}">
        <c:redirect url="${applicationScope['ovirt-ssoContext'].engineUrl}" />
    </c:if>

    <c:if test="${ssoSession.reauthenticate == true}">
        <c:redirect
            url="/oauth/authorize?client_id=${ssoSession.clientId}&response_type=code&scope=${ssoSession.scope}&app_url=${ssoSession.appUrl}&redirect_uri=${ssoSession.redirectUri}?" />
    </c:if>

    <c:set target="${ssoSession}" property="reauthenticate" value="true" />

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
                        <fmt:message key="loginpage.title" bundle="${loginpage}" />
                    </h1>
                </header>

                <div class="pf-c-login__main-body">
                    <form
                        novalidate class="pf-c-form" id="loginForm"
                        method="post"
                        action="${pageContext.request.contextPath}/interactive-login"
                    >
                        <p class="pf-c-form__helper-text pf-m-error">
                            <c:if test="${ssoSession.loginMessage != null && ssoSession.loginMessage != '' }">
                                <i class="fas fa-exclamation-circle pf-c-form__helper-text-icon"></i>
                                <c:out value="${ssoSession.loginMessage}"/>
                                <c:set target="${ssoSession}" property="loginMessage" value="" />
                            </c:if>
                        </p>

                        <input
                            type="hidden" class="pf-c-form-control" id="sessionIdToken"
                            placeholder="sessionIdToken"
                            name="sessionIdToken"
                            value="${ssoSession.sessionIdToken}"
                        >

                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="username">
                                <fmt:message key="loginpage.username" bundle="${loginpage}" />
                            </label>
                            <input type="text" id="username" name="username" class="pf-c-form-control" autofocus tabIndex="1">
                        </div>
                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="password">
                                <fmt:message key="loginpage.password" bundle="${loginpage}" />
                            </label>
                            <input type="password" class="pf-c-form-control" id="password" name="password" tabIndex="2">
                        </div>
                        <div class="pf-form__group">
                            <label class="pf-c-form__label-text" for="profile">
                                <fmt:message key="loginpage.profile" bundle="${loginpage}" />
                            </label>
                            <select class="pf-c-form-control" id="profile" name="profile" tabIndex="3">
                                <c:forEach
                                    items="${applicationScope['ovirt-ssoContext'].ssoProfilesSupportingPasswd}"
                                    var="profile"
                                >
                                    <c:choose>
                                        <c:when test="${cookie['profile'] != null && cookie['profile'].value == profile}">
                                            <option value="${profile}" selected>${profile}</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${profile}">${profile}</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="pf-c-form__group pf-m-action">
                            <button class="pf-c-button pf-m-primary pf-m-block" type="submit">
                                <fmt:message key="loginpage.login" bundle="${loginpage}" />
                            </button>
                        </div>
                    </form>

                    <c:if test="${fn:length(ssoSession.authStack) gt 0}">
                        <div class="pull-right">
                            <form
                                class="form-horizontal"
                                method="post"
                                action="${pageContext.request.contextPath}/interactive-login-next-auth"
                                enctype="application/x-www-form-urlencoded"
                            >
                                <button type="submit" class="btn btn-primary btn-lg" tabIndex="5">
                                    <fmt:message key="loginpage.nextauth" bundle="${loginpage}" />
                                </button>
                                <span>&nbsp;</span>
                            </form>
                        </div>
                    </c:if>
                </div>
            </main>

            <footer class="pf-c-login__footer">
                <p class="obrand_loginPageSubtitle">
                    <fmt:message key="obrand.loginpage.subtitle" />
                </p>
            </footer>
        </div>
    </div>
</body>
</html>
