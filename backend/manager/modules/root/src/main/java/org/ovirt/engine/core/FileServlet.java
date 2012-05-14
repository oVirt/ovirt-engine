package org.ovirt.engine.core;

import static org.ovirt.engine.core.ServletUtils.sendFile;

import java.io.File;
import java.io.IOException;

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
 * This can be useful when the web application has to serve files that are contained
 * in external directories.
 */
public class FileServlet extends HttpServlet {
    // Serialization id:
    private static final long serialVersionUID = -1794616863361641804L;

    // The log:
    private static final Logger log = Logger.getLogger(FileServlet.class);

    // The names of the parameters:
    private static final String TYPE = "type";
    private static final String FILE = "file";

    // The values of the parameters:
    private String type;
    private File file;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);

        // Get the content type and the name of the file:
        type = config.getInitParameter(TYPE);
        if (type == null) {
            final String message = "Can't get content type from parameter \"" + TYPE + "\".";
            log.error(message);
            throw new ServletException(message);
        }
        final String name = config.getInitParameter(FILE);
        if (name == null) {
            final String message = "Can't get file name from parameter \"" + FILE + "\".";
            log.error(message);
            throw new ServletException(message);
        }
        file = new File(name);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendFile(request, response, file, type);
    }
}
