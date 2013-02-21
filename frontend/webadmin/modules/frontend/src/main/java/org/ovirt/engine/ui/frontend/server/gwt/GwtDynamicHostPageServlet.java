package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;

/**
 * Renders the HTML host page of a GWT application.
 * <p>
 * Allows to {@linkplain #writeAdditionalJsData include} additional data (JavaScript objects) into the host page. By
 * default, information about the currently logged-in user is included via {@code userInfo} object.
 * <p>
 * In order to prevent browsers from caching the GWT selector script, a dummy URL parameter with unique value is added
 * to the GWT selector script URL. This makes all GWT selector script requests unique from web resource point of view.
 * <p>
 * Note: this class resides in Frontend servlet package as it's already embedded in a JAR under WEB-INF/lib.
 */
public abstract class GwtDynamicHostPageServlet extends HttpServlet {

    private static final long serialVersionUID = 3946034162721073929L;

    private BackendLocal backend;

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
        response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$

        writer.append("<!DOCTYPE html><html><head>"); //$NON-NLS-1$
        writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"); //$NON-NLS-1$

        writer.append("<script type=\"text/javascript\">"); //$NON-NLS-1$
        writeAdditionalJsData(request, writer);
        writer.append("</script>"); //$NON-NLS-1$

        writer.append("</head><body>"); //$NON-NLS-1$
        writer.append("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>"); //$NON-NLS-1$
        writer.append("<script type=\"text/javascript\" src=\"" + getSelectorScriptName() + "?nocache=" + new Date().getTime() + "\"></script>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        writer.append("</body></html>"); //$NON-NLS-1$
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Writes additional data (JavaScript objects) into the host page.
     */
    protected void writeAdditionalJsData(HttpServletRequest request, PrintWriter writer) {
        VdcUser loggedUser = getLoggedInUser(request);

        if (loggedUser != null) {
            Map<String, String> userInfoData = new HashMap<String, String>();
            userInfoData.put("id", loggedUser.getUserId().toString()); //$NON-NLS-1$
            userInfoData.put("userName", loggedUser.getUserName()); //$NON-NLS-1$
            userInfoData.put("domain", loggedUser.getDomainControler()); //$NON-NLS-1$
            writeJsObject(writer, "userInfo", userInfoData); //$NON-NLS-1$
        }
    }

    /**
     * @return Name of the GWT selector script, e.g. {@code myapp.nocache.js}.
     */
    protected abstract String getSelectorScriptName();

    /**
     * Writes a string representing JavaScript object literal containing given attributes.
     */
    protected void writeJsObject(PrintWriter writer, String objectName, Map<String, String> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(objectName).append(" = { "); //$NON-NLS-1$ //$NON-NLS-2$

        int countdown = attributes.size();
        for (Entry<String, String> e : attributes.entrySet()) {
            appendJsObjectAttribute(sb, e.getKey(), e.getValue(), true);

            if (--countdown > 0) {
                sb.append(", "); //$NON-NLS-1$
            }
        }

        sb.append(" };"); //$NON-NLS-1$
        writer.append(sb.toString());
    }

    protected void appendJsObjectAttribute(StringBuilder sb, String name, String value, boolean quoteValue) {
        sb.append(name).append(": "); //$NON-NLS-1$
        sb.append(quoteValue ? "\"" + value + "\"" : value); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void initQueryParams(VdcQueryParametersBase queryParams, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        queryParams.setSessionId(sessionId);
        queryParams.setHttpSessionId(sessionId);
        queryParams.setFiltered(false);
    }

    /**
     * Executes a backend {@linkplain BackendLocal#RunQuery query} and returns its result value if successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams, HttpServletRequest request) {
        initQueryParams(queryParams, request);
        VdcQueryReturnValue result = backend.RunQuery(queryType, queryParams);

        if (result.getSucceeded()) {
            return result.getReturnValue();
        } else {
            return null;
        }
    }

    /**
     * Executes a backend {@linkplain BackendLocal#RunPublicQuery public query} and returns its result value if
     * successful.
     * <p>
     * Returns {@code null} otherwise.
     */
    protected Object runPublicQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams,
            HttpServletRequest request) {
        initQueryParams(queryParams, request);
        VdcQueryReturnValue result = backend.RunPublicQuery(queryType, queryParams);

        if (result.getSucceeded()) {
            return result.getReturnValue();
        } else {
            return null;
        }
    }

    private VdcUser getLoggedInUser(HttpServletRequest request) {
        return (VdcUser) runQuery(VdcQueryType.GetUserBySessionId, new VdcQueryParametersBase(), request);
    }

}
