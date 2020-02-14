<%@ tag
    language="java"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    body-content="empty"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="themes" value="${requestScope['brandingStyle']}" />
<c:set var="applicationName" value="${initParam['applicationName']}" />
<c:set var="obrandPrefix" value="${pageContext.request.contextPath}${initParam['obrandThemePath']}" />

<%--

  Link to all of the style sheets for the current `applicationName` defined by each
  installed brand's `branding.properties` key `{applicationName}_css`.

--%>
<c:if test="${not empty themes and not empty applicationName}">
    <c:forEach var="theme" items="${themes}">
          <c:forEach  var="css" items="${theme.getThemeStylesheets(applicationName)}">
              <link rel="stylesheet" type="text/css" href="${obrandPrefix}${theme.path}/${css}">
          </c:forEach>
    </c:forEach>
</c:if>
