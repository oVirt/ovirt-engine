<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="messages" var="pageheader" />

<div style="height: 41px;
            position: relative;
            top: 4px;
            background-image: -webkit-gradient(linear, left 0%, left 100%, from(#474c50), to(#383f43));
            background-image: -webkit-linear-gradient(top, #474c50 0%, #383f43 100%);
            background-image: -moz-linear-gradient(top, #474c50 0%, #383f43 100%);
            background-image: linear-gradient(to bottom, #474c50 0%, #383f43 100%);
            background-repeat: repeat-x;
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ff474c50', endColorstr='#ff383f43', GradientType=0);">
  <div class="collapse navbar-collapse navbar-collapse-1" style="position: absolute; top 0px; right: 20px;">
    <ul class="nav navbar-nav navbar-utility">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <span class="pficon pficon-user" style="font-size: 11px; cursor: pointer;" ></span>
                <c:choose>
                <c:when test='${sessionScope["sso_user"] != null}'>
                    ${sessionScope["sso_user"]}
                </c:when>
                <c:otherwise>
                     <fmt:message key="pageheader.notLoggedIn" bundle="${pageheader}" />
                </c:otherwise>
                </c:choose>
                <b class="caret"></b>
            </a>
            <ul class="dropdown-menu">
                 <c:choose>
                 <c:when test='${sessionScope["sso_user"] != null}'>
                     <li>
                         <a href='${applicationScope["sso_logout_url"]}' style="font-size: 11px; cursor: pointer;"><fmt:message key="pageheader.signOut" bundle="${pageheader}" /></a>
                     </li>
                     <li>
                         <a href='${applicationScope["sso_switch_user_url"]}' style="font-size: 11px; cursor: pointer;"><fmt:message key="pageheader.switchUser" bundle="${pageheader}" /></a>
                     </li>
                     <c:if test='${sessionScope["capability_credentials_change"] == true}'>
                         <li>
                             <a href='${applicationScope["sso_credential_change_url"]}' style="font-size: 11px; cursor: pointer;"><fmt:message key="pageheader.changePassword" bundle="${pageheader}" /></a>
                         </li>
                     </c:if>
                 </c:when>
                 <c:otherwise>
                     <li>
                         <a href='${applicationScope["sso_login_url"]}' style="font-size: 11px; cursor: pointer;"><fmt:message key="pageheader.login" bundle="${pageheader}" /></a>
                     </li>
                 </c:otherwise>
                 </c:choose>
             </ul>
        </li>
    </ul>
  </div>
</div>