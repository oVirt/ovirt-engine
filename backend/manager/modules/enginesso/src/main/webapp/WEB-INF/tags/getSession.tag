<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ tag import="org.ovirt.engine.core.sso.api.SsoConstants" %>
<%@ tag import="org.ovirt.engine.core.sso.api.SsoSession" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="var" required="true" rtexprvalue="false" %>
<%@ variable alias="var_" name-from-attribute="var" scope="AT_BEGIN" variable-class="org.ovirt.engine.core.sso.api.SsoSession" %>

<c:set var="var_" value="${sessionScope['ovirt-ssoSession']}" />
