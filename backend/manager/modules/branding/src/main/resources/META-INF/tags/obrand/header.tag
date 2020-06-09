<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="welcome-messages" var="pageheader" />

<%--

    The Header part of a PF4 Page https://www.patternfly.org/v4/documentation/core/components/page
    to be used by welcome/landing pages to show logged in status.

    "Not Logged In" ...(if sessionScope["sso_user"] is empty)
      -> Login

    "User@Profile"
      -> Log Out
      -> Switch User ...(if supported)
      -> Change Password ...(if supported)

--%>
<script type="text/javascript">
document.addEventListener('DOMContentLoaded', () => {
  const dropdown = document.querySelector('.obrand_ssoHeader .pf-c-dropdown');
  const button = document.querySelector('.obrand_ssoHeader .pf-c-dropdown__toggle');
  const menu = document.querySelector('.obrand_ssoHeader .pf-c-dropdown__menu');
  const rootClose = document.querySelector('.obrand_ssoHeader_rootClose');

  const flipExpandedCollapsed = () => {
    const isOpen = menu.getAttribute('hidden') === 'true';
    if (isOpen) {
      dropdown.className += ' pf-m-expanded';
      button.setAttribute('aria-expanded', 'false');
      menu.removeAttribute('hidden');
      rootClose.style.display = 'block';
    } else {
      dropdown.className = dropdown.className.replace(/ pf-m-expanded/, '');
      button.setAttribute('aria-expanded', 'true');
      menu.setAttribute('hidden', 'true');
      rootClose.style.removeProperty('display');
    }
  };

  button.addEventListener('click', flipExpandedCollapsed);
  rootClose.addEventListener('click', flipExpandedCollapsed);
})
</script>
<header role="banner" class="pf-c-page__header obrand_ssoHeader">
    <%--
        No branding or navigation toggles required on welcome/landing pages.
    --%>
    <div class="pf-c-page__header-tools">
        <div class="pf-c-dropdown">
            <div class="obrand_ssoHeader_rootClose"></div>
            <button class="pf-c-dropdown__toggle" type="button" id="sso-dropdown-toggle" aria-expanded="false">
                <span class="pf-c-dropdown__toggle-text">
                    <c:if test='${empty sessionScope["sso_user"]}'>
                        <fmt:message key="pageheader.notLoggedIn" bundle="${pageheader}" />
                    </c:if>
                    <c:if test='${not empty sessionScope["sso_user"]}'>
                        ${sessionScope["sso_user"]}
                    </c:if>
                </span>
                <i class="fas fa-caret-down pf-c-dropdown__toggle-icon" aria-hidden="true"></i>
            </button>
            <ul class="pf-c-dropdown__menu pf-m-align-right" aria-labelledby="sso-dropdown-toggle" hidden="true">
                <c:if test='${empty sessionScope["sso_user"]}'>
                    <li>
                        <a class="pf-c-dropdown__menu-item" href='${applicationScope["sso_login_url"]}'><fmt:message key="pageheader.login" bundle="${pageheader}" /></a>
                    </li>
                </c:if>

                <c:if test='${not empty sessionScope["sso_user"]}'>
                    <li>
                        <a class="pf-c-dropdown__menu-item" href='${applicationScope["sso_logout_url"]}'><fmt:message key="pageheader.signOut" bundle="${pageheader}" /></a>
                    </li>
                    <c:if test='${sessionScope["engine_sso_enable_external_sso"] == false}'>
                        <li>
                            <a class="pf-c-dropdown__menu-item" href='${applicationScope["sso_switch_user_url"]}'><fmt:message key="pageheader.switchUser" bundle="${pageheader}" /></a>
                        </li>
                    </c:if>
                    <c:if test='${sessionScope["capability_credentials_change"] == true}'>
                        <li>
                            <a class="pf-c-dropdown__menu-item" href='${requestScope["sso_credential_change_url"]}'><fmt:message key="pageheader.changePassword" bundle="${pageheader}" /></a>
                        </li>
                    </c:if>
                </c:if>
            </ul>
        </div>

        <img class="pf-c-avatar" alt="avatar image" src="data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==" />
    </div>
</header>
