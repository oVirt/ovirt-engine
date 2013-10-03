package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This Servlet is used to forward page not found requests to the root context 404 handler. This way other applications
 * don't have to worry about getting proper messages and branding, just forward to the 404 handler of the root
 * context.
 */
public class PageNotFoundForwardServlet extends HttpServlet {
    /**
     * The logger instance.
     */
    private static final Logger log = Logger.getLogger(PageNotFoundForwardServlet.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -4938071390518075052L;

    /**
     * The key to use in web.xml to specify the target context.
     */
    public static final String CONTEXT_PARAM = "targetContext"; //$NON-NLS-1$
    /**
     * The fall-back value if no target context is specified.
     */
    private static final String ROOT_CONTEXT = "/"; //$NON-NLS-1$
    /**
     * The key to use in web.xml to specify the page not found forward URI.
     */
    public static final String PAGE_NOT_FOUND_URI_PARAM = "pageNotFoundURI"; //$NON-NLS-1$
    /**
     * The fall-back value if no page not found URI is specified.
     */
    private static final String ROOT_404 = "/404.html"; //$NON-NLS-1$

    /**
     * The target context to use when forwarding.
     */
    private String targetContext = null;
    /**
     * The page not found URI to use when forwarding.
     */
    private String pageNotFoundURI = null;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);
        if (getServletConfig() != null) {
            targetContext = getInitParameter(CONTEXT_PARAM);
            pageNotFoundURI = getInitParameter(PAGE_NOT_FOUND_URI_PARAM);
        }
        if (targetContext == null) {
            //The target context is not provided in web.xml (maybe unit test?)
            targetContext = ROOT_CONTEXT;
        }
        if (pageNotFoundURI == null) {
            //The target URI is not provided in web.xml (maybe unit test?)
            pageNotFoundURI = ROOT_404;
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        //Forward to the page not found URI of the target context, which should have all the proper branding/messages.
        final ServletContext forwardContext = getServletContext().getContext(targetContext);
        if (forwardContext != null) {
            final RequestDispatcher dispatcher = forwardContext.getRequestDispatcher(pageNotFoundURI);
            if (dispatcher != null) {
                dispatcher.forward(request, response);
            } else {
                log.error("Unable to determine dispatcher");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "unable to determine dispatcher");
            }
        } else {
            log.error("Unable to determine forwarding context");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "unable to determine forward context");
        }
    }

}
