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

    // Anything longer than this is considered a large file and a warning
    // will be generating when serving it:
    private static final long LARGE = 1048576; // 1 MiB

    private ServletUtils() {
        // No instances allowed.
    }

    public static MimetypesFileTypeMap getMimeMap() {
        return mimeMap;
    }

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

    public static void sendFile(final HttpServletRequest request, final HttpServletResponse response, final File file, final String type) throws IOException {
        // Make sure the file exits and is readable and send a 404 error
        // response if it doesn't:
        if (!file.exists() || !file.canRead()) {
            log.error("Can't read file \"" + file.getAbsolutePath() + "\" for request \"" + request.getRequestURI() + "\", will send a 404 error response.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Advice against large files:
        long length = file.length();
        if (length > LARGE) {
            log.warn("File \"" + file.getAbsolutePath() + " is " + length + " bytes long. Please reconsider using this servlet for files larger than " + LARGE + " bytes.");
        }

        // Set the content type:
        response.setContentType(type);
        response.setContentLength((int) length);

        // Send the content of the file:
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
        catch (IOException exception) {
            final String message = "Error sending file \"" + file.getAbsolutePath() + "\".";
            log.error(message, exception);
            throw new IOException(message);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
