package org.ovirt.engine.core.branding;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.servlet.ServletUtils;

/**
 * <p>Serves non-CSS-based resource files (images, PDFs, or anything else you'd
 * want to cascade) from the branding themes to the browser. It provides ETags so
 * browsers can cache the output of the Servlet.
 * </p>
 * <p>
 * Resources are served in a cascading manner such that the highest theme with
 * a copy of the resource "wins" and has its resource served.
 * </p>
 */
public class BrandingCascadingResourceServlet extends HttpServlet {

    private static final long serialVersionUID = -8873220859154328909L;

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

        String resourceName = request.getPathInfo().replaceFirst("^\\/", "");  // strip preceding slash
        CascadingResource resource = brandingManager.getCascadingResource(resourceName);

        // serve the file
        if (resource == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            ServletUtils.sendFile(request, response, resource.getFile(), resource.getContentType());
        }
    }

}
