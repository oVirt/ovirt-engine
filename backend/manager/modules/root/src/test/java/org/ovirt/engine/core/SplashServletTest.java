package org.ovirt.engine.core;

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
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

@RunWith(MockitoJUnitRunner.class)
public class SplashServletTest {
    SplashServlet testServlet;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    RequestDispatcher mockDispatcher;

    final List<String> localeKeys = createLocaleKeys();

    @Before
    public void setUp() throws Exception {
        testServlet = new SplashServlet();
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
        keys.add("pt_BR");
        keys.add("zh_CN");
        return keys;
    }
}
