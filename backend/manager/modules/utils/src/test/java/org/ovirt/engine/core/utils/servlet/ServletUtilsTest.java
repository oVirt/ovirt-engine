package org.ovirt.engine.core.utils.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

public class ServletUtilsTest {
    private String canReadFileName;

    @Before
    public void setup() throws IOException, URISyntaxException {
        canReadFileName = this.getClass().getResource("small_file.txt").toURI().toASCIIString().replaceAll("file:", "");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#canReadFile(java.io.File)}.
     */
    @Test
    public void testCanReadFile() {
        // Make sure the user is not root
        // This tests relies on EXISTING files which only root can access
        String userName = System.getProperty("user.name");
        assumeTrue(!"root".equals(userName));

        //Does not exist.
        File file = new File("/doesnotexist/iamprettysure");
        assertFalse("We should not be able to read this file.", ServletUtils.canReadFile(file));
        //Exists, but should not be readable.
        file = new File("/etc/securetty");
        assertFalse("We should not be able to read this file.", ServletUtils.canReadFile(file));
        //Exists and we can read.
        file = new File(canReadFileName);
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream() throws IOException {
        File file = new File(canReadFileName);
        long hostSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        ServletUtils.writeFileToStream(out, file);
        assertEquals("The bytes in the buffer have to match the length of the file", (int)hostSize, out.size());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream_0SizeFile() throws Exception {
        File file = new File(this.getClass().getResource("zerosize").toURI());
        long zeroSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        assertEquals("The file size should be 0", 0L, zeroSize);
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        ServletUtils.writeFileToStream(out, file);
        assertEquals("The bytes in the buffer have to match the length of the file", (int)zeroSize, out.size());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test(expected = IOException.class)
    public void testWriteFileToStream_IOException() throws IOException {
        File file = new File("/doesnotexist/iamprettysure");
        //Make a large buffer.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletUtils.writeFileToStream(out, file);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#getFileSize(java.io.File)}.
     */
    @Test
    public void testGetFileSize() {
        File file = new File(canReadFileName);
        long hostsSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        assertEquals("Values should match", hostsSize, ServletUtils.getFileSize(file));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#isSane(java.lang.String)}.
     */
    @Test
    public void testIsSane() {
        assertTrue("/etc should be sane", ServletUtils.isSane("/etc"));
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            builder.append("/abcdefghijkl");
        }
        assertFalse("longPath is not sane", ServletUtils.isSane(builder.toString()));
        assertFalse("path with .. is not sane", ServletUtils.isSane("/something/../etc/password"));
        assertFalse("path with // is not sane", ServletUtils.isSane("/something//etc/password"));
        assertFalse("path with ./ is not sane", ServletUtils.isSane("/something/./etc/password"));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_NullPath() {
        File file = new File(canReadFileName);
        File testFile = ServletUtils.makeFileFromSanePath(null, file);
        assertEquals("new file should be same as old file", file, testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_Happy() throws URISyntaxException {
        String path = this.getClass().getResource(".").toURI().toASCIIString().replaceAll("file:", "");
        File file = new File(path);
        File testFile = ServletUtils.makeFileFromSanePath("small_file.txt", file);
        assertEquals("new file should be same as old file", new File(canReadFileName), testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFrom_InsanePath() {
        File file = new File("/etc");
        File testFile = ServletUtils.makeFileFromSanePath("/../hosts", file);
        assertNull("testfile should be null", testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_MissingFile() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = new File("/etc/doesntexist");
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_PNG() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, "application/xml");
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("application/xml");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int) file.length());
       //Make sure the stream is written to.
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_PNGNoMime() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("image/png");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int) file.length());
       //Make sure the stream is written to.
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String, boolean)}.
     */
    @Test
    public void testSendFile_PNGNoMime_Cache() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, null, true);
       //Check that we have eTag
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("image/png");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int) file.length());
       //Make sure the stream is written to.
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String, boolean)}.
     */
    @Test
    public void testSendFile_PNGNoMime_NoCache() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, null, false);
       //Check that we have eTag
       verify(mockResponse, never()).setHeader(eq("ETag"), anyString());
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("image/png");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int) file.length());
       //Make sure the stream is written to.
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    private File createTempPng() throws IOException {
        File file = File.createTempFile("favicon", ".png");
        file.deleteOnExit();
        BufferedImage img = new BufferedImage(256, 256,
                BufferedImage.TYPE_INT_RGB);
        if (!ImageIO.write(img, "PNG", file)) {
            fail("Unable to write temporary image file");
        }
        return file;
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void test_ETag_Format() throws IOException {
        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(1234L);
        when(mockFile.lastModified()).thenReturn(8271L);
        assertEquals("ETag does not match", "W/\"1234-8271\"", ServletUtils.getETag(mockFile));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_ETag_None() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       when(mockRequest.getHeader("If-None-Match")).thenReturn(null);
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_ETag_Same() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       File file = createTempPng();
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       when(mockRequest.getHeader("If-None-Match")).thenReturn(ServletUtils.getETag(file));
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_ETag_All() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       File file = createTempPng();
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       when(mockRequest.getHeader("If-None-Match")).thenReturn("*");
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       verify(mockResponse).setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void testSendFile_ETag_Different() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       File file = createTempPng();
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       when(mockRequest.getHeader("If-None-Match")).thenReturn("xxxx");
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       verify(responseOut).write(anyObject(), eq(0), anyInt());
    }

    @Test
    public void testGetAsAbsoluteContext() {
        String result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "..");
        assertEquals("The result should be '/ovirt-engine/'", "/ovirt-engine/", result);
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "/something");
        assertEquals("The result should be '/something'", "/something", result);
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "../somethingelse");
        assertEquals("The result should be '/ovirt-engine/somethingelse'", "/ovirt-engine/somethingelse", result);
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", ".");
        assertEquals("The result should be '/ovirt-engine/testpath/'", "/ovirt-engine/testpath/", result);
    }

    @Test
    public void testGetBaseContextPath() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getServletContext()).thenReturn(mockServletContext);
        when(mockRequest.getContextPath()).thenReturn("/ovirt-engine/test");
        when(mockServletContext.getInitParameter(ServletUtils.CONTEXT_TO_ROOT_MODIFIER)).thenReturn("..");
        String result = ServletUtils.getBaseContextPath(mockRequest);
        assertEquals("Result should be '/ovirt-engine/'", "/ovirt-engine/", result);
    }

    @Test
    public void testGetBaseContextPath2() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletContext mockServletContext = mock(ServletContext.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getServletContext()).thenReturn(mockServletContext);
        when(mockRequest.getContextPath()).thenReturn("/ovirt-engine/test");
        when(mockServletContext.getInitParameter(ServletUtils.CONTEXT_TO_ROOT_MODIFIER)).thenReturn("../..");
        String result = ServletUtils.getBaseContextPath(mockRequest);
        assertEquals("Result should be '/'", "/", result);
    }

}
