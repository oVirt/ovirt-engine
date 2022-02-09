<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ tag import="org.ovirt.engine.core.sso.api.SsoConstants" %>
<%@ tag import="org.ovirt.engine.core.sso.api.SsoContext" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="var"    required="true" rtexprvalue="false" %>
<%@ attribute name="locale" required="true" rtexprvalue="false" %>

<%@ variable alias="var_"    name-from-attribute="var"    scope="AT_BEGIN" variable-class="org.ovirt.engine.core.sso.api.SsoContext" %>
<%@ variable alias="locale_" name-from-attribute="locale" scope="AT_BEGIN" variable-class="java.util.Locale" %>

<c:set var="var_"    value="${applicationScope['ovirt-ssoContext']}" />
<c:set var="locale_" value="${pageContext.request.getAttribute(SsoConstants.LOCALE)}" />
