package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Servlet is used to forward requests from one context to another context. You can define the target context
 * using init parameters in web.xml. There are two init parameter keys that are important:
 * <ul>
 *   <li>targetContext: This is the context path to the target context</li>
 *   <li>uri: This is the URI relative to the target context path</li>
 * </ul>
 * So combined the targetContext and uri will define the URL of the target servlet.
 * <br />
 * For instance: <br />
 * &lt;init-param&gt; <br />
 * &nbsp;&nbsp;&lt;param-name&gt;targetContext&lt;/param-name&gt;<br />
 * &nbsp;&nbsp;&lt;param-value&gt;/ovirt-engine/services;&lt;/param-value&gt;<br />
 * &lt;/init-param&gt; <br />
 * &lt;init-param&gt; <br />
 * &nbsp;&nbsp;&lt;param-name&gt;uri&lt;/param-name&gt;<br />
 * &nbsp;&nbsp;&lt;param-value&gt;/pkiresource&lt;/param-value&gt;<br />
 * &lt;/init-param&gt; <br />
 * Any extra path associated with the URL (i.e. any path after the servlet context path)
 * will be appended to given URI relative to the target context path.
 * <br />
 * The parameters' value can contain property expressions for expanding Engine property values in
 * form of %{PROP_NAME}.
 */
public class ForwardServlet extends HttpServlet {
    /**
     * The logger instance.
     */
    private static final Logger log = LoggerFactory.getLogger(ForwardServlet.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -4938071390518075052L;

    /**
     * The key to use in web.xml to specify the target context.
     */
    public static final String CONTEXT_PARAM = "targetContext"; //$NON-NLS-1$
    /**
     * The key to use in web.xml to specify the target URI.
     */
    public static final String URI_PARAM = "uri"; //$NON-NLS-1$
    /**
     * Init param prefix for adding attributes.
     */
    public static final String ATTR_PREF = "attr-";

    /**
     * The target context to use when forwarding.
     */
    private String targetContext = null;
    /**
     * The target URI to use when forwarding.
     */
    private String uri = null;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);
        targetContext = config.getInitParameter(CONTEXT_PARAM);
        if (targetContext == null) {
            throw new ServletException("Target context not defined in web.xml"); //$NON-NLS-1$
        }
        uri = config.getInitParameter(URI_PARAM);
        if (uri == null) {
            throw new ServletException("Target URI not defined in web.xml"); //$NON-NLS-1$
        }

        // we use %{x} convention to avoid conflict with jboss properties
        EngineLocalConfig engineLocalConfig = EngineLocalConfig.getInstance();
        targetContext = ServletUtils.getAsAbsoluteContext(
            getServletContext().getContextPath(),
            engineLocalConfig.expandString(targetContext.replaceAll("%\\{", "\\${"))
        );
        uri = engineLocalConfig.expandString(uri.replaceAll("%\\{", "\\${"));
    }

    /**
     * Forward the request to the appropriate servlet in the context specified in the init method. This works
     * for all verbs due to the fact that RequestDispatcher.forward calls the 'service' method in the target
     * servlet, which determines which verb was used based on the request object.
     *
     * If the initialization of the {@code ForwardServlet} includes special parameters with a key starts with
     * 'attr-' those keys and values will be added to the request attributes for use in the target servlet. The
     * 'attr-' prefix will be stripped from attribute key.
     * For instance: <br />
     * &lt;init-param&gt; <br />
     * &nbsp;&nbsp;&lt;param-name&gt;attr-location&lt;/param-name&gt;<br />
     * &nbsp;&nbsp;&lt;param-value&gt;secret location&lt;/param-value&gt;<br />
     * &lt;/init-param&gt; <br />
     * Will results in a new attribute in the request object with the key 'location' and the value 'secret location'
     *
     * @param request The {@code HttpServletRequest} object
     * @param response The {@code HttpServletResponse} object
     * @throws IOException When underlying code throws IOException.
     * @throws ServletException When underlying code throws ServletException.
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {

        try {
            if (!request.getRequestURI().startsWith(request.getContextPath() + request.getServletPath())) {
                throw new RuntimeException("Unexpected request URI " + request.getRequestURI() +
                    " with servlet path " + request.getContextPath() + request.getServletPath());
            }

            final ServletContext forwardContext = getServletContext().getContext(targetContext);
            if (forwardContext == null) {
                throw new RuntimeException("Unable to determine forward context for " + targetContext);
            }

            final String forwardUri = uri + request.getRequestURI().substring(
                request.getContextPath().length() + request.getServletPath().length());
            final RequestDispatcher dispatcher = forwardContext.getRequestDispatcher(forwardUri);
            if (dispatcher == null) {
                throw new RuntimeException("Unable to determine dispatcher for " + forwardUri);
            }

            for (String initParam : Collections.list(getInitParameterNames())) {
                if (initParam.startsWith(ATTR_PREF)) {
                    request.setAttribute(initParam.replaceFirst(ATTR_PREF, ""), getInitParameter(initParam));
                }
            }
            dispatcher.forward(request, response);
        } catch(Exception e) {
            log.error("Forward failed", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

}
