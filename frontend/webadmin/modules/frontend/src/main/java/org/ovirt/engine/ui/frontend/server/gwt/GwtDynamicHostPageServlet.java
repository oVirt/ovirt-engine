package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.branding.BrandingFilter;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;
import org.ovirt.engine.core.utils.servlet.ServletUtils;
import org.ovirt.engine.core.utils.servlet.UnsupportedLocaleHelper;

/**
 * Renders the HTML host page of a GWT application.
 * <p>
 * Embeds additional data (JavaScript objects) into the host page.
 * By default, information about the currently logged-in user is included via {@code userInfo} object.
 */
public abstract class GwtDynamicHostPageServlet extends HttpServlet {

    /**
     * Request attributes that participate in MD5 checksum calculation.
     */
    public enum MD5Attributes {

        ATTR_SELECTOR_SCRIPT("selectorScript"), //$NON-NLS-1$
        ATTR_USER_INFO("userInfo"), //$NON-NLS-1$
        ATTR_STYLES("brandingStyle"), //$NON-NLS-1$
        ATTR_MESSAGES("messages"), //$NON-NLS-1$
        ATTR_BASE_CONTEXT_PATH("baseContextPath"), //$NON-NLS-1$
        ATTR_LOCALE(LocaleFilter.LOCALE),
        ATTR_APPLICATION_TYPE(BrandingFilter.APPLICATION_NAME),
        ATTR_DISPLAY_LOCALES("visibleLocales"), //$NON-NLS-1$
        ATTR_ENGINE_RPM_VERSION("engineRpmVersion"), //$NON-NLS-1$
        ATTR_DISPLAY_UNCAUGHT_UI_EXCEPTIONS("DISPLAY_UNCAUGHT_UI_EXCEPTIONS"); //$NON-NLS-1$

        private final String attributeKey;

        MD5Attributes(String key) {
            this.attributeKey = key;
        }

