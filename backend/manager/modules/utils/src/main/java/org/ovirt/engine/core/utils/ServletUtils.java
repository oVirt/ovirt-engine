package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ServletUtils {
    // The log:
    private static final Logger log = Logger.getLogger(ServletUtils.class);

    // Map of MIME types:
    private static MimetypesFileTypeMap mimeMap;

    // Anything longer than this is considered a large file and a warning
    // will be generating when serving it:
    private static final long LARGE = 1048576; // 1 MiB

    static {
        // Load the system wide MIME types map:
        try {
            mimeMap = new MimetypesFileTypeMap("/etc/mime.types");
        }
        catch (IOException exception) {
            log.error("Can't load system mime types file.", exception);
            mimeMap = new MimetypesFileTypeMap();
        }
    }

    // The max size of path names (this is less than supported in Linux, but we
    // don't use paths larger than this, and this way we are a bit safer):
    private static final long PATH_MAX = 512;

    private ServletUtils() {
        // No instances allowed.
    }

    public static MimetypesFileTypeMap getMimeMap() {
        return mimeMap;
    }

    /**
     * Send a file to the output stream of the response passed into the method.
     * @param request The {@code HttpServletRequest} so we can get the path of the file.
     * @param response The {@code HttpServletResponse} so we can get the output stream and set response headers.
     * @param file The {@code File} to write to the response output stream.
     * @param type The MIME type of the file.
     * @throws IOException
     */
    public static void sendFile(final HttpServletRequest request, final HttpServletResponse response, final File file, final String defaultType) throws IOException {
        // Make sure the file exits and is readable and send a 404 error
        // response if it doesn't:
        if (!canReadFile(file)) {
            log.error("Can't read file \"" + file.getAbsolutePath() + "\" for request \"" + request.getRequestURI() + "\", will send a 404 error response.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Find the MIME type:
        String mime = defaultType;
        if (mime == null) {
            mime = mimeMap.getContentType(file);
        }
        // Set the content type:
        response.setContentType(mime);
        response.setContentLength((int) getFileSize(file));

        writeFileToStream(response.getOutputStream(), file);
    }

    /**
     * Check if the file is readable.
     * @param file
     * @return
     */
    public static boolean canReadFile(final File file) {
        return file != null && file.exists() && file.canRead();
    }

    /**
     * Write the file passed in out to the output stream passed in.
     * @param out The {@code OutputStream} to write to.
     * @param file The {@code File} to read.
     * @throws IOException If there is a problem reading the file or writing to the stream.
     */
    public static void writeFileToStream(final OutputStream out, final File file) throws IOException {
        // Send the content of the file:
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
        catch (IOException exception) {
            final String message = "Error sending file \"" + file.getAbsolutePath() + "\".";
            log.error(message, exception);
            throw new IOException(message, exception);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Returns the size of the {@code File} passed in.
     * @param file The file to get the length for.
     * @return The length of the file as a {@code long}
     */
    public static long getFileSize(File file) {
        // Advice against large files:
        final long length = file.length();
        if (length > LARGE) {
            log.warn("File \"" + file.getAbsolutePath() + " is " + length + " bytes long. Please reconsider using this servlet for files larger than " + LARGE + " bytes.");
        }
        return length;
    }

    /**
     * Check if the path passed in is sane. This method does the following checks:
     * <ol>
     *   <li>Checks if the path length is longer than the maximum allowed</li>
     *   <li>Checks if the path contains potentially dangerous characters (.., //, ./)</li>
     * </ol>
     * @param path The path to check.
     * @return {@code true} if the path is sane, {@code false} otherwise.
     */
    public static boolean isSane(String path) {
        // Check that the path is not too long:
        final int length = path.length();
        if (length > PATH_MAX) {
            log.error("The path is " + length + " characters long, which is longer than the maximum allowed " + PATH_MAX + ".");
            return false;
        }

        // Check that there aren't potentially dangerous directory navigation sequences:
        if (path.contains("..") || path.contains("//") || path.contains("./")) {
            log.error("The path contains potentially dangerous directory navigation sequences.");
            return false;
        }

        // All checks passed, the path is sane:
        return true;
    }

    /**
     * Get a {@code File} object from the passed in path and base location. This
     * method will do a sanity check to make sure the passed in path is not try
     * to read file it should. see isSane for all checks that are done.
     * @see #isSane
     * @param path The path to check for the file.
     * @param base The base path.
     * @return A {@code File} object pointing to the file, or null if the file
     * cannot be found.
     */
    public static File makeFileFromSanePath(String path, File base) {
        File file = null;

        if (path == null) {
            file = base;
        }
        else if (!isSane(path)) {
            log.error("The path \"" + path + "\" is not sane, will return null.");
        }
        else {
            file = new File(base, path);
        }
        return file;
    }


}
