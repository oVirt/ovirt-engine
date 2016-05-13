<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<fmt:setBundle basename="languages" var="lang" />
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <obrand:favicon />
    <title><fmt:message key="obrand.welcome.title" /></title>
    <obrand:stylesheets />
    <obrand:javascripts />
    <script src="splash.js" type="text/javascript"></script>
</head>
<body onload="pageLoaded()">
    <div class="obrand_loginPageBackground">
  
	<nav class="navbar navbar-default" role="navigation">
        <div class="navbar-right download-link-container">
            <div class="download-link">
                <a id="WelcomePage_ccr" href="/eayunVirt" target="_blank">
                    <div class="media">
                        <div class="pull-left">
                            <div class="download-link-image"></div>
                        </div>
                        <div class="media-body download-text-container">
                            <h4><fmt:message key="obrand.welcome.downloads_title" /></h4>
                            <p><fmt:message key="obrand.welcome.portal_client.resource" /></p>
                        </div>	
                    </div>
                </a>
            </div>
        </div>
    </nav>
        <div class="login-pf">
            <div class="container">
                <div class="row">
                
                    <noscript>
                        <div class="well col-sm-11 well-sm" id="well-error">
                            <span class="label label-default" id="well-error-label">
                                <b><fmt:message key="obrand.welcome.browser.javascript1" /></b>
                                <fmt:message key="obrand.welcome.browser.javascript2" />
                            </span>
                        </div>
                        <div style="clear: both;"></div>
                    </noscript>

                    <div class="col-sm-12" id="sections">
                        ${requestScope['sections'].toString()}
                    </div>

                    <div style="clear: both;">

                    </div>
                </div>
			    </div>
			   </div>
			  
			  <div class="logo-modal-footer">
				     <div class="footer-container">
				          <div class="col-sm-9">
				            <div class="logo-image">
			             		 <a class="obrand_loginPageLogoImage" href="${obrand.common.vendor_url}">
			           			</a>
			              </div>
			              
			              <div class="hidden-xs product-description">
			                <p class="description">开放式虛拟化管理者</p>
			                <p class="description">open virtualization manager</p>
			              </div>
			              </div>
			            
			          <div class="col-sm-3 language-menu-container">
                        <select class="gwt-ListBox obrand_locale_list_box" onchange="localeSelected(this)" id="localeBox">
                            <c:forEach items="${requestScope['localeKeys']}" var="localeKey">
                                <c:choose>
                                <c:when test="${requestScope['locale'].toString() == localeKey}">
                                    <c:set var="selectedLocale" value="${localeKey}"/>
                                    <option value="${localeKey}" selected="selected"><fmt:message key="${localeKey}" bundle="${lang}"/></option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${localeKey}"><fmt:message key="${localeKey}" bundle="${lang}"/></option>
                                </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </select>
                      </div>
			        </div>
			      </div>
            </div>
        
    
</body>
<script type="text/javascript">
function view(){
	var sec = document.getElementById("sections");
	window.alert(sec.innerHTML);
}
</script>
</html>