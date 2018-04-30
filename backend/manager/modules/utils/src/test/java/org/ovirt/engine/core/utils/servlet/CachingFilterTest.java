package org.ovirt.engine.core.utils.servlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CachingFilterTest {
    private static final String GMT = "GMT"; //$NON-NLS-1$

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockChain;

    @Mock
    private FilterConfig mockConfig;

    @Captor
    private ArgumentCaptor<HttpServletResponse> responseCaptor;

    private CachingFilter testFilter;

    @BeforeEach
    public void setUp() throws Exception {
        testFilter = new CachingFilter();
        when(mockConfig.getInitParameter(CachingFilter.CACHE_INIT_PARAM)).thenReturn(".*\\.cache\\..*|.*\\/theme(-resource)?\\/.*|.*\\.(css|gif|png|favicon|js|ttf|woff|woff2)(\\?.*)?"); //$NON-NLS-1$
        when(mockConfig.getInitParameter(CachingFilter.NO_CACHE_INIT_PARAM)).thenReturn(".*WebAdmin\\.html|.*\\.nocache\\..*|.*\\/plugin\\/.*"); //$NON-NLS-1$
        when(mockConfig.getInitParameter(CachingFilter.NO_STORE_INIT_PARAM)).thenReturn(".*GenericApiGWTService"); //$NON-NLS-1$
        testFilter.init(mockConfig);
    }

    @Test
    public void testDoFilter_NoMatch() throws IOException, ServletException {
        when(mockRequest.getRequestURI()).thenReturn(""); //$NON-NLS-1$
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).setHeader(eq(CachingFilter.EXPIRES_HEADER), any());
        verify(mockResponse, never()).setHeader(eq(CachingFilter.CACHE_CONTROL_HEADER), any());
        verify(mockResponse, never()).setHeader(eq(CachingFilter.PRAGMA_HEADER), any());
        verify(mockChain).doFilter(eq(mockRequest), responseCaptor.capture());
        assertFalse(responseCaptor.getValue() instanceof HttpServletResponseWrapper);
    }

    @Test
    public void testDoFilter_CacheMatch() throws IOException, ServletException {
        when(mockRequest.getRequestURI()).thenReturn("something.cache.js"); //$NON-NLS-1$
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockResponse).setHeader(eq(CachingFilter.EXPIRES_HEADER), any());
        verify(mockResponse).setHeader(eq(CachingFilter.CACHE_CONTROL_HEADER), eq(CachingFilter.CACHE_YEAR));
        verify(mockResponse).setHeader(eq(CachingFilter.PRAGMA_HEADER), eq(CachingFilter.EMPTY_STRING));
        verify(mockChain).doFilter(eq(mockRequest), responseCaptor.capture());
        assertTrue(responseCaptor.getValue() instanceof HttpServletResponseWrapper);
        HttpServletResponse responseWrapper = responseCaptor.getValue();
        responseWrapper.setHeader(CachingFilter.ETAG_HEADER, "test"); //$NON-NLS-1$
        responseWrapper.setHeader(CachingFilter.LAST_MODIFIED_HEADER, "test"); //$NON-NLS-1$
        verify(mockResponse).setHeader(eq(CachingFilter.ETAG_HEADER), any());
        verify(mockResponse).setHeader(eq(CachingFilter.LAST_MODIFIED_HEADER), any());
    }

    @Test
    public void testDoFilter_NoCacheMatch() throws IOException, ServletException {
        when(mockRequest.getRequestURI()).thenReturn("something.nocache.js"); //$NON-NLS-1$
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockResponse).setHeader(eq(CachingFilter.EXPIRES_HEADER), any());
        verify(mockResponse).setHeader(eq(CachingFilter.CACHE_CONTROL_HEADER), eq(CachingFilter.NO_CACHE));
        verify(mockResponse).setHeader(eq(CachingFilter.PRAGMA_HEADER), eq(CachingFilter.NO_CACHE));
        verify(mockChain).doFilter(eq(mockRequest), responseCaptor.capture());
        assertTrue(responseCaptor.getValue() instanceof HttpServletResponseWrapper);
        HttpServletResponse responseWrapper = responseCaptor.getValue();
        responseWrapper.setHeader(CachingFilter.ETAG_HEADER, "test"); //$NON-NLS-1$
        responseWrapper.setHeader(CachingFilter.LAST_MODIFIED_HEADER, "test"); //$NON-NLS-1$
        verify(mockResponse).setHeader(eq(CachingFilter.ETAG_HEADER), any());
        verify(mockResponse).setHeader(eq(CachingFilter.LAST_MODIFIED_HEADER), any());
    }

    @Test
    public void testDoFilter_NoStoreMatch() throws IOException, ServletException {
        when(mockRequest.getRequestURI()).thenReturn("GenericApiGWTService"); //$NON-NLS-1$
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockResponse).setHeader(eq(CachingFilter.EXPIRES_HEADER), any());
        verify(mockResponse).setHeader(eq(CachingFilter.CACHE_CONTROL_HEADER), eq(CachingFilter.NO_STORE));
        verify(mockResponse).setHeader(eq(CachingFilter.PRAGMA_HEADER), eq(CachingFilter.NO_CACHE));
        verify(mockChain).doFilter(eq(mockRequest), responseCaptor.capture());
        assertTrue(responseCaptor.getValue() instanceof HttpServletResponseWrapper);
        HttpServletResponse responseWrapper = responseCaptor.getValue();
        responseWrapper.setHeader(CachingFilter.ETAG_HEADER, "test"); //$NON-NLS-1$
        responseWrapper.setHeader(CachingFilter.LAST_MODIFIED_HEADER, "test"); //$NON-NLS-1$
        verify(mockResponse, never()).setHeader(eq(CachingFilter.ETAG_HEADER), any());
        verify(mockResponse, never()).setHeader(eq(CachingFilter.LAST_MODIFIED_HEADER), any());

    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch() {
        when(mockRequest.getRequestURI()).thenReturn("something.cache.js"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch2() {
        when(mockRequest.getRequestURI()).thenReturn("something.js"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch3() {
        when(mockRequest.getRequestURI()).thenReturn("/whatever/something.css"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch4() {
        when(mockRequest.getRequestURI()).thenReturn("myfont.woff2"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch5() {
        when(mockRequest.getRequestURI()).thenReturn("/something/in/a/theme/hello.html"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch6() {
        when(mockRequest.getRequestURI()).thenReturn("/something/in/a/theme-resource/hello.html"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testCacheFilterPatternMatches_PositiveMatch7() {
        when(mockRequest.getRequestURI()).thenReturn("myfile.css?v2"); //$NON-NLS-1$
        assertTrue(testFilter.cacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testNoCacheFilterPatternMatches_PositiveMatch() {
        when(mockRequest.getRequestURI()).thenReturn("something.nocache.js"); //$NON-NLS-1$
        assertTrue(testFilter.noCacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testNoCacheFilterPatternMatches_NegativeMatch() {
        when(mockRequest.getRequestURI()).thenReturn("something.cache.js"); //$NON-NLS-1$
        assertFalse(testFilter.noCacheFilterPatternMatches(mockRequest));
    }

    @Test
    public void testNoStoreFilterPatternMatches_PositiveMatch() {
        when(mockRequest.getRequestURI()).thenReturn("GenericApiGWTService"); //$NON-NLS-1$
        assertTrue(testFilter.noStoreFilterPatternMatches(mockRequest));
    }

    @Test
    public void testNoStoreFilterPatternMatches_NegativeMatch() {
        when(mockRequest.getRequestURI()).thenReturn("OtherService"); //$NON-NLS-1$
        assertFalse(testFilter.noStoreFilterPatternMatches(mockRequest));
    }

    @Test
    public void testGetNowPlusYearHttpDate() {
        Calendar calendar = spy(Calendar.getInstance(TimeZone.getTimeZone(GMT), Locale.US));
        calendar.clear();
        calendar.set(2000, 0, 1, 15, 0);
        String result = testFilter.getNowPlusYearHttpDate(calendar);
        assertNotNull(result);
        //We know this should be 01 Jan 2001 15:00:00 as the code generates
        //dates in GMT
        assertTrue(result.contains("01 Jan 2001 15:00:00")); //$NON-NLS-1$
        verify(calendar).add(Calendar.YEAR, 1);
    }

    @Test
    public void testGetYesterdayHttpDate() {
        Calendar calendar = spy(Calendar.getInstance(TimeZone.getTimeZone(GMT), Locale.US));
        calendar.clear();
        calendar.set(2000, 0, 5, 15, 0);
        String result = testFilter.getYesterdayHttpDate(calendar);
        assertNotNull(result);
        //We know this should be 04 Jan 2001 15:00:00 as the code generates
        //dates in GMT
        assertTrue(result.contains("04 Jan 2000 15:00:00")); //$NON-NLS-1$
        verify(calendar).add(Calendar.DAY_OF_MONTH, -1);
    }

}
