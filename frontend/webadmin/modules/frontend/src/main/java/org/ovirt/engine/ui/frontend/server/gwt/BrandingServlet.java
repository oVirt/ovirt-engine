package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.branding.BrandingManager;
import org.ovirt.engine.core.utils.servlet.ServletUtils;

/**
 * This class serves files from the branding themes to the browser. This
 * includes images, style sheets and other files. It provides ETags so
 * browsers can cache the output of the Servlet.
 */
public class BrandingServlet extends HttpServlet {

    private static final long serialVersionUID = 8687185074759812924L;

    /**
     * The logger object.
     */
    private static final Logger log = Logger.getLogger(BrandingServlet.class);

    /**
     * The branding manager, it resolves relative paths to the Servlet into
     * absolute paths on the file system.
     */
    private BrandingManager brandingManager;

    @Override
    public void init() {
        init(BrandingManager.getInstance());
    }

    void init(BrandingManager brandingManager) {
        this.brandingManager = brandingManager;
    }

    @Override
    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        // Get the requested path:
        String path = request.getPathInfo();

        // Locate the requested file:
        String fullPath = getFullPath(path);
        if (fullPath != null) {
            File file = new File(fullPath);
            if (!file.exists() || !file.canRead() || file.isDirectory()) {
                log.error("Unable to retrieve file: " + file.getAbsolutePath() //$NON-NLS-1$
                        + ", will send a 404 error response."); //$NON-NLS-1$
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                String etag = generateEtag(file);
                if (etag.equals(request.getHeader(GwtDynamicHostPageServlet.
                        IF_NONE_MATCH_HEADER))) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                } else {
                    String mime = ServletUtils.getMimeMap().
                            getContentType(file);
                    response.addHeader(GwtDynamicHostPageServlet.ETAG_HEADER,
                            etag);
                    // Send the content of the file:
                    ServletUtils.sendFile(request, response, file, mime);
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Generate an ETAG based on the file length, and last modified date
     * of the passed in file.
     * @param file The {@code File} object to check.
     * @return A {@code String} representing the ETag.
     */
    String generateEtag(final File file) {
        StringBuilder builder = new StringBuilder();
        // Start of ETag
        builder.append("W/\""); //$NON-NLS-1$
        builder.append(file.length());
        // Divider between length and last modified date.
        builder.append('-'); //$NON-NLS-1$
        builder.append(file.lastModified());
        // End of ETag
        builder.append("\""); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Translate the passed in path into a real file path so we can locate
     * the appropriate file.
     * @param path The path to translate.
     * @return A full absolute path for the passed in path.
     */
    String getFullPath(final String path) {
        String result = null;
        if (path != null && ServletUtils.isSane(path)) {
            // Return a result relative to the branding root path.
            result = brandingManager.getBrandingRootPath().getAbsolutePath() + path;
        } else {
            log.error("The path \"" + path + "\" is not sane"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

}
