package org.ovirt.engine.core.branding;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

@ExtendWith(MockitoExtension.class)
public class BrandingFilterTest {
    /**
     * The filter under test.
     */
    BrandingFilter testFilter;
    @Mock
    HttpServletRequest mockRequest;
    @Mock
    HttpServletResponse mockResponse;
    @Mock
    FilterChain mockChain;
    @Mock
    FilterConfig mockFilterConfig;
    @Mock
    BrandingManager mockBrandingManager;
    @Mock
    ServletContext mockServletContext;

    List<BrandingTheme> mockBrandingThemes;

    @BeforeEach
    public void setUp() throws Exception {
        testFilter = new BrandingFilter();
        testFilter.init(mockFilterConfig, mockBrandingManager);
        mockBrandingThemes = new ArrayList<>();
        when(mockRequest.getAttribute(LocaleFilter.LOCALE)).thenReturn(Locale.US);
        when(mockRequest.getServletContext()).thenReturn(mockServletContext);
        when(mockServletContext.getInitParameter("applicationName")).thenReturn("test");
        when(mockBrandingManager.getBrandingThemes()).thenReturn(mockBrandingThemes);
    }

    @Test
    public void testDoFilter() throws ServletException, IOException {
        testFilter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mockRequest).setAttribute(BrandingFilter.THEMES_KEY, mockBrandingThemes);
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

}
