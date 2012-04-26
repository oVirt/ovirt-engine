package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

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

    private static final long serialVersionUID = 1L;

    private BackendLocal backend;

    @EJB(beanInterface = BackendLocal.class, mappedName = "java:global/engine/engine-bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("text/html"); //$NON-NLS-1$
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

        writer.append("<!DOCTYPE html><html><head>"); //$NON-NLS-1$
        writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"); //$NON-NLS-1$
        writer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\"/>"); //$NON-NLS-1$

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

    protected void writeAdditionalJsData(HttpServletRequest request, PrintWriter writer) {
        VdcUser loggedUser = getLoggedInUser(request);

        if (loggedUser != null) {
            writer.append(" var userInfo = { "); //$NON-NLS-1$
            writer.append(" \"id\" : \"" + loggedUser.getUserId().toString() + "\","); //$NON-NLS-1$ //$NON-NLS-2$
            writer.append(" \"userName\" : \"" + loggedUser.getUserName() + "\","); //$NON-NLS-1$ //$NON-NLS-2$
            writer.append(" \"domain\" : \"" + loggedUser.getDomainControler() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            writer.append(" };"); //$NON-NLS-1$
        }
    }

    /**
     * @return Name of the GWT selector script, e.g. {@code myapp.nocache.js}.
     */
    protected abstract String getSelectorScriptName();

    private VdcUser getLoggedInUser(HttpServletRequest request) {
        VdcQueryReturnValue returnValue = backend.RunQuery(VdcQueryType.GetUserBySessionId,
                createQueryParams(request.getSession().getId(), filterQueries()));

        if (returnValue.getSucceeded()) {
            return (VdcUser) returnValue.getReturnValue();
        } else {
            return null;
        }
    }

    protected VdcQueryParametersBase createQueryParams(String sessionId, boolean isFiltered) {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(sessionId);
        queryParams.setHttpSessionId(sessionId);
        queryParams.setFiltered(isFiltered);
        return queryParams;
    }

    protected boolean filterQueries() {
        return false;
    }

}
