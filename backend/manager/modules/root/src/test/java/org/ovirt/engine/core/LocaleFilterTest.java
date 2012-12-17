package org.ovirt.engine.core;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocaleFilterTest {
    LocaleFilter testFilter;

    @Mock
    HttpServletRequest mockRequest;
    @Mock
    HttpServletResponse mockResponse;
    @Mock
    FilterChain mockChain;

    @Before
    public void setUp() throws Exception {
        testFilter = new LocaleFilter();
    }

    @Test
    public void testDoFilterFromParameter() throws IOException, ServletException {
        Locale testLocale = Locale.ITALIAN;
        when(mockRequest.getParameter(LocaleFilter.LOCALE)).thenReturn(testLocale.toString());
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, testLocale);
    }

    @Test
    public void testDoFilterFromParameterWithCookie() throws IOException, ServletException {
        Locale testLocale = Locale.ITALIAN;
        Cookie[] cookies = createCookies(Locale.GERMAN);
        when(mockRequest.getParameter(LocaleFilter.LOCALE)).thenReturn(testLocale.toString());
        when(mockRequest.getCookies()).thenReturn(cookies);
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, testLocale);
    }

    @Test
    public void testDoFilterFromCookie() throws IOException, ServletException {
        Locale testLocale = Locale.ITALIAN;
        Cookie[] cookies = createCookies(testLocale);
        when(mockRequest.getCookies()).thenReturn(cookies);
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, testLocale);
    }

    @Test
    public void testDoFilterFromCookieNull() throws IOException, ServletException {
        when(mockRequest.getCookies()).thenReturn(null);
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        //Verify that it defaulted to the US locale
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, Locale.US);
    }

    @Test
    public void testDoFilterFromCookieDifferent() throws IOException, ServletException {
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie("name", "value");
        cookies[1] = new Cookie("name2", "value2");
        when(mockRequest.getCookies()).thenReturn(cookies);
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        //Verify that it defaulted to the US locale
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, Locale.US);
    }

    @Test
    public void testDoFilterFromRequest() throws IOException, ServletException {
        when(mockRequest.getLocale()).thenReturn(Locale.JAPANESE);
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(LocaleFilter.LOCALE, Locale.JAPANESE);
    }

    @Test
    public void testInit() throws ServletException {
        testFilter.init(null);
        //Should not throw an exception.
    }

    @Test
    public void testDestroy() {
        testFilter.destroy();
        //Should not throw an exception.
    }

    /*
     * Helper methods.
     */
    private Cookie[] createCookies(Locale... locales) {
        List<Cookie> cookieList = new ArrayList<Cookie>();
        for(Locale locale: locales) {
            cookieList.add(new Cookie(LocaleFilter.LOCALE, locale.toString()));
        }
        return cookieList.toArray(new Cookie[cookieList.size()]);
    }

}
