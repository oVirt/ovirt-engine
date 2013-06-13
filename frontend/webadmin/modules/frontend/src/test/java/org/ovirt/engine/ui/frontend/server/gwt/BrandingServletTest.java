package org.ovirt.engine.ui.frontend.server.gwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.branding.BrandingManager;

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
    String testFileEtag;

    @Before
    public void setUp() throws Exception {
        testServlet = new BrandingServlet();
        testServlet.init(mockBrandingManager);
        when(mockBrandingManager.getBrandingRootPath()).thenReturn(mockFile);
        when(mockFile.getAbsolutePath()).thenReturn("/abs/test"); //$NON-NLS-1$
        when(mockRequest.getPathInfo()).thenReturn("/test/something.txt"); //$NON-NLS-1$
        when(mockResponse.getOutputStream()).thenReturn(mockResponseOutputStream);
        File testFile = new File(this.getClass().getClassLoader().
                getResource("./org/ovirt/engine/ui/frontend/server/gwt/BrandingServletTest.class") //$NON-NLS-1$
                .getFile());
        testFileEtag = testServlet.generateEtag(testFile);
    }

    @Test
    public void testDoGet_NotFound_InvalidPath() throws IOException, ServletException {
        when(mockRequest.getPathInfo()).thenReturn(null); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGet_NotFound_MissingFile() throws IOException, ServletException {
        // The file should not exist, and thus return a 404.
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGet_ExistingFile() throws IOException, ServletException {
        when(mockRequest.getPathInfo())
            .thenReturn("/org/ovirt/engine/ui/frontend/server/gwt/BrandingServletTest.class"); //$NON-NLS-1$
        when(mockFile.getAbsolutePath()).thenReturn(this.getClass().getClassLoader().
                getResource(".").getFile()); //$NON-NLS-1$
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).addHeader(GwtDynamicHostPageServlet.ETAG_HEADER, testFileEtag);
    }

    @Test
    public void testDoGet_ExistingFile_NotModified() throws IOException, ServletException {
        when(mockRequest.getPathInfo())
            .thenReturn("/org/ovirt/engine/ui/frontend/server/gwt/BrandingServletTest.class"); //$NON-NLS-1$
        when(mockFile.getAbsolutePath()).thenReturn(this.getClass().getClassLoader().
                getResource(".").getFile()); //$NON-NLS-1$
        when(mockRequest.getHeader(GwtDynamicHostPageServlet.IF_NONE_MATCH_HEADER)).thenReturn(testFileEtag);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    @Test
    public void testGenerateEtag() {
        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(1234L);
        Date lastModifiedDate = new Date();
        when(mockFile.lastModified()).thenReturn(lastModifiedDate.getTime());
        String result = testServlet.generateEtag(mockFile);
        assertNotNull("There should be a result", result); //$NON-NLS-1$
        assertEquals("W/\"1234-" + lastModifiedDate.getTime() + "\"", result); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetFullPath_nullParameter() {
        String fullPath = testServlet.getFullPath(mockFile, null);
        assertNull("Path should be null", fullPath); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPath_NonSaneParameter() {
        String fullPath = testServlet.getFullPath(mockFile, "../something"); //$NON-NLS-1$
        assertNull("Path should be null", fullPath); //$NON-NLS-1$
    }

    @Test
    public void testGetFullPath_SaneParameter() {
        String fullPath = testServlet.getFullPath(mockFile, "/branding/test"); //$NON-NLS-1$
        assertNotNull("Path should not be null", fullPath); //$NON-NLS-1$
        assertEquals("Path should be '/abs/test/branding/test'", //$NON-NLS-1$
                "/abs/test/branding/test", fullPath); //$NON-NLS-1$
    }

}
