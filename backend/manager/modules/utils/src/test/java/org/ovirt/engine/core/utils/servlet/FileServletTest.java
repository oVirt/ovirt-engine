package org.ovirt.engine.core.utils.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
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
public class FileServletTest {
    FileServlet testServlet;

    @Mock
    ServletConfig mockConfig;
    @Mock
    HttpServletRequest mockRequest;
    @Mock
    HttpServletResponse mockResponse;

    File file;

    @BeforeEach
    public void setUp() throws Exception {
        file = new File(this.getClass().getResource("small_file.txt").toURI());
        testServlet = new FileServlet();
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#init(javax.servlet.ServletConfig)}.
     */
    @Test
    public void testInitServletConfig_NoInitParams() {
        assertThrows(ServletException.class, () -> testServlet.init(mockConfig));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#init(javax.servlet.ServletConfig)}.
     */
    @Test
    public void testInitServletConfig_BaseSet() throws ServletException {
        when(mockConfig.getInitParameter("file")).thenReturn(file.getParent());
        testServlet.init(mockConfig);
        assertNull(testServlet.type, "Type should be null");
        assertEquals(file.getParentFile(), testServlet.base, "base should be " + file.getParent());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet_Empty() throws ServletException, IOException {
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet1() throws ServletException, IOException {
        when(mockConfig.getInitParameter("file")).thenReturn(file.getParent());
        testServlet.init(mockConfig);
        when(mockRequest.getPathInfo()).thenReturn(file.getName());
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        testServlet.doGet(mockRequest, mockResponse);
        //Make sure cache is enabled
        verify(mockResponse).setHeader(eq("ETag"), any());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(any(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet2() throws ServletException, IOException {
        when(mockConfig.getInitParameter("cache")).thenReturn("true");
        when(mockConfig.getInitParameter("file")).thenReturn(file.getParent());
        testServlet.init(mockConfig);
        when(mockRequest.getPathInfo()).thenReturn(file.getName());
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        testServlet.doGet(mockRequest, mockResponse);
        //Make sure cache is enabled
        verify(mockResponse).setHeader(eq("ETag"), any());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(any(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet3() throws ServletException, IOException {
        when(mockConfig.getInitParameter("cache")).thenReturn("false");
        when(mockConfig.getInitParameter("file")).thenReturn(file.getParent());
        testServlet.init(mockConfig);
        when(mockRequest.getPathInfo()).thenReturn(file.getName());
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        testServlet.doGet(mockRequest, mockResponse);
        //Make sure cache is disabled
        verify(mockResponse, never()).setHeader(eq("ETag"), any());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(any(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#checkForIndex(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testCheckForIndex_BadParams() throws IOException {
        assertNull(testServlet.checkForIndex(mockRequest, mockResponse, null, null), "no index file");
        assertNull(testServlet.checkForIndex(mockRequest, mockResponse, file.getParentFile(), null), "no index file");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#checkForIndex(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testCheckForIndex_Exists() throws IOException, URISyntaxException {
        File file = new File(this.getClass().getResource("filetest").toURI());
        when(mockRequest.getServletPath()).thenReturn("/test/path");
        File indexFile = testServlet.checkForIndex(mockRequest, mockResponse, file, null);
        assertNotNull(indexFile, "indexFile should not be null");
        assertTrue(indexFile.exists(), "indexFile should exist");
        verify(mockResponse).sendRedirect("/test/path/index.html");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#checkForIndex(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testCheckForIndex_Exists2() throws IOException, URISyntaxException {
        File file = new File(this.getClass().getResource("filetest").toURI());
        when(mockRequest.getServletPath()).thenReturn("/test/path");
        File indexFile = testServlet.checkForIndex(mockRequest, mockResponse, file, "/path2");
        assertNotNull(indexFile, "indexFile should not be null");
        assertTrue(indexFile.exists(), "indexFile should exist");
        verify(mockResponse).sendRedirect("/test/path/path2/index.html");
    }
}
