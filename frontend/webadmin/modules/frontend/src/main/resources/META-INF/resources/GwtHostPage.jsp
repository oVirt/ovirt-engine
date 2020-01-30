<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="obrand" uri="obrand" %>
<!DOCTYPE html>
<html class="layout-pf layout-pf-fixed transitions">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
        <c:if test="${requestScope['baseContextPath'] != null}">
            var baseContextPath = <c:out value="${requestScope['baseContextPath']}" escapeXml="false"/>;
        </c:if>
        <c:if test="${requestScope['DISPLAY_UNCAUGHT_UI_EXCEPTIONS'] != null}">
            var displayUncaughtUIExceptions = <c:out value="${requestScope['DISPLAY_UNCAUGHT_UI_EXCEPTIONS']}" escapeXml="false"/>;
        </c:if>
    </script>
</head>
<body>
    <!-- launch GWT. This takes a while -->
    <script type="text/javascript" src="${requestScope['selectorScript']}"></script>

    <div id="host-page-placeholder">
        <!-- while GWT is launching... skeleton the layout -->
        <style type="text/css">
            .container-pf-nav-pf-vertical {
                height: calc(100vh - var(--ovirt-masthead-height));
                display: flex;
                flex-direction: column;
                justify-content: center;
            }
            #host-page-placeholder-spinner {
                display: flex;
                justify-content: center;
            }
            #host-page-placeholder-spinner .spinner {
                margin: 0;
            }
            #host-page-placeholder-text {
                font-size: 18px;
                margin-left: 20px;
            }
        </style>
        <nav class="navbar navbar-pf-vertical obrand_masthead" role="navigation">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a href="javascript:;" class="navbar-brand obrand_headerLogoLink">
                    <img class="obrand_mastheadLogo" src="clear.cache.gif">
                </a>
            </div>
            <div class="collapse navbar-collapse">
                <ul class="nav navbar-nav navbar-right navbar-iconic">
                    <li class="nav-item-iconic-skeleton"></li>
                </ul>
            </div>
        </nav>

        <div class="container-pf-nav-pf-vertical">
            <div id="host-page-placeholder-spinner">
                <div class="spinner spinner-lg"></div>
                <div id="host-page-placeholder-text">Loading ...</div>
            </div>
        </div>

        <div>
            <div class="nav-pf-vertical nav-pf-vertical-with-sub-menus">
                <ul class="list-group"></ul>
            </div>
        </div>
    </div>
</body>
</html>
