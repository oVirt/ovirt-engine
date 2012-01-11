package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.PrintWriter;

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
 * Renders HTML host page used to load GWT web applications.
 * <p>
 * Allows inclusion of additional data into the host page, such as the information about the currently logged-in user.
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
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        writer.append("<!DOCTYPE html><html><head>");
        writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
        writer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\"/>");
        writer.append("<title>" + getPageTitle() + "</title>");

        writer.append("<script type=\"text/javascript\">");
        writeAdditionalJsData(request, writer);
        writer.append("</script>");

        writer.append("</head><body>");
        writer.append("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>");
        writer.append("<script type=\"text/javascript\" src=\"" + getSelectorScriptName() + "\"></script>");
        writer.append("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    protected void writeAdditionalJsData(HttpServletRequest request, PrintWriter writer) {
        VdcUser loggedUser = getLoggedInUser(request.getSession().getId());

        if (loggedUser != null) {
            writer.append(" var userInfo = { ");
            writer.append(" \"id\" : \"" + loggedUser.getUserId().toString() + "\",");
            writer.append(" \"userName\" : \"" + loggedUser.getUserName() + "\",");
            writer.append(" \"domain\" : \"" + loggedUser.getDomainControler() + "\"");
            writer.append(" };");
        }
    }

    /**
     * @return HTML page title.
     */
    protected abstract String getPageTitle();

    /**
     * @return Name of the GWT selector script, e.g. {@code myapp.nocache.js}.
     */
    protected abstract String getSelectorScriptName();

    private VdcUser getLoggedInUser(String sessionId) {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(sessionId);
        queryParams.setHttpSessionId(sessionId);

        VdcQueryReturnValue vqrv = backend.RunQuery(
                VdcQueryType.GetUserBySessionId, queryParams);

        if (!vqrv.getSucceeded()) {
            return null;
        } else if (vqrv.getSucceeded()) {
            if (vqrv.getReturnValue() == null)
                return null;
            return (VdcUser) vqrv.getReturnValue();
        } else {
            // For unknown reason the result was failed be returned.
            return null;
        }
    }

}
