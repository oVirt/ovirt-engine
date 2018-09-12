package org.ovirt.engine.core.utils.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServletUtilsTest {
    private String canReadFileName;

    @BeforeEach
    public void setup() throws URISyntaxException {
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
        assertFalse(ServletUtils.canReadFile(file), "We should not be able to read this file.");
        //Exists, but should not be readable.
        file = new File("/etc/securetty");
        assertFalse(ServletUtils.canReadFile(file), "We should not be able to read this file.");
        //Exists and we can read.
        file = new File(canReadFileName);
        assertTrue(ServletUtils.canReadFile(file), "We should be able to read this file.");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream() throws IOException {
        File file = new File(canReadFileName);
        long hostSize = file.length();
        assertTrue(ServletUtils.canReadFile(file), "We should be able to read this file.");
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        ServletUtils.writeFileToStream(out, file);
        assertEquals((int)hostSize, out.size(), "The bytes in the buffer have to match the length of the file");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream_0SizeFile() throws Exception {
        File file = new File(this.getClass().getResource("zerosize").toURI());
        long zeroSize = file.length();
        assertTrue(ServletUtils.canReadFile(file), "We should be able to read this file.");
        assertEquals(0L, zeroSize, "The file size should be 0");
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        ServletUtils.writeFileToStream(out, file);
        assertEquals((int)zeroSize, out.size(), "The bytes in the buffer have to match the length of the file");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream_IOException() {
        File file = new File("/doesnotexist/iamprettysure");
        //Make a large buffer.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> ServletUtils.writeFileToStream(out, file));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#isSane(java.lang.String)}.
     */
    @Test
    public void testIsSane() {
        assertTrue(ServletUtils.isSane("/etc"), "/etc should be sane");
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            builder.append("/abcdefghijkl");
        }
        assertFalse(ServletUtils.isSane(builder.toString()), "longPath is not sane");
        assertFalse(ServletUtils.isSane("/something/../etc/password"), "path with .. is not sane");
        assertFalse(ServletUtils.isSane("/something//etc/password"), "path with // is not sane");
        assertFalse(ServletUtils.isSane("/something/./etc/password"), "path with ./ is not sane");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_NullPath() {
        File file = new File(canReadFileName);
        File testFile = ServletUtils.makeFileFromSanePath(null, file);
        assertEquals(file, testFile, "new file should be same as old file");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_Happy() throws URISyntaxException {
        String path = this.getClass().getResource(".").toURI().toASCIIString().replaceAll("file:", "");
        File file = new File(path);
        File testFile = ServletUtils.makeFileFromSanePath("small_file.txt", file);
        assertEquals(new File(canReadFileName), testFile, "new file should be same as old file");
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFrom_InsanePath() {
        File file = new File("/etc");
        File testFile = ServletUtils.makeFileFromSanePath("/../hosts", file);
        assertNull(testFile, "testfile should be null");
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
       verify(responseOut).write(any(), eq(0), anyInt());
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
       verify(responseOut).write(any(), eq(0), anyInt());
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
       verify(responseOut).write(any(), eq(0), anyInt());
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
       verify(mockResponse, never()).setHeader(eq("ETag"), any());
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("image/png");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int) file.length());
       //Make sure the stream is written to.
       verify(responseOut).write(any(), eq(0), anyInt());
    }

    private File createTempPng() throws IOException {
        File file = File.createTempFile("favicon", ".png");
        file.deleteOnExit();
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        assertTrue(ImageIO.write(img, "PNG", file), "Unable to write temporary image file");
        return file;
    }

    /**
     * Test method for {@link org.ovirt.engine.core.utils.servlet.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     */
    @Test
    public void test_ETag_Format() {
        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(1234L);
        when(mockFile.lastModified()).thenReturn(8271L);
        assertEquals("W/\"1234-8271\"", ServletUtils.getETag(mockFile), "ETag does not match");
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
       File file = createTempPng();
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       verify(mockResponse).setHeader("ETag", ServletUtils.getETag(file));
       verify(responseOut).write(any(), eq(0), anyInt());
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
       verify(responseOut).write(any(), eq(0), anyInt());
    }

    @Test
    public void testGetAsAbsoluteContext() {
        String result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "..");
        assertEquals("/ovirt-engine/", result, "The result should be '/ovirt-engine/'");
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "/something");
        assertEquals("/something", result, "The result should be '/something'");
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", "../somethingelse");
        assertEquals("/ovirt-engine/somethingelse", result, "The result should be '/ovirt-engine/somethingelse'");
        result = ServletUtils.getAsAbsoluteContext("/ovirt-engine/testpath", ".");
        assertEquals("/ovirt-engine/testpath/", result, "The result should be '/ovirt-engine/testpath/'");
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
        assertEquals("/ovirt-engine/", result, "Result should be '/ovirt-engine/'");
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
        assertEquals("/", result, "Result should be '/'");
    }

}
