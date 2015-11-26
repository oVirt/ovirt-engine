<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=10">
    <obrand:favicon />
    <meta name="gwt:property" content="locale=${requestScope['locale']}">
    <obrand:stylesheets />
    <obrand:javascripts />
    <script type="text/javascript">
        <c:if test="${requestScope['userInfo'] != null}">
            var userInfo = <c:out value="${requestScope['userInfo']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['applicationMode'] != null}">
            var applicationMode = <c:out value="${requestScope['applicationMode']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['pluginDefinitions'] != null}">
            var pluginDefinitions = <c:out value="${requestScope['pluginDefinitions']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['engineRpmVersion'] != null}">
            var engineRpmVersion = <c:out value="${requestScope['engineRpmVersion']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['messages'] != null}">
            var messages = <c:out value="${requestScope['messages']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['visibleLocales'] != null}">
            var visibleLocales = <c:out value="${requestScope['visibleLocales']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['baseContextPath'] != null}">
            var baseContextPath = <c:out value="${requestScope['baseContextPath']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['ENGINE_REPORTS_BASE_URL'] != null}">
            var engineReportsUrls = <c:out value="${requestScope['ENGINE_REPORTS_BASE_URL']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['DISPLAY_UNCAUGHT_UI_EXCEPTIONS'] != null}">
            var displayUncaughtUIExceptions = <c:out value="${requestScope['DISPLAY_UNCAUGHT_UI_EXCEPTIONS']}" escapeXml="false"/>;
        </c:if>
    </script>
</head>
<body>
    <script type="text/javascript" src="${requestScope['selectorScript']}"></script>
</body>
</html>
