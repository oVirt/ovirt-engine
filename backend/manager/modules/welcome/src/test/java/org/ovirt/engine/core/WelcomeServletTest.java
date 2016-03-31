package org.ovirt.engine.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

@RunWith(MockitoJUnitRunner.class)
public class WelcomeServletTest {
    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(mockConfig(ConfigValues.UnsupportedLocalesFilterOverrides, new ArrayList<>()));

    WelcomeServlet testServlet;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpSession mockSession;

    @Mock
    ServletContext mockContext;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    RequestDispatcher mockDispatcher;

    @Mock
    BackendLocal mockBackend;

    @Mock
    BrandingManager mockBrandingManager;

    final List<String> localeKeys = createLocaleKeys();

    private void mockBackendQuery(VdcQueryType queryType, Object returnValue) {
        VdcQueryReturnValue queryReturnValueMock = when(mock(VdcQueryReturnValue.class).getReturnValue())
                .thenReturn(returnValue).getMock();
        when(mockBackend.runPublicQuery(eq(queryType), any(VdcQueryParametersBase.class)))
                .thenReturn(queryReturnValueMock);
    }

    @Before
    public void setUp() throws Exception {
        testServlet = new WelcomeServlet() {

            private static final long serialVersionUID = 1446616158991683162L;
            @Override
            public String getCurrentSsoSessionUser(HttpServletRequest request, Map<String, Object> userInfoMap) {
                return "admin@internal";
            }

            public boolean getChangePasswordEnabled(Map<String, Object> userInfoMap) {
                return true;
            }

            @Override
            public boolean isSessionValid(HttpServletRequest request, String token) {
                return true;
            }

            @Override
            public Map<String, Object> isSsoWebappDeployed() {
                return Collections.emptyMap();
            }
        };
        testServlet.setBackend(mockBackend);
        testServlet.init(mockBrandingManager, "/ovirt-engine");
        mockBackendQuery(VdcQueryType.GetConfigurationValue, "oVirtVersion");
        when(mockBrandingManager.getBrandingThemes()).thenReturn(new ArrayList<>());
        when(mockBrandingManager.getWelcomeSections(any(Locale.class))).thenReturn("Welcome Section HTML");
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseNoDispatcher() throws IOException, ServletException {
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        when(mockRequest.getParameterMap()).thenReturn(new HashMap<>());
        when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ovirt-engine/"));
        when(mockRequest.getServletContext()).thenReturn(mockContext);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockSession.getAttribute("authCode")).thenReturn("aU1KZG1OUytQSktnd29SQ3NIOVhWckls");
        when(mockSession.getAttribute("token")).thenReturn("aU1KZG1OUytQSktnd29SQ3NIOVhWckls");
        when(mockSession.getAttribute("error")).thenReturn("");
        when(mockSession.getAttribute("error_code")).thenReturn("");
        when(mockRequest.getServletContext().getAttribute("sso_logout_url")).thenReturn(
                new StringBuffer("http://localhost:8080/ovirt-engine/logout"));
        when(mockRequest.getServletContext().getAttribute("sso_switch_user_url")).thenReturn(
                new StringBuffer("http://localhost:8080/ovirt-engine/login"));
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseWithDispatcher() throws IOException, ServletException {
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        when(mockRequest.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp")).thenReturn(mockDispatcher);
        when(mockRequest.getParameterMap()).thenReturn(new HashMap<>());
        when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ovirt-engine/"));
        when(mockRequest.getServletContext()).thenReturn(mockContext);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockSession.getAttribute("authCode")).thenReturn("aU1KZG1OUytQSktnd29SQ3NIOVhWckls");
        when(mockSession.getAttribute("token")).thenReturn("aU1KZG1OUytQSktnd29SQ3NIOVhWckls");
        when(mockSession.getAttribute("error")).thenReturn("");
        when(mockSession.getAttribute("error_code")).thenReturn("");
        when(mockRequest.getServletContext().getAttribute("sso_logout_url")).thenReturn(
                new StringBuffer("http://localhost:8080/ovirt-engine/logout"));
        when(mockRequest.getServletContext().getAttribute("sso_switch_user_url")).thenReturn(
                new StringBuffer("http://localhost:8080/ovirt-engine/login"));
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
        //Make sure the include is called on the dispatcher.
        verify(mockDispatcher).include(mockRequest, mockResponse);
    }

    private List<String> createLocaleKeys() {
        List<String> keys = new ArrayList<>();
        keys.add("de_DE");
        keys.add("en_US");
        keys.add("es_ES");
        keys.add("fr_FR");
        keys.add("it_IT");
        keys.add("ja_JP");
        keys.add("ko_KR");
        keys.add("pt_BR");
        keys.add("zh_CN");
        return keys;
    }
}
