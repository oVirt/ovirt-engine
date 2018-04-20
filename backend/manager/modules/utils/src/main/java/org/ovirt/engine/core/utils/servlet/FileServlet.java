package org.ovirt.engine.core.utils.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(FileServlet.class);

    // The names of the parameters:
    private static final String CACHE = "cache";
    private static final String TYPE = "type";
    private static final String FILE = "file";
    private static final String REQUIRED = "required";

    // The name of the index page:
    private static final String INDEX = "index.html";

    // The values of the parameters:
    protected boolean cache;
    protected String type;
    protected File base;
    protected boolean required = true;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);

        final String cacheString = config.getInitParameter(CACHE);
        if (cacheString == null) {
            cache = true;
        } else {
            cache = Boolean.parseBoolean(cacheString);
        }

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
        // we use %{x} convention to avoid conflict with jboss properties
        final String expanded = EngineLocalConfig.getInstance().expandString(name.replaceAll("%\\{", "\\${"));
        if (StringUtils.isEmpty(expanded)) {
            final String message = "Refusing to serve empty location";
            log.debug(message);
            throw new ServletException(message);
        }

        // is the base file required
        final String isBaseRequired = config.getInitParameter(REQUIRED);
        if (isBaseRequired != null) {
            required = Boolean.parseBoolean(isBaseRequired);
        }

        base = new File(expanded);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Locate the requested file:
        File file = ServletUtils.makeFileFromSanePath(request.getPathInfo(), base);
        file = checkForIndex(request, response, file, request.getPathInfo());
        // Send the content of the file:
        // type is the default MIME type of the Servlet.
        ServletUtils.sendFile(request, response, file, type, cache, required);
    }

    protected File checkForIndex(HttpServletRequest request, HttpServletResponse response, File file, String path) throws IOException {
        // If the requested file is a directory then try to replace it with the
        // corresponding index page (if it exists):
        if (file != null && file.isDirectory()) {
            File index = new File(file, INDEX);
            log.info("Index is '{}'.", index.getAbsolutePath());
            if (index.isFile()) {
                String redirect = null;
                if (path == null) {
                    redirect = request.getServletPath() + "/" + INDEX;
                } else {
                    redirect = request.getServletPath() + path + "/" + INDEX;
                }
                response.sendRedirect(redirect);
                file = new File(file, INDEX);
            } else {
                log.info("There is no index page for directory '{}' -- 404", file.getAbsolutePath());
                file = null;
            }
        }
        return file;
    }

}
