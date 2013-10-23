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

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.EngineLocalConfig;

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
 * The uri can be '*' which means all URLs relative to the ForwardServlet context are used to determine
 * the path in the target servlet context. So if the ForwardServlet is mapped to /docs and the target context
 * is /ovirt-engine/docs. Then /docs/something gets forwarded to /ovirt-engine/docs/something. <br />
 * <br />
 * The targetContext param value can contain property expressions for expanding Engine property values in
 * form of %{PROP_NAME}.
 */
public class ForwardServlet extends HttpServlet {
    /**
     * The logger instance.
     */
    private static final Logger log = Logger.getLogger(ForwardServlet.class);

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
     * The URI parameter that will simply try to forward the request path to the new context without any additional
     * URI suffix.
     */
    public static final String ALL = "*"; //$NON-NLS-1$
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
        if (getServletConfig() != null) {
            // we use %{x} convention to avoid conflict with jboss properties
            targetContext = ServletUtils.getAsAbsoluteContext(getServletContext().getContextPath(),
                    EngineLocalConfig.getInstance().expandString(
                            config.getInitParameter(CONTEXT_PARAM).replaceAll("%\\{", "\\${")));
            uri = getInitParameter(URI_PARAM);
        }
        if (targetContext == null) {
            throw new ServletException("Target context not defined in web.xml"); //$NON-NLS-1$
        }
        if (uri == null) {
            uri = ALL;
        }
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
        final ServletContext forwardContext = getServletContext().getContext(targetContext);
        if (forwardContext != null) {
            String forwardUri = uri;
            if (ALL.equals(uri)) {
                forwardUri = request.getRequestURI();
            }
            final RequestDispatcher dispatcher = forwardContext.getRequestDispatcher(forwardUri);
            if (dispatcher != null) {
                for (String initParam : Collections.list(getInitParameterNames())) {
                    if (initParam.startsWith(ATTR_PREF)) {
                        request.setAttribute(initParam.replaceFirst(ATTR_PREF, ""), getInitParameter(initParam));
                    }
                }
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
