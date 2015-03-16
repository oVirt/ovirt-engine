package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * This servlet redirects a request to another URL which is defined as an init param. The key of the init param is
 * url.
 * <br />
 * For instance: <br />
 * &lt;init-param&gt; <br />
 * &nbsp;&nbsp;&lt;param-name&gt;url&lt;/param-name&gt;<br />
 * &nbsp;&nbsp;&lt;param-value&gt;/ovirt-engine/&lt;/param-value&gt;<br />
 * &lt;/init-param&gt; <br />
 * The url param value can contain property expressions for expanding Engine property values in
 * form of %{PROP_NAME}.
 */
public class RedirectServlet extends HttpServlet {
    private static final long serialVersionUID = -1794616863361241804L;

    private static final String url404Default = EngineLocalConfig.getInstance().getEngineURI() + "/404.html";
    private static final Pattern urlPattern = Pattern.compile(
        "((?<prefix>(\\w*://)([^/]*?@)?)?(?<host>(\\[[\\da-fA-F:]+\\])|([^/:]+)?))?(?<suffix>.*)"
    );

    private static final String LOCALHOST_PLACEHOLDER = "@__LOCALHOST__@";
    private static final String TARGET_URL = "targetUrl";
    private static final String URL404 = "url404";
    private static final Set<String> localHostNames = new HashSet<>(Arrays.asList(
        "localhost",
        "localhost.localdomain",
        "localhost4",
        "localhost4.localdomain",
        "localhost4.localdomain4",
        "localhost6",
        "localhost6.localdomain",
        "localhost6.localdomain6",
        "ip4-localhost",
        "ip4-loopback",
        "ip6-localhost",
        "ip6-loopback"
    ));

    private String targetUrl;
    private String url404;

    private String injectLocalHost(String url) {
        Matcher m = urlPattern.matcher(url);
        if (m.matches()) {
            String host = m.group("host");
            if (host != null) {
                if (localHostNames.contains(host) || host.startsWith("127.") || host.equals("::1")) {
                    url = String.format(
                        "%s%s%s",
                        m.group("prefix") != null ? m.group("prefix") : "",
                        LOCALHOST_PLACEHOLDER,
                        m.group("suffix") != null ? m.group("suffix") : ""
                    );
                }
            }
        }
        return url;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // we use %{x} convention to avoid conflict with jboss properties
        targetUrl = injectLocalHost(EngineLocalConfig.getInstance().expandString(
                config.getInitParameter(TARGET_URL).replaceAll("%\\{", "\\${")));
        url404 = url404Default;
        if (StringUtils.isNotEmpty(config.getInitParameter(URL404))) {
            url404 = config.getInitParameter(URL404);
        }
        url404 = injectLocalHost(EngineLocalConfig.getInstance().expandString(url404.replaceAll("%\\{", "\\${")));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String redirectUrl = targetUrl;
        if (StringUtils.isNotEmpty(redirectUrl)) {
            String pathInfo = request.getPathInfo();
            if (StringUtils.isNotEmpty(pathInfo)) {
                redirectUrl += pathInfo;
            }
            String queryString = request.getQueryString();
            if (StringUtils.isNotEmpty(queryString)) {
                if (redirectUrl.indexOf("?") == -1) {
                    redirectUrl += "?";
                } else {
                    redirectUrl += "&";
                }
                redirectUrl += queryString;
            }
        } else {
            redirectUrl = url404;
        }
        String localhost = request.getHeader("Host");
        if (localhost != null) {
            if (localhost.charAt(0) == '[') {
                int n = localhost.indexOf("]");
                if (n != -1) {
                    localhost = localhost.substring(0, n+1);
                }
            } else {
                int n = localhost.indexOf(":");
                if (n != -1) {
                    localhost = localhost.substring(0, n);
                }
            }
        } else {
            localhost = EngineLocalConfig.getInstance().getHost();
        }
        response.sendRedirect(redirectUrl.replace(LOCALHOST_PLACEHOLDER, localhost));
    }
}
