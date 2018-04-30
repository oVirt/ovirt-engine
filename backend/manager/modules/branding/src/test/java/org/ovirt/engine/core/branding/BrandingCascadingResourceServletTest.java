package org.ovirt.engine.core.branding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BrandingCascadingResourceServletTest {

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    BrandingManager mockBrandingManager;

    @Mock
    ServletOutputStream mockResponseOutputStream;

    @Mock
    CascadingResource mockCascadingResource;

    BrandingCascadingResourceServlet testServlet;

    @BeforeEach
    public void setUp() throws Exception {
        testServlet = new BrandingCascadingResourceServlet();
        testServlet.init(mockBrandingManager);
        // simulate a request for "/theme-resource/favicon"
        when(mockRequest.getPathInfo()).thenReturn("favicon"); //$NON-NLS-1$
        when(mockResponse.getOutputStream()).thenReturn(mockResponseOutputStream);
    }

    /**
     * Test that serving works when the request is "/favicon".
     */
    @Test
    public void testDoGetServeFavicon() throws IOException, URISyntaxException, ServletException {
        when(mockBrandingManager.getCascadingResource("favicon")).thenReturn(mockCascadingResource); //$NON-NLS-1$
        when(mockCascadingResource.getFile()).thenReturn(
                new File(this.getClass().getClassLoader().
                        getResource("./org/ovirt/engine/core/branding/02-test2.brand/images/favicon.ico") //$NON-NLS-1$
                        .toURI().getPath()));
        when(mockCascadingResource.getContentType()).thenReturn("madeUp/ContentType"); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).setHeader(eq("ETag"), any()); //$NON-NLS-1$
        verify(mockResponse).setContentType("madeUp/ContentType"); //$NON-NLS-1$
    }

    /**
     * Test that a 404 is served when no resources are available.
     */
    @Test
    public void testDoGetServeFaviconNotFound() throws IOException, ServletException {
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

}
