package org.ovirt.engine.core;

import static org.ovirt.engine.core.ServletUtils.sendFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This is a simple servlet that generates the web-conf.js script from the
 * configuration.
 */
public class WebConfServlet extends HttpServlet {
    // Serialization id:
    private static final long serialVersionUID = 6242595311775511209L;

    // The log:
    private static final Logger log = Logger.getLogger(WebConfServlet.class);

    // File location and content type:
    private static final String PATH = "/etc/ovirt-engine/web-conf.js";
    private static final String TYPE = "application/javascript";

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);

        // Check that the file exists and is readable, just to generate
        // a warning, the check will be repeated later with each access:
        final File file = new File(PATH);
        if (!file.exists() || !file.canRead()) {
            log.warn("File \"" + file.getAbsolutePath() + "\" doesn't exist or isn't readable.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If the file exists then send the content directly:
        final File file = new File(PATH);
        if (file.exists() && file.canRead()) {
            sendFile(request, response, file, TYPE);
            return;
        }
        log.warn("File \"" + file.getAbsolutePath() + "\" doesn't exist or isn't readable.");

        // Try to guess the host and port numbers (this is just guessing and
        // will fail if using non standard ports):
        final String host = request.getServerName();
        int httpPort = 8080;
        int httpsPort = 8443;
        if (request.isSecure()) {
            httpsPort = request.getServerPort();
            if (httpsPort == 443) {
                httpPort = 80;
            }
        }
        else {
            httpPort = request.getServerPort();
            if (httpPort == 80) {
                httpsPort = 443;
            }
        }
        log.warn("Using host \"" + host + "\" and ports " + httpPort + " and " + httpsPort + ".");

        // Generate the content:
        response.setContentType(TYPE);
        final PrintWriter out = response.getWriter();
        out.printf("var host_fqdn = \"%s\"\n", host);
        out.printf("var http_port = \"%d\"\n", httpPort);
        out.printf("var https_port = \"%d\"\n", httpsPort);
        out.printf("var http_url = \"http://%s:%d\"\n", host, httpPort);
        out.printf("var https_url = \"https://%s:%d\"\n", host, httpsPort);
    }
}
