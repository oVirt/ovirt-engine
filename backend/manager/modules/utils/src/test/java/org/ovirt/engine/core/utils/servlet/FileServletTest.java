package org.ovirt.engine.core.utils.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
public class FileServletTest {
    FileServlet testServlet;

    @Mock
    ServletConfig mockConfig;
    @Mock
    HttpServletRequest mockRequest;
    @Mock
    HttpServletResponse mockResponse;

    File file;

    @Before
    public void setUp() throws Exception {
        file = new File(this.getClass().getResource("small_file.txt").toURI());
        testServlet = new FileServlet();
        ServletContext mockContext = mock(ServletContext.class);
        when(mockConfig.getServletContext()).thenReturn(mockContext);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#init(javax.servlet.ServletConfig)}.
     */
    @Test(expected=ServletException.class)
    public void testInitServletConfig_NoInitParams() throws ServletException {
        testServlet.init(mockConfig);
        fail("Should not get here");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#init(javax.servlet.ServletConfig)}.
     */
    @Test
    public void testInitServletConfig_BaseSet() throws ServletException {
        when(mockConfig.getInitParameter("file")).thenReturn(file.getParent());
        testServlet.init(mockConfig);
        assertNull("Type should be null", testServlet.type);
        assertEquals("base should be " + file.getParent(), file.getParentFile(), testServlet.base);
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
        verify(mockResponse).setHeader(eq("ETag"), anyString());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(anyObject(), eq(0), anyInt());
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
        verify(mockResponse).setHeader(eq("ETag"), anyString());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(anyObject(), eq(0), anyInt());
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
        verify(mockResponse, never()).setHeader(eq("ETag"), anyString());
        //Make sure something is written to the output stream (assuming it is the file).
        verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#checkForIndex(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testCheckForIndex_BadParams() throws IOException, URISyntaxException {
        assertNull("no index file", testServlet.checkForIndex(mockRequest, mockResponse, null, null));
        assertNull("no index file", testServlet.checkForIndex(mockRequest, mockResponse, file.getParentFile(), null));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.FileServlet#checkForIndex(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testCheckForIndex_Exists() throws IOException, URISyntaxException {
        File file = new File(this.getClass().getResource("filetest").toURI());
        when(mockRequest.getServletPath()).thenReturn("/test/path");
        File indexFile = testServlet.checkForIndex(mockRequest, mockResponse, file, null);
        assertNotNull("indexFile should not be null", indexFile);
        assertTrue("indexFile should exist", indexFile.exists());
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
        assertNotNull("indexFile should not be null", indexFile);
        assertTrue("indexFile should exist", indexFile.exists());
        verify(mockResponse).sendRedirect("/test/path/path2/index.html");
    }
}
