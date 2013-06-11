package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.branding.BrandingManager;
import org.ovirt.engine.core.utils.branding.BrandingTheme;
import org.ovirt.engine.core.utils.branding.BrandingTheme.ApplicationType;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

/**
 * Renders the HTML host page of a GWT application.
 * <p>
 * Embeds additional data (JavaScript objects) into the host page.
 * By default, information about the currently logged-in user is included via {@code userInfo} object.
 */
public abstract class GwtDynamicHostPageServlet extends HttpServlet {

    /**
     * The values of this enum are used by the MD5 sum calculation to
     * determine if the values have changed.
     */
    public enum MD5Attributes {
        ATTR_SELECTOR_SCRIPT("selectorScript"), //$NON-NLS-1$
        ATTR_USER_INFO("userInfo"), //$NON-NLS-1$
        ATTR_STYLES("brandingStyle"), //$NON-NLS-1$
        ATTR_MESSAGES("messages"), //$NON-NLS-1$
        ATTR_LOCALE(LocaleFilter.LOCALE),
        ATTR_APPLICATION_TYPE("applicationType"); //$NON-NLS-1$

        private String attributeKey;

        /**
         * Constructor for enum.
         * @param key The key to store
         */
        private MD5Attributes(final String key) {
            this.attributeKey = key;
        }

        /**
         * Get the value of the attribute key.
         * @return A {@code String} containing the key.
         */
        public String getKey() {
            return attributeKey;
        }
    }

    private static final long serialVersionUID = 3946034162721073929L;

    public static final String IF_NONE_MATCH_HEADER = "If-None-Match"; //$NON-NLS-1$
    public static final String ETAG_HEADER = "Etag"; //$NON-NLS-1$

    private static final String HOST_JSP = "/GwtHostPage.jsp"; //$NON-NLS-1$
    private static final String UTF_CONTENT_TYPE = "text/html; charset=UTF-8"; //$NON-NLS-1$

    private BackendLocal backend;

    private ObjectMapper mapper;
    private BrandingManager brandingManager;

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    @Override
    public void init() {
        init(new ObjectMapper(), BrandingManager.getInstance());
    }

