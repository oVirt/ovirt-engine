<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="loginpage" />
<!DOCTYPE html>
<html class="obrand_background">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="product" /> <fmt:message key="loginpage.title" bundle="${loginpage}" /></title>
    <obrand:stylesheets />
</head>
<body>
    <a href="${applicationScope['ovirt-ssoContext'].engineUrl}" class="obrand_loginPageLogoImageLink">
         <span class="obrand_loginPageLogoImage"></span>
    </a>
    <div class="login-pf">
        <div class="container">
            <div class="row">
                <div class="col-sm-12">
                    <div id="brand">
                        <div class="obrand_loginFormLogoImage"></div>
                    </div>
                </div>
                <c:set var="ssoSession" value="${sessionScope['ovirt-ssoSession']}" />

                <c:if test="${ssoSession.status == 'authenticated'}">
                    <c:redirect url="/interactive-login" />
                </c:if>

                <c:if test="${ssoSession.clientId == null}">
                    <c:redirect url="${applicationScope['ovirt-ssoContext'].engineUrl}" />
                </c:if>

                <c:if test="${ssoSession.reauthenticate == true}">
                    <c:redirect url="/oauth/authorize?client_id=${ssoSession.clientId}&response_type=code&scope=${ssoSession.scope}&state=${ssoSession.state}&redirect_uri=${ssoSession.redirectUri}?" />
                </c:if>
                <c:set target="${ssoSession}" property="reauthenticate" value="true" />

                <div style="height:45px;vertical-align:top;display: table;">
                    <c:if test="${ssoSession.loginMessage != null && ssoSession.loginMessage != '' }">
                        <span style="vertical-align:top;display:table-cell;">
                            <span class="pficon-layered">
                                <span class="pficon pficon-warning-triangle"></span>
                                <span class="pficon pficon-warning-exclamation"></span>
                            </span>
                            ${ssoSession.loginMessage}
                        </span>
                        <c:set target="${ssoSession}" property="loginMessage" value="" />
                    </c:if>
                </div>
                <div style="vertical-align:top;display: table;">
                    <span style="vertical-align:top;display:table-cell;width:630px;">
                        <form class="form-horizontal" method="post" action="${pageContext.request.contextPath}/interactive-login">
                            <div class="form-group">
                                <label class="col-sm-2 col-md-2 control-label" for="username">
                                    <fmt:message key="loginpage.username" bundle="${loginpage}" />
                                </label>
                                <div class="col-sm-10 col-md-10">
                                    <input type="text" id="username" name="username" class="form-control" autofocus tabIndex="1">
                                 </div>
                            </div>
                            <div class="form-group">
                                 <label class="col-md-2 control-label" for="password">
                                     <fmt:message key="loginpage.password" bundle="${loginpage}" />
                                 </label>
                                 <div class="col-sm-10">
                                     <input type="password" class="form-control" id="password" name="password" tabIndex="2">
                                 </div>
                             </div>
                             <div class="form-group">
                                 <label class="col-md-2 control-label" for="profile">
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
                    </span>
                    <span style="vertical-align:top;display:table-cell;">
                        <c:if test="${applicationScope.msg_of_the_day != null && sessionScope.msg_of_the_day != '' }">
                            <div style="margin-left:20px;">
                                <strong>Message of the Day</strong>
                            </div>
                            <div style="margin-left:20px;">
                                ${applicationScope.msg_of_the_day}
                            </div>
                        </c:if>
                    </span>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
