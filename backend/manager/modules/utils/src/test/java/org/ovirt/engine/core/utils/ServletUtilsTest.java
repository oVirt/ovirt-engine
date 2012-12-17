package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class ServletUtilsTest {

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#canReadFile(java.io.File)}.
     */
    @Test
    public void testCanReadFile() {
        //Does not exist.
        File file = new File("/doesnotexist/iamprettysure");
        assertFalse("We should not be able to read this file.", ServletUtils.canReadFile(file));
        //Exists, but should not be readable.
        file = new File("/etc/securetty");
        assertFalse("We should not be able to read this file.", ServletUtils.canReadFile(file));
        //Exists and we can read.
        file = new File("/etc/hosts");
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream() {
        File file = new File("/etc/hosts");
        long hostSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        try {
            ServletUtils.writeFileToStream(out, file);
            assertEquals("The bytes in the buffer have to match the length of the file", (int)hostSize, out.size());
        } catch(IOException ioe) {
            fail("IOException thrown: " + ioe.getMessage());
        }
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream_0SizeFile() throws Exception {
        File file = new File(this.getClass().getResource("zerosize").toURI());
        long zeroSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        assertEquals("The file size should be 0", 0L, zeroSize);
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        try {
            ServletUtils.writeFileToStream(out, file);
            assertEquals("The bytes in the buffer have to match the length of the file", (int)zeroSize, out.size());
        } catch(IOException ioe) {
            fail("IOException thrown: " + ioe.getMessage());
        }
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#writeFileToStream(java.io.OutputStream, java.io.File)}.
     */
    @Test
    public void testWriteFileToStream_IOException() {
        File file = new File("/doesnotexist/iamprettysure");
        //Make a large buffer.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ServletUtils.writeFileToStream(out, file);
            fail("Should not get here, file doesn't exist");
        } catch(IOException ioe) {
        }
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#getFileSize(java.io.File)}.
     */
    @Test
    public void testGetFileSize() {
        File file = new File("/etc/hosts");
        long hostsSize = file.length();
        assertTrue("We should be able to read this file.", ServletUtils.canReadFile(file));
        assertEquals("Values should match", hostsSize, ServletUtils.getFileSize(file));
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#isSane(java.lang.String)}.
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
     * Test method for {@link org.ovirt.engine.core.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_NullPath() {
        File file = new File("/etc/hosts");
        File testFile = ServletUtils.makeFileFromSanePath(null, file);
        assertEquals("new file should be same as old file", file, testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFromString_Happy() {
        File file = new File("/etc");
        File testFile = ServletUtils.makeFileFromSanePath("hosts", file);
        assertEquals("new file should be same as old file", new File("/etc/hosts"), testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#makeFileFromSanePath(java.lang.String, java.io.File)}.
     */
    @Test
    public void testGetFileFrom_InsanePath() {
        File file = new File("/etc");
        File testFile = ServletUtils.makeFileFromSanePath("/../hosts", file);
        assertNull("testfile should be null", testFile);
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     * @throws IOException
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
     * Test method for {@link org.ovirt.engine.core.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     * @throws IOException
     */
    @Test
    public void testSendFile_PNG() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = new File("/etc/favicon.png");
       ServletUtils.sendFile(mockRequest, mockResponse, file, "application/xml");
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("application/xml");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int)file.length());
       //Make sure the stream is written to.
       verify(responseOut).write((byte[])anyObject(), eq(0), anyInt());
    }

    /**
     * Test method for {@link org.ovirt.engine.core.ServletUtils#sendFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.File, java.lang.String)}.
     * @throws IOException
     */
    @Test
    public void testSendFile_PNGNoMime() throws IOException {
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);
       HttpServletResponse mockResponse = mock(HttpServletResponse.class);
       ServletOutputStream responseOut = mock(ServletOutputStream.class);
       when(mockResponse.getOutputStream()).thenReturn(responseOut);
       File file = new File("/etc/favicon.png");
       ServletUtils.sendFile(mockRequest, mockResponse, file, null);
       //Check that the mime type was set to the one passed in, instead of the one associated with the file
       verify(mockResponse).setContentType("image/png");
       //Check the file length is set right.
       verify(mockResponse).setContentLength((int)file.length());
       //Make sure the stream is written to.
       verify(responseOut).write((byte[])anyObject(), eq(0), anyInt());
    }
}