        /**
         * Get the key associated with this attribute.
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
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        final String engineSessionId = getEngineSessionId(request);

        // Set attribute for selector script
        request.setAttribute(MD5Attributes.ATTR_SELECTOR_SCRIPT.getKey(), getSelectorScriptName());

        // Set the messages that need to be replaced.
        request.setAttribute(MD5Attributes.ATTR_MESSAGES.getKey(),
                getBrandingMessages(getApplicationTypeFromRequest(request), getLocaleFromRequest(request)));
        request.setAttribute(MD5Attributes.ATTR_BASE_CONTEXT_PATH.getKey(),
                getValueObject(ServletUtils.getBaseContextPath(request)));
        request.setAttribute(MD5Attributes.ATTR_DISPLAY_LOCALES.getKey(), getValueObject(
                StringUtils.join(UnsupportedLocaleHelper.getDisplayedLocales(LocaleFilter.getLocaleKeys()), ","))); //$NON-NLS-1$
        request.setAttribute(MD5Attributes.ATTR_DISPLAY_UNCAUGHT_UI_EXCEPTIONS.getKey(),
                getDisplayUncaughtUIExceptions() ? BooleanNode.TRUE : BooleanNode.FALSE);

        // Set attributes for userInfo object
        DbUser loggedInUser = getLoggedInUser(engineSessionId);
        if (loggedInUser != null) {
            String ssoToken = getSsoToken(engineSessionId);
            request.setAttribute(MD5Attributes.ATTR_USER_INFO.getKey(),
                    getUserInfoObject(loggedInUser, engineSessionId, ssoToken));
        }

        // Set attribute for engineRpmVersion object
        String engineRpmVersion = getEngineRpmVersion(engineSessionId);
        request.setAttribute(MD5Attributes.ATTR_ENGINE_RPM_VERSION.getKey(),
                getValueObject(engineRpmVersion));

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

    protected String getEngineSessionId(final HttpServletRequest request) {
        return (String) request.getSession().getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
    }

    private String getSsoToken(final String engineSessionId) {
        return (String) runQuery(VdcQueryType.GetEngineSessionIdToken, new VdcQueryParametersBase(), engineSessionId);
    }

    protected Boolean getDisplayUncaughtUIExceptions() {
        return Config.<Boolean> getValue(ConfigValues.DisplayUncaughtUIExceptions);
    }

    /**
     * Retrieves the application type from the request object, this can return null if the
     * attribute containing the application type is empty.
     * @param request The {@code HttpServletRequest} object.
     * @return A string containing the application type.
     */
    private String getApplicationTypeFromRequest(final HttpServletRequest request) {
        return (String) request.getAttribute(BrandingFilter.APPLICATION_NAME);
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
     * Create a Javascript value object with the value being the passed in value.
     * @param value The {@code String} value to use as the value of the object.
     * @return A String representation of the Javascript object.
     */
    protected String getValueObject(final String value) {
        ObjectNode node = mapper.createObjectNode();
        node.put("value", value); //$NON-NLS-1$
        return node.toString();
    }

    /**
     * Get a JavaScript associative array string that define the branding messages.
     * @param applicationName the application name.
     * @param locale {@code Locale} to use to look up the messages.
     * @return The messages as a {@code String}
     */
    private String getBrandingMessages(final String applicationName, final Locale locale) {
        return brandingManager.getMessages(applicationName, locale);
    }

    /**
     * @return {@code true} if all queries should be filtered according to user permissions, {@code false} otherwise.
     */
    protected abstract boolean filterQueries();

    protected void initQueryParams(VdcQueryParametersBase queryParams, String sessionId) {
        queryParams.setSessionId(sessionId);
        queryParams.setFiltered(filterQueries());
    }

    /**
     * Executes a backend {@linkplain BackendLocal#runQuery query} and returns its result value if successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams, String sessionId) {
        initQueryParams(queryParams, sessionId);
        VdcQueryReturnValue result = backend.runQuery(queryType, queryParams);
        return result != null && result.getSucceeded() ? result.getReturnValue() : null;
    }

    /**
     * Executes a backend {@linkplain BackendLocal#runPublicQuery public query} and returns its result value if
     * successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runPublicQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams, String sessionId) {
        initQueryParams(queryParams, sessionId);
        VdcQueryReturnValue result = backend.runPublicQuery(queryType, queryParams);
        return result != null && result.getSucceeded() ? result.getReturnValue() : null;
    }

    protected ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    protected ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    protected DbUser getLoggedInUser(String sessionId) {
        return (DbUser) runQuery(VdcQueryType.GetUserBySessionId, new VdcQueryParametersBase(), sessionId);
    }

    // TODO Engine session ID is required until the Reports portal is integrated with Engine SSO infra
    protected ObjectNode getUserInfoObject(DbUser loggedInUser, String engineSessionId, String ssoToken) {
        ObjectNode obj = createObjectNode();
        obj.put("id", loggedInUser.getId().toString()); //$NON-NLS-1$
        obj.put("userName", loggedInUser.getLoginName()); //$NON-NLS-1$
        obj.put("domain", loggedInUser.getDomain()); //$NON-NLS-1$
        obj.put("isAdmin", loggedInUser.isAdmin()); //$NON-NLS-1$
        obj.put("engineSessionId", engineSessionId); //$NON-NLS-1$
        obj.put("ssoToken", ssoToken); //$NON-NLS-1$
        return obj;
    }

    protected String getMd5Sum(HttpServletRequest request) throws NoSuchAlgorithmException,
        UnsupportedEncodingException {
        return new HexBinaryAdapter().marshal(getMd5Digest(request).digest());
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
    protected MessageDigest getMd5Digest(HttpServletRequest request)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = createMd5Digest();
        for (MD5Attributes attribute: MD5Attributes.values()) {
            if (request.getAttribute(attribute.getKey()) != null) {
                digest.update(request.getAttribute(attribute.getKey()).toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        return digest;
    }

    protected MessageDigest createMd5Digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5"); //$NON-NLS-1$
    }

    protected String getEngineRpmVersion(String sessionId) {
        return (String) runPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion,
                        ConfigCommon.defaultConfigurationVersion), sessionId);
    }

}
