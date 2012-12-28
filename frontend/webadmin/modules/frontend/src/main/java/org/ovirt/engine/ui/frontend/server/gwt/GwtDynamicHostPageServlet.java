package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
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

/**
 * Renders the HTML host page of a GWT application.
 * <p>
 * Embeds additional data (JavaScript objects) into the host page. By default, information about the currently logged-in
 * user is included via {@code userInfo} object.
 */
public abstract class GwtDynamicHostPageServlet extends HttpServlet {

    private static final long serialVersionUID = 3946034162721073929L;

    protected static final String ATTR_SELECTOR_SCRIPT = "selectorScript"; //$NON-NLS-1$
    protected static final String ATTR_USER_INFO = "userInfo"; //$NON-NLS-1$

    protected static final String IF_NONE_MATCH_HEADER = "If-None-Match"; //$NON-NLS-1$
    protected static final String ETAG_HEADER = "Etag"; //$NON-NLS-1$

    private static final String HOST_JSP = "/GwtHostPage.jsp"; //$NON-NLS-1$
    private static final String UTF_CONTENT_TYPE = "text/html; charset=UTF-8"; //$NON-NLS-1$

    private BackendLocal backend;
    private ObjectMapper mapper;

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    @Override
    public void init() {
        this.mapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Set attribute for selector script
        request.setAttribute(ATTR_SELECTOR_SCRIPT, getSelectorScriptName());

        // Set attribute for userInfo object
        VdcUser loggedInUser = getLoggedInUser(request.getSession().getId());
        if (loggedInUser != null) {
            request.setAttribute(ATTR_USER_INFO, getUserInfoObject(loggedInUser));
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * @return Name of the GWT selector script, e.g. {@code myapp.nocache.js}.
     */
    protected abstract String getSelectorScriptName();

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

    protected MessageDigest getMd5Digest(HttpServletRequest request) throws NoSuchAlgorithmException {
        MessageDigest digest = createMd5Digest();

        // Update based on selector script
        digest.update(request.getAttribute(ATTR_SELECTOR_SCRIPT).toString().getBytes());

        // Update based on userInfo object
        if (request.getAttribute(ATTR_USER_INFO) != null) {
            digest.update(request.getAttribute(ATTR_USER_INFO).toString().getBytes());
        }

        return digest;
    }

    protected MessageDigest createMd5Digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5"); //$NON-NLS-1$
    }

}
