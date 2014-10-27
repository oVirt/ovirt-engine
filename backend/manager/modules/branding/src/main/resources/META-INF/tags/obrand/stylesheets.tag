<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${requestScope['brandingStyle'] != null}">
    <c:forEach items="${requestScope['brandingStyle']}" var="theme">
        <c:if test="${initParam['obrandApplicationName'] != null && theme.getThemeStylesheets(initParam['obrandApplicationName']) != null}">
            <c:forEach items="${theme.getThemeStylesheets(initParam['obrandApplicationName'])}" var="css">
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}${initParam['obrandThemePath']}${theme.path}/${css}">
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