    void init(ObjectMapper mapper, BrandingManager brandingManager) {
        this.mapper = mapper;
        this.brandingManager = brandingManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        List<BrandingTheme> brandingThemes;

        // Set attribute for selector script
        request.setAttribute(MD5Attributes.ATTR_SELECTOR_SCRIPT.getKey(), getSelectorScriptName());
        // Set attribute for themes.
        brandingThemes = brandingManager.getBrandingThemes();
        request.setAttribute(MD5Attributes.ATTR_STYLES.getKey(), brandingThemes);
        // Set the messages that need to be replaced.
        request.setAttribute(MD5Attributes.ATTR_MESSAGES.getKey(), getBrandingMessages(getLocaleFromRequest(request)));
        // Set class of servlet
        request.setAttribute(MD5Attributes.ATTR_APPLICATION_TYPE.getKey(), getApplicationType());
        // Set attribute for userInfo object
        VdcUser loggedInUser = getLoggedInUser(request.getSession().getId());
        if (loggedInUser != null) {
            request.setAttribute(MD5Attributes.ATTR_USER_INFO.getKey(), getUserInfoObject(loggedInUser));
        }

        try {
            // Calculate MD5 for use with If-None-Match request header
            String md5sum = getMd5Sum(request);

            if (request.getHeader(IF_NONE_MATCH_HEADER) != null
                    && request.getHeader(IF_NONE_MATCH_HEADER).equals(md5sum)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(HOST_JSP);
                response.setContentType(UTF_CONTENT_TYPE);
                response.addHeader(ETAG_HEADER, md5sum);
                if (dispatcher != null) {
                    dispatcher.include(request, response);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
        IOException {
        doGet(req, resp);
    }

    /**
     * @return Name of the GWT selector script, e.g. {@code myapp.nocache.js}.
     */
    protected abstract String getSelectorScriptName();

    /**
     * Get the user locale from the request. The {@code LocaleFilter} should have populated the value.
     * @param request {@code ServletRequest} that contains the locale used to look up the messages.
     * @return The {@code Locale} from the request. if not found defaults to Locale.US
     */
    private Locale getLocaleFromRequest(final ServletRequest request) {
        Locale locale = (Locale) request.getAttribute(LocaleFilter.LOCALE); //$NON-NLS-1$
        if (locale == null) {
            // If no locale defined, default back to the default.
            locale = LocaleFilter.DEFAULT_LOCALE;
        }
        return locale;
    }

    /**
     * Get a JavaScript associative array string that define the branding messages.
     * @param locale {@code Locale} to use to look up the messages.
     * @return The messages as a {@code String}
     */
    private String getBrandingMessages(final Locale locale) {
        return brandingManager.getMessages(getApplicationType().getPrefix(), locale);
    }

    /**
     * Get the application type the Servlet is serving for instance web admin or user portal.
     * @return A {@code ApplicationType} defining the type of application served.
     */
    protected abstract ApplicationType getApplicationType();

    /**
     * @return {@code true} if all queries should be filtered according to user permissions, {@code false} otherwise.
     */
    protected abstract boolean filterQueries();

    protected void initQueryParams(VdcQueryParametersBase queryParams, String sessionId) {
        queryParams.setSessionId(sessionId);
        queryParams.setHttpSessionId(sessionId);
        queryParams.setFiltered(filterQueries());
    }

    /**
     * Executes a backend {@linkplain BackendLocal#RunQuery query} and returns its result value if successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams, String sessionId) {
        initQueryParams(queryParams, sessionId);
        VdcQueryReturnValue result = backend.RunQuery(queryType, queryParams);
        return result.getSucceeded() ? result.getReturnValue() : null;
    }

    /**
     * Executes a backend {@linkplain BackendLocal#RunPublicQuery public query} and returns its result value if
     * successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runPublicQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams, String sessionId) {
        initQueryParams(queryParams, sessionId);
        VdcQueryReturnValue result = backend.RunPublicQuery(queryType, queryParams);
        return result.getSucceeded() ? result.getReturnValue() : null;
    }

    protected ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    protected ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    protected VdcUser getLoggedInUser(String sessionId) {
        return (VdcUser) runQuery(VdcQueryType.GetUserBySessionId, new VdcQueryParametersBase(), sessionId);
    }

    protected ObjectNode getUserInfoObject(VdcUser loggedInUser) {
        ObjectNode obj = createObjectNode();
        obj.put("id", loggedInUser.getUserId().toString()); //$NON-NLS-1$
        obj.put("userName", loggedInUser.getUserName()); //$NON-NLS-1$
        obj.put("domain", loggedInUser.getDomainControler()); //$NON-NLS-1$
        return obj;
    }

    protected String getMd5Sum(HttpServletRequest request) throws NoSuchAlgorithmException {
        return (new HexBinaryAdapter()).marshal(getMd5Digest(request).digest());
    }

    /**
     * Calculate an MD5 sum to be used as an ETag in the request header.
     * The attributes come from the request scope and they keys are
     * determine by the {@code MD5Attributes} enum.
     *
     * @param request The {@code HttpServletRequest} to use as the source
     * of the attribute values.
     * @return A {@code MessageDigest} which will be used to generate the
     * string representation of the MD5 sum.
     * @throws NoSuchAlgorithmException If the method cannot create the digest
     * object.
     */
    protected MessageDigest getMd5Digest(final HttpServletRequest request)
            throws NoSuchAlgorithmException {
        MessageDigest digest = createMd5Digest();
        for (MD5Attributes attribute: MD5Attributes.values()) {
            if (request.getAttribute(attribute.getKey()) != null) {
                digest.update(request.getAttribute(attribute.getKey()).
                        toString().getBytes());
            }
        }
        return digest;
    }

    protected MessageDigest createMd5Digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5"); //$NON-NLS-1$
    }

}
