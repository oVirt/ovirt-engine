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

  Link to all of the javascripts for the current `applicationName` defined by each
  installed brand's `branding.properties` key `{applicationName}_js`.

--%>
<c:if test="${not empty themes and not empty applicationName}">
    <c:forEach var="theme" items="${themes}">
        <c:set var="javascripts" value="${theme.getThemeJavascripts(applicationName)}" />
        <c:if test="${not empty javascripts and !javascripts.isEmpty()}">
          <c:forEach var="js" items="${javascripts}">
              <script type="text/javascript" src="${obrandPrefix}${theme.path}/${js}"></script>
          </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
