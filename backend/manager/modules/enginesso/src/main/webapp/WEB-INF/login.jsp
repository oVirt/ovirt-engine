<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="loginpage" />
<!DOCTYPE html>
<html class="login-pf">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <obrand:favicon />
    <title><fmt:message key="product" /> <fmt:message key="loginpage.title" bundle="${loginpage}" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
    <script src="retain-fragment.js" type="text/javascript"></script>
</head>
<body>

    <c:set var="ssoSession" value="${sessionScope['ovirt-ssoSession']}" />

    <c:if test="${ssoSession.status == 'authenticated'}">
        <c:redirect url="/interactive-login" />
    </c:if>

    <c:if test="${ssoSession.clientId == null}">
        <c:redirect url="${ssoSession.engineUrl}" />
    </c:if>

    <c:if test="${ssoSession.reauthenticate == true}">
        <c:redirect url="/oauth/authorize?client_id=${ssoSession.clientId}&response_type=code&scope=${ssoSession.scope}&app_url=${ssoSession.appUrl}&redirect_uri=${ssoSession.redirectUri}?" />
    </c:if>

    <c:set target="${ssoSession}" property="reauthenticate" value="true" />

    <div class="obrand_landingBgTop"></div>
    <div class="obrand_landingBgBottom"></div>

    <a href="${ssoSession.engineUrl}" class="obrand_loginPageLogoLink">
        <span class="obrand_loginPageLogo"></span>
    </a>
    <div class="ovirt-container">
        <div class="container">
            <div id="welcome-section-login">
                <div class="obrand_middleLogoName"></div>
            </div>

            <div class="row">
                <div class="col-sm-7">
                    <c:if test="${ssoSession.loginMessage != null && ssoSession.loginMessage != '' }">
                        <div class="alert alert-warning alert-dismissable">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
                                <span class="pficon pficon-close"></span>
                            </button>
                            <span class="pficon pficon-warning-triangle-o"></span>
                            ${ssoSession.loginMessage}
                        </div>
                        <c:set target="${ssoSession}" property="loginMessage" value="" />
                    </c:if>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-7 col-md-6 col-lg-5 login">
                    <form class="form-horizontal" id="loginForm" method="post" action="${pageContext.request.contextPath}/interactive-login">
                        <input type="hidden" class="form-control" id="sessionIdToken" placeholder="sessionIdToken" name="sessionIdToken" value="${ssoSession.sessionIdToken}">
                        <div class="form-group">
                            <label class="col-sm-2 control-label nowrap" for="username">
                                <fmt:message key="loginpage.username" bundle="${loginpage}" />
                            </label>
                            <div class="col-sm-10">
                                <input type="text" id="username" name="username" class="form-control" autofocus tabIndex="1">
                             </div>
                        </div>
                        <div class="form-group">
                             <label class="col-sm-2 control-label nowrap" for="password">
                                 <fmt:message key="loginpage.password" bundle="${loginpage}" />
                             </label>
                             <div class="col-sm-10">
                                 <input type="password" class="form-control" id="password" name="password" tabIndex="2">
                             </div>
                         </div>
                         <div class="form-group">
                             <label class="col-sm-2 control-label nowrap" for="profile">
                                 <fmt:message key="loginpage.profile" bundle="${loginpage}" />
                             </label>
                             <div class="col-sm-10">
                                 <select class="form-control" id="profile" name="profile" tabIndex="3">
                                     <c:forEach items="${applicationScope['ovirt-ssoContext'].ssoProfilesSupportingPasswd}" var="profile" >
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
                         </div>
                         <div class="pull-right">
                             <button type="submit" class="btn btn-primary btn-lg" tabIndex="4">
                                 <fmt:message key="loginpage.login" bundle="${loginpage}" />
                             </button>
                         </div>
                    </form>
                    <c:if test="${fn:length(ssoSession.authStack) gt 0}">
                        <div class="pull-right">
                            <form class="form-horizontal" method="post" action="${pageContext.request.contextPath}/interactive-login-next-auth" enctype="application/x-www-form-urlencoded">
                                <button type="submit" class="btn btn-primary btn-lg" tabIndex="5">
                                    <fmt:message key="loginpage.nextauth" bundle="${loginpage}" />
                                </button>
                                <span>&nbsp;</span>
                            </form>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
