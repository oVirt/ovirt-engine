<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Missing language: <c:out value="${requestScope['locale'].getDisplayLanguage()}" /></title>
	<link href="/docs/common/en-US/css/menu.css" type="text/css" rel="stylesheet">
</head>
<body>
	<p id="title">
		<a href="http://www.redhat.com" class="left">
			<img src="/docs/RedHat/en-US/images/image_left.png" alt="Product Site">
		</a>
		<a href="http://docs.redhat.com" class="right">
			<img src="/docs/RedHat/en-US/images/image_right.png" alt="Documentation Site">
		</a>
	</p>
	<div class="book">
		<div class="titlepage">
			<div>
				<p>It appears that you do not have the <c:out value="${requestScope['locale'].getDisplayLanguage()}" /> language pack installed.
				Please have your administrator install the proper language.</p>
				<p>
				Please click <a href="${requestScope['englishHref']}">here</a> for the English documentation. This message will
				only be displayed once per session.
				</p>
			</div>
		</div>
	</div>
</body>
</html>