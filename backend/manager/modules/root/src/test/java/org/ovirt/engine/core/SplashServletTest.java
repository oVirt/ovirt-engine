package org.ovirt.engine.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SplashServletTest {
    SplashServlet testServlet;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    RequestDispatcher mockDispatcher;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    final List<String> localeKeys = createLocaleKeys();

    @Before
    public void setUp() throws Exception {
        testServlet = new SplashServlet();
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseNoDispatcher() throws IOException, ServletException {
        Cookie responseCookie = new Cookie(LocaleFilter.LOCALE, Locale.JAPANESE.toString());
        responseCookie.setSecure(false); //Doesn't have to be secure.
        responseCookie.setMaxAge(Integer.MAX_VALUE); //Doesn't expire.
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        verify(mockResponse).addCookie(cookieCaptor.capture());
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
        assertEquals("Cookie names should match", cookieCaptor.getValue().getName(), responseCookie.getName());
        assertEquals("Cookie values should match", cookieCaptor.getValue().getValue(), responseCookie.getValue());
    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponseWithDispatcher() throws IOException, ServletException {
        Cookie responseCookie = new Cookie(LocaleFilter.LOCALE, Locale.JAPANESE.toString());
        responseCookie.setSecure(false); //Doesn't have to be secure.
        responseCookie.setMaxAge(Integer.MAX_VALUE); //Doesn't expire.
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.JAPANESE);
        when(mockRequest.getRequestDispatcher("/WEB-INF/index.jsp")).thenReturn(mockDispatcher);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute("localeKeys", localeKeys);
        verify(mockResponse).addCookie(cookieCaptor.capture());
        //Make sure the content type contains UTF-8 so the characters display properly.
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
        //Make sure the include is called on the dispatcher.
        verify(mockDispatcher).include(mockRequest, mockResponse);
        assertEquals("Cookie names should match", cookieCaptor.getValue().getName(), responseCookie.getName());
        assertEquals("Cookie values should match", cookieCaptor.getValue().getValue(), responseCookie.getValue());
    }

    private List<String> createLocaleKeys() {
        List<String> keys = new ArrayList<String>();
        keys.add("de");
        keys.add("en_US");
        keys.add("es");
        keys.add("fr");
        keys.add("ja");
        keys.add("pt_BR");
        keys.add("zh_CN");
        return keys;
    }
}
