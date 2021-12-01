<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="context" required="true" type="org.ovirt.engine.core.sso.api.SsoContext" %>
<%@ attribute name="locale"  required="true" type="java.util.Locale" %>
<%@ attribute name="key"     required="true" %>

<c:out value="${context.getLocalizationUtils().localize(key, locale)}" />
