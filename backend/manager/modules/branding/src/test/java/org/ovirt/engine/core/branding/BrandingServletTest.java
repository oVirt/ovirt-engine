package org.ovirt.engine.core.branding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @BeforeEach
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
    public void testDoGetExistingFile() throws IOException, URISyntaxException, ServletException {
        when(mockRequest.getPathInfo())
            .thenReturn("/org/ovirt/engine/core/branding/BrandingServletTest.class"); //$NON-NLS-1$
        when(mockFile.getAbsolutePath()).thenReturn(this.getClass().getClassLoader().
                getResource(".").toURI().getPath()); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).setHeader(eq("ETag"), any()); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathNullParameter() {
        File file = testServlet.getFile(mockFile, null);
        assertNull(file, "Path should be null"); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathNonSaneParameter() {
        File file = testServlet.getFile(mockFile, "../something"); //$NON-NLS-1$
        assertNull(file, "Path should be null"); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPathSaneParameter() {
        File file = testServlet.getFile(mockFile, "/branding/test"); //$NON-NLS-1$
        assertNotNull(file, "Path should not be null"); //$NON-NLS-1$
        assertEquals("/abs/test/branding/test", file.getAbsolutePath(), //$NON-NLS-1$
                "Path should be '/abs/test/branding/test'"); //$NON-NLS-1$
    }

}
