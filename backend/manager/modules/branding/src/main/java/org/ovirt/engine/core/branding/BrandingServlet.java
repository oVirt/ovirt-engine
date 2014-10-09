package org.ovirt.engine.core.branding;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(BrandingServlet.class);

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

        // serve the file
        ServletUtils.sendFile(request, response,
            getFile(brandingManager.getBrandingRootPath(), request.getPathInfo()), null);
    }

    /**
     * Translate the passed in path into a real file path so we can locate
     * the appropriate file.
     * @param brandingRootPath The path to the root of the branding. Cannot be null
     * @param path The path to translate.
     * @return A full absolute path for the passed in path.
     */
    File getFile(final File brandingRootPath, final String path) {
        File result = null;
        String mergedPath = new File(brandingRootPath.getAbsolutePath(), path == null ? "": path).getAbsolutePath();
        if (path != null && ServletUtils.isSane(mergedPath)) {
            // Return a result relative to the branding root path.
            result = new File(mergedPath);
        } else {
            log.error("The path '{}' is not sane", mergedPath); //$NON-NLS-1$
        }
        return result;
    }

}
