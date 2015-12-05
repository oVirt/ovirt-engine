package org.ovirt.engine.core.branding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BrandingServletTest {

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    BrandingManager mockBrandingManager;

    @Mock
    File mockFile;

    @Mock
    ServletOutputStream mockResponseOutputStream;

    BrandingServlet testServlet;

    @Before
    public void setUp() throws Exception {
        testServlet = new BrandingServlet();
        testServlet.init(mockBrandingManager);
        when(mockBrandingManager.getBrandingRootPath()).thenReturn(mockFile);
        when(mockFile.getAbsolutePath()).thenReturn("/abs/test"); //$NON-NLS-1$
        when(mockRequest.getPathInfo()).thenReturn("/test/something.txt"); //$NON-NLS-1$
        when(mockResponse.getOutputStream()).thenReturn(mockResponseOutputStream);
    }

    @Test
    public void testDoGetNotFoundInvalidPath() throws IOException, ServletException {
        when(mockRequest.getPathInfo()).thenReturn(null); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetNotFoundMissingFile() throws IOException, ServletException {
        // The file should not exist, and thus return a 404.
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetExistingFile() throws IOException, ServletException, URISyntaxException {
        when(mockRequest.getPathInfo())
            .thenReturn("/org/ovirt/engine/core/branding/BrandingServletTest.class"); //$NON-NLS-1$
        when(mockFile.getAbsolutePath()).thenReturn(this.getClass().getClassLoader().
                getResource(".").toURI().getPath()); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).setHeader(eq("ETag"), anyString()); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathNullParameter() {
        File file = testServlet.getFile(mockFile, null);
        assertNull("Path should be null", file); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathNonSaneParameter() {
        File file = testServlet.getFile(mockFile, "../something"); //$NON-NLS-1$
        assertNull("Path should be null", file); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathSaneParameter() {
        File file = testServlet.getFile(mockFile, "/branding/test"); //$NON-NLS-1$
        assertNotNull("Path should not be null", file); //$NON-NLS-1$
        assertEquals("Path should be '/abs/test/branding/test'", //$NON-NLS-1$
                "/abs/test/branding/test", file.getAbsolutePath()); //$NON-NLS-1$
    }

}
