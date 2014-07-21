package org.ovirt.engine.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.branding.BrandingTheme;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

@RunWith(MockitoJUnitRunner.class)
public class WelcomeServletTest {
    WelcomeServlet testServlet;

    @Mock
    HttpServletRequest mockRequest;

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
        testServlet = new WelcomeServlet();
        testServlet.setBackend(mockBackend);
        testServlet.init(mockBrandingManager);
        mockBackendQuery(VdcQueryType.GetConfigurationValue, "oVirtVersion");
        when(mockBrandingManager.getBrandingThemes()).thenReturn(new ArrayList<BrandingTheme>());
        when(mockBrandingManager.getWelcomeSections(any(Locale.class))).thenReturn("Welcome Section HTML");
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseNoDispatcher() throws IOException, ServletException {
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseWithDispatcher() throws IOException, ServletException {
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        when(mockRequest.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp")).thenReturn(mockDispatcher);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
        //Make sure the include is called on the dispatcher.
        verify(mockDispatcher).include(mockRequest, mockResponse);
    }

    private List<String> createLocaleKeys() {
        List<String> keys = new ArrayList<String>();
        keys.add("de_DE");
        keys.add("en_US");
        keys.add("es_ES");
        keys.add("fr_FR");
        keys.add("ja_JP");
        keys.add("ko_KR");
        keys.add("pt_BR");
        keys.add("zh_CN");
        return keys;
    }
}
