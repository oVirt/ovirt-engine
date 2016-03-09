<%@ page pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setLocale value="${locale}" />
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="product" /> Change Password</title>
    <obrand:stylesheets />
</head>
<body>
<div class="obrand_loginPageBackground">
        <a href="${applicationScope['ovirt-ssoContext'].engineUrl}" class="obrand_loginPageLogoImageLink">
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

                    <div style="height:45px;vertical-align:top;display: table;">
                        <c:choose>
                            <c:when test="${ssoSession.changePasswdMessage != null && ssoSession.changePasswdMessage != '' }">
                                <span style="vertical-align:top;display:table-cell;">
                                    <span class="pficon-layered">
                                        <span class="pficon pficon-warning-triangle"></span>
                                        <span class="pficon pficon-warning-exclamation"></span>
                                    </span>
                                    ${ssoSession.changePasswdMessage}
                                </span>
                                <c:set target="${ssoSession}" property="changePasswdMessage" value="" />
                            </c:when>
                            <c:otherwise>
                                <span style="vertical-align:top;display:table-cell;">
                                Change password for user <b> ${ssoUserName}@${ssoUserProfile} </b>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>



                    <div style="vertical-align:top;display: table;">
                        <span style="vertical-align:top;display:table-cell;width:540px;">
                            <form class="form-horizontal" method="post" action="${pageContext.request.contextPath}/interactive-change-passwd" enctype="application/x-www-form-urlencoded">
                                <input type="hidden" class="form-control" id="username" placeholder="username" name="username" value="${ssoUserName}">
                                <input type="hidden" class="form-control" id="profile" placeholder="profile" name="profile" value="${ssoUserProfile}">
                                <div class="form-group">
                                    <label class="col-md-3 control-label" for="oldPassword">Old Password</label>
                                    <div class="col-sm-9">
                                        <input type="password" class="form-control" id="credentials" placeholder="PASSWORD" name="credentials" tabIndex="1">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-md-3 control-label" for="newPassword1">New Password</label>
                                    <div class="col-sm-9">
                                        <input type="password" class="form-control" id="credentialsNew1" placeholder="NEW PASSWORD" name="credentialsNew1" tabIndex="2">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-md-3 control-label" for="newPassword2">Retype Password</label>
                                    <div class="col-sm-9">
                                        <input type="password" class="form-control" id="credentialsNew2" placeholder="NEW PASSWORD" name="credentialsNew2" tabIndex="3">
                                    </div>
                                </div>
                                <div class="pull-right">
                                    <button type="submit" class="btn btn-primary" tabIndex="4">Change Password</button>
                                </div>
                            </form>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
