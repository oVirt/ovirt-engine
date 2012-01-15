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
 * A landing servlet for WebAdmin project
 * <p>
 * note: this page resides in frontend servlet package as it's already embedded as a jar under WEB-INF/lib of the
 * webadmin WAR
 * </p>
 *
 * @author Asaf Shakarchi
 */
public class WebadminDynamicHostingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BackendLocal backend;

    // TODO: Should only set an html element that will be replaced by GWT widget instead.
    String title = "oVirt Enterprise Virtualization Engine Web Administration";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        writer.append("<!DOCTYPE html><html><head>");
        writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
        writer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\"/>");
        writer.append("<title>" + title + "</title>");
        VdcUser loggedUser = getLoggedInUser(req.getSession().getId());
        if (loggedUser != null) {
            writer.append("<script type=\"text/javascript\">");
            writer.append(" var userInfo = { ");
            writer.append(" \"id\" : \"" + loggedUser.getUserId().toString() + "\",");
            writer.append(" \"userName\" : \"" + loggedUser.getUserName() + "\",");
            writer.append(" \"domain\" : \"" + loggedUser.getDomainControler() + "\"");
            writer.append(" };");
            writer.append("</script>");
        }

        writer.append("</head><body>");

        writer.append("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>");
        writer.append("<script type=\"text/javascript\" src=\"webadmin.nocache.js\"></script>");

        writer.append("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    public VdcUser getLoggedInUser(String sessionId) {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(sessionId);
        queryParams.setHttpSessionId(sessionId);

        VdcQueryReturnValue vqrv = backend.RunQuery(VdcQueryType.GetUserBySessionId, queryParams);

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

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/engine-bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }
}
