package org.ovirt.engine.core;

import static org.ovirt.engine.core.ServletUtils.sendFile;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This is a very simple servlet that receives a mime type and a file name as
 * parameters and serves the content of that file. It is intended to use in the
 * web application descriptor as follows:
 *
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;web-conf.js&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.ovirt.engine.core.FileServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;type&lt;/param-name&gt;
 *     &lt;param-value&gt;text/javascript&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;name&lt;/param-name&gt;
 *     &lt;param-value&gt;/etc/ovirt-engine/web-conf.js&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 *  &lt;/servlet&gt;
 *  &lt;servlet-mapping&gt;
 *    &lt;servlet-name&gt;web-conf.js&lt;/servlet-name&gt;
 *    &lt;url-pattern&gt;/web-conf.js&lt;/url-pattern&gt;
 *  &lt;/servlet-mapping&gt;
 * &lt;servlet&gt;
 * </pre>
 *
 * This can be useful when the web application has to serve files that are
 * contained in external directories.
 *
 * If the file given in the <code>file</code> is a directory the servlet will
 * serve any file inside that directory, taking the relative path from the
 * request URL and appending it to the value of the <code>file</code> parameter.
 *
 * If the <code>type</code> parameter is not given the content type will be
 * calculated according to the system MIME types configuration file
 * <code>/etc/mime.types</code>.
 */
public class FileServlet extends HttpServlet {
    // Serialization id:
    private static final long serialVersionUID = -1794616863361641804L;

    // The log:
    private static final Logger log = Logger.getLogger(FileServlet.class);

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

    // The names of the parameters:
    private static final String TYPE = "type";
    private static final String FILE = "file";

    // The name of the index page:
    private static final String INDEX = "index.html";

    // The values of the parameters:
    private String type;
    private File base;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);

        // Get the content type of the file (it can be null, in which case the
        // global system MIME types map will be used to figure it out):
        type = config.getInitParameter(TYPE);

        // Get the name of the file or base directory:
        final String name = config.getInitParameter(FILE);
        if (name == null) {
            final String message = "Can't get base name from parameter \"" + FILE + "\".";
            log.error(message);
            throw new ServletException(message);
        }

        // Create the base file object:
        base = new File(name);
    }

    private static boolean isSane(String path) {
        // Check that the path is not too long:
        final int lenght = path.length();
        if (lenght > PATH_MAX) {
            log.error("The path is " + lenght + " characters long, which is longer than the maximum allowed " + PATH_MAX + ".");
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the requested path:
        String path = request.getPathInfo();

        // Locate the requested file:
        File file = null;
        if (path == null) {
            file = base;
        }
        else if (!isSane(path)) {
            log.error("The path \"" + path + "\" is not sane, will send a 404 error response.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        else {
            file = new File(base, path);
        }

        // If the requested file is a directory then try to replace it with the
        // corresponding index page (if it exists):
        if (file.isDirectory()) {
            File index = new File(file, INDEX);
            log.info("Index is \"" + index.getAbsolutePath() + "\".");
            if (index.isFile()) {
                String redirect = null;
                if (path == null) {
                    redirect = request.getServletPath() + "/" + INDEX;
                }
                else {
                    redirect = request.getServletPath() + path + "/" + INDEX;
                }
                response.sendRedirect(redirect);
            }
            else {
                log.error("There is no index page for directory \"" + file.getAbsolutePath() + "\", will send a 404 error response.");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        // Find the MIME type:
        String mime = type;
        if (mime == null) {
            mime = mimeMap.getContentType(file);
        }

        // Send the content of the file:
        sendFile(request, response, file, mime);
    }
}
