package org.ovirt.engine.core.utils.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@code DocsServlet} class.
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class DocsServletTest {
    DocsServlet testServlet;

    /**
     * The mockRequest used in the test.
     */
    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    HttpSession mockSession;

    @Mock
    ServletConfig mockConfig;

    @Before
    public void setUp() throws Exception {
        testServlet = new DocsServlet();
        when(mockConfig.getInitParameter("file")).thenReturn(this.getClass().getResource("filetest").toURI().
                toASCIIString().replaceAll("file:", ""));
        ServletContext mockContext = mock(ServletContext.class);
        when(mockConfig.getServletContext()).thenReturn(mockContext);
        testServlet.init(mockConfig);
        when(mockRequest.getSession(true)).thenReturn(mockSession);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)}.
     * @throws ServletException If test fails
     * @throws IOException If test fails
     */
    @Test
    public void testDoGet_CheckIndex() throws ServletException, IOException {
        //Because we would have found the index file, we do a redirect and thus the response is committed.
        when(mockResponse.isCommitted()).thenReturn(Boolean.TRUE);
        when(mockRequest.getServletPath()).thenReturn("/docs");
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendRedirect("/docs/index.html");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet_NotFound() throws ServletException, IOException {
        when(mockRequest.getPathInfo()).thenReturn("/abc/def");
        when(mockRequest.getServletPath()).thenReturn("/docs");
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet_ResponseLangOkay() throws ServletException, IOException {
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        when(mockRequest.getPathInfo()).thenReturn("/fr/index.html");
        when(mockRequest.getServletPath()).thenReturn("/docs");
        testServlet.doGet(mockRequest, mockResponse);
        verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet_Lang_MissingFirstTimeNoDispatcher() throws ServletException, IOException {
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockConfig.getServletContext()).thenReturn(mockContext);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        when(mockRequest.getPathInfo()).thenReturn("/ja/index.html");
        when(mockRequest.getServletPath()).thenReturn("/docs");
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testDoGet_Lang_MissingFirstTimeNoDispatcher_SessionFalse() throws ServletException, IOException {
        ServletOutputStream responseOut = mock(ServletOutputStream.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockSession.getAttribute(DocsServlet.LANG_PAGE_SHOWN)).thenReturn(Boolean.FALSE);
        when(mockConfig.getServletContext()).thenReturn(mockContext);
        when(mockResponse.getOutputStream()).thenReturn(responseOut);
        when(mockRequest.getPathInfo()).thenReturn("/ja/index.html");
        when(mockRequest.getServletPath()).thenReturn("/docs");
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#determineActualFile(javax.servlet.http.HttpServletRequest, java.lang.String)}.
     */
    @Test
    public void testDetermineActualFile_US() {
        File originalFile = ServletUtils.makeFileFromSanePath("/" + Locale.US.toLanguageTag(), testServlet.base);
        when(mockRequest.getPathInfo()).thenReturn("/" + Locale.US.toLanguageTag());
        File actualFile = testServlet.determineActualFile(mockRequest, Locale.US);
        assertNotNull("actualFile should not be null", actualFile);
        assertTrue("actualFile should exist", actualFile.exists());
        assertEquals("original and actual should match", originalFile, actualFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#determineActualFile(javax.servlet.http.HttpServletRequest, java.lang.String)}.
     */
    @Test
    public void testDetermineActualFile_Fr() {
        //Fr exists, so the original and actual should match.
        File originalFile = ServletUtils.makeFileFromSanePath("/" + Locale.FRENCH.toLanguageTag(), testServlet.base);
        when(mockRequest.getPathInfo()).thenReturn("/" + Locale.FRENCH.toLanguageTag());
        File actualFile = testServlet.determineActualFile(mockRequest, Locale.US);
        assertNotNull("actualFile should not be null", actualFile);
        assertTrue("actualFile should exist", actualFile.exists());
        assertEquals("original and actual should match", originalFile, actualFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#determineActualFile(javax.servlet.http.HttpServletRequest, java.lang.String)}.
     */
    @Test
    public void testDetermineActualFile_Jp() {
        //Japanese does not exist, so the original and actual should NOT match.
        File originalFile = ServletUtils.makeFileFromSanePath("/" + Locale.JAPANESE.toLanguageTag(), testServlet.base);
        when(mockRequest.getPathInfo()).thenReturn("/" + Locale.JAPANESE.toLanguageTag());
        File actualFile = testServlet.determineActualFile(mockRequest, Locale.JAPANESE);
        assertNotNull("actualFile should not be null", actualFile);
        assertFalse("original and actual should not match", originalFile.equals(actualFile));
        assertTrue("actual file should end in /en-US", actualFile.toString().endsWith(Locale.US.toLanguageTag()));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleFromRequest(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleFromRequest() {
        Locale result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be en-US", Locale.US, result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?locale=fr");
        result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be fr", Locale.FRENCH, result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleFromRequest(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleFromRequest_withHash() {
        Locale result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be en-US", Locale.US, result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?locale=fr#basic");
        result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be fr", Locale.FRENCH, result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleFromRequest(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleFromRequest_Brazilian() {
        Locale result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be en-US", Locale.US, result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?locale=pt_BR");
        result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be pt_BR", new Locale("pt", "BR"), result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleFromRequest(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleFromRequest_Path() {
        when(mockRequest.getPathInfo()).thenReturn("/ja/index.html");
        Locale result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be ja", Locale.JAPANESE, result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleFromRequest(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleFromRequest_Path_Underscore() {
        when(mockRequest.getPathInfo()).thenReturn("/ja-JP/index.html");
        Locale result = testServlet.getLocaleFromRequest(mockRequest);
        assertEquals("The locale should be ja_JP", Locale.JAPAN, result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleStringFromPath(java.lang.String)}.
     */
    @Test
    public void testGetLocaleStringFromPath() {
        String result = testServlet.getLocaleStringFromPath(null);
        assertNull("There should be no result", result);
        result = testServlet.getLocaleStringFromPath("/index.html");
        assertNull("There should be no result", result);
        //File doesn't exist, it will end up with a 404 eventually
        result = testServlet.getLocaleStringFromPath("/index2.html");
        assertNotNull("There should be a result", result);
        assertEquals("locale should be 'index2.html'", "index2.html", result);
        //File does exist, but it is a directory.
        result = testServlet.getLocaleStringFromPath("/fr/index.html");
        assertNotNull("There should be a result", result);
        assertEquals("locale should be 'fr'", "fr", result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleStringFromPath(java.lang.String)}.
     */
    @Test
    public void testGetLocaleStringFromPath_IllegalPath() {
        assertNull("Path without '/' should return null", testServlet.getLocaleStringFromPath("index.html"));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleStringFromReferer(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleStringFromReferer() {
        String result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertNull("There should be no result", result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("123thisisnot a uri");
        result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertNull("There should be no result", result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html#noparam");
        result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertNull("There should be no result", result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?param1=something&param2=somethingelse");
        result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertNull("There should be no result", result);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.DocsServlet#getLocaleStringFromReferer(javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public void testGetLocaleStringFromReferer_Valid() {
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?locale=fr");
        String result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertEquals("The result should be 'fr'", "fr", result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?param1=xxx&locale=fr");
        result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertEquals("The result should be 'fr'", "fr", result);
        when(mockRequest.getHeader(DocsServlet.REFERER)).thenReturn("http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?param1=xxx&locale=fr&param2=yyy");
        result = testServlet.getLocaleStringFromReferer(mockRequest);
        assertEquals("The result should be 'fr'", "fr", result);
    }
}
