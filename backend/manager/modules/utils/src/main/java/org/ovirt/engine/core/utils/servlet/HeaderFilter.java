package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter automatically adds headers to the response of a {@code HttpServletResponse}. To enable
 * this filter you must add the following to your web.xml
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;HeaderFilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.ovirt.engine.core.utils.servlet.HeaderFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 *
 * &lt;filter-mapping&gt;
 *   &lt;filter-name&gt;HeaderFilter&lt;/filter-name&gt;
 *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 *
 * </pre>
 * There are 3 default headers that are added to the response.
 * <ul>
 *   <li>X-frame-options (To prevent click jacking)</li>
 *   <li>X-content-type-options (To prevent mime sniffing attacks)</li>
 *   <li>X-XSS-protection (To enable IE XSS filter)</li>
 * </ul>
 * It is possible to override the default values for these by adding an init param to the filter definition in your
 * web.xml. For instance:
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;HeaderFilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.ovirt.engine.core.utils.servlet.HeaderFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;X-frame-options&lt;/param-name&gt;
 *     &lt;param-value&gt;DENY&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * This will set the value to DENY instead of the default SAMEORIGIN. Using the same technique one can add any
 * number of extra headers.
 */
public class HeaderFilter implements Filter {

    /**
     * X-frame-options string constant.
     */
    private static final String X_FRAME_OPTIONS = "X-FRAME-OPTIONS"; //$NON-NLS-1$
    /**
     * X-frame-options default value string constant.
     */
    // This has to be SAMEORIGIN otherwise GWT won't work.
    private static final String X_FRAME_OPTIONS_DEFAULT = "SAMEORIGIN"; //$NON-NLS-1$

    /**
     * X-content-type-options string constant.
     */
    private static final String X_CONTENT_TYPE_OPTIONS = "X-CONTENT-TYPE-OPTIONS"; //$NON-NLS-1$
    /**
     * X-content-type-options default value string constant.
     */
    private static final String X_CONTENT_TYPE_OPTIONS_DEFAULT = "NOSNIFF"; //$NON-NLS-1$

    /**
     * X-XSS-protection string constant.
     */
    private static final String X_XSS_PROTECTION = "X-XSS-PROTECTION"; //$NON-NLS-1$
    /**
     * X-XSS-protection default value string constant.
     */
    private static final String X_XSS_PROTECTION_DEFAULT = "1; MODE=BLOCK"; //$NON-NLS-1$

    /**
     * The map that contains the header name and value pairs to be added to each response.
     */
    private final Map<String, String> headerValueMap = new HashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        for (Map.Entry<String, String> entry: headerValueMap.entrySet()) {
            res.addHeader(entry.getKey(), entry.getValue());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Enumeration<String> headerNames = filterConfig.getInitParameterNames();
        addDefaultsToMap();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            //This allows for overriding the defaults.
            headerValueMap.put(name.toUpperCase(), filterConfig.getInitParameter(name));
        }
    }

    /**
     * These are headers each response should have.
     */
    private void addDefaultsToMap() {
        headerValueMap.put(X_FRAME_OPTIONS, X_FRAME_OPTIONS_DEFAULT);
        headerValueMap.put(X_CONTENT_TYPE_OPTIONS, X_CONTENT_TYPE_OPTIONS_DEFAULT);
        headerValueMap.put(X_XSS_PROTECTION, X_XSS_PROTECTION_DEFAULT);
    }

    @Override
    public void destroy() {
        // Do nothing.
    }

}
