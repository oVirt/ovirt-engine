package org.ovirt.engine.ui.frontend.server.gwt;

import java.util.ArrayList;
import java.util.Random;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;

import com.google.gwt.rpc.server.RpcServlet;
import com.google.gwt.user.client.rpc.SerializationException;

public class GenericApiGWTServiceImpl extends RpcServlet implements GenericApiGWTService {

    private static final long serialVersionUID = 7395780289048030855L;

    static Random r = new Random();
    boolean noBackend = false;

    private static final Logger log = Logger.getLogger(GenericApiGWTServiceImpl.class);

    private BackendLocal backend;

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public BackendLocal getBackend() {
        return backend;
    }

    @Override
    public void init() throws ServletException {
        log.debug("Initializing servlet!"); //$NON-NLS-1$
    }

    @Override
    public VdcQueryReturnValue RunQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters) {
        log.debug("Server: RunQuery invoked!"); //$NON-NLS-1$
        debugQuery(search, searchParameters);
        searchParameters.setSessionId(getSessionId());
        return getBackend().RunQuery(search, searchParameters);
    }

    @Override
    public VdcQueryReturnValue RunPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params) {
        log.debug("Server: RunPublicQuery invoked! " + queryType); //$NON-NLS-1$
        debugQuery(queryType, params);
        return getBackend().RunPublicQuery(queryType, params);
    }

    @Override
    public ArrayList<VdcQueryReturnValue> RunMultipleQueries(
            ArrayList<VdcQueryType> queryTypeList,
            ArrayList<VdcQueryParametersBase> queryParamsList) {
        log.debug("Server: RunMultipleQuery invoked! [amount of queries: " + queryTypeList.size() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        ArrayList<VdcQueryReturnValue> ret = new ArrayList<VdcQueryReturnValue>();

        if (queryTypeList == null || queryParamsList == null) {
            // TODO: LOG: "queryTypeList and/or queryParamsList is null."
        }

        else if (queryTypeList.size() != queryParamsList.size()) {
            // TODO: LOG:
            // "queryTypeList and queryParamsList don't have the same amount of items."
        }

        else {
            for (int i = 0; i < queryTypeList.size(); i++) {
                debugQuery(queryTypeList.get(i), queryParamsList.get(i));
                ret.add(RunQuery(queryTypeList.get(i), queryParamsList.get(i)));
            }
        }

        for (VdcQueryReturnValue vqrv : ret) {
            log.debug("VdcQueryReturnValue: " + vqrv); //$NON-NLS-1$
        }

        log.debug("Server: RunMultipleQuery result [amount of queries: " + ret.size() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        return ret;
    }

    @Override
    public ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams, boolean isRunOnlyIfAllCanDoPass) {
        log.debug("Server: RunMultipleAction invoked! [amount of actions: " + multipleParams.size() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        for (VdcActionParametersBase params : multipleParams) {
            params.setSessionId(getSessionId());
        }

        ArrayList<VdcReturnValueBase> returnValues =
                getBackend().RunMultipleActions(actionType, multipleParams, isRunOnlyIfAllCanDoPass);

        return returnValues;
    }

    @Override
    public VdcReturnValueBase RunAction(VdcActionType actionType,
            VdcActionParametersBase params) {
        log.debug("Server: RunAction invoked!"); //$NON-NLS-1$
        debugAction(actionType, params);
        params.setSessionId(getSessionId());

        if (noBackend) {
            VdcReturnValueBase rValue = new VdcReturnValueBase();
            rValue.setSucceeded(true);
            return rValue;
        }

        return getBackend().RunAction(actionType, params);
    }

    @Override
    public VdcUser getLoggedInUser() {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(getSessionId());
        queryParams.setHttpSessionId(getSessionId());

        VdcQueryReturnValue vqrv = RunQuery(VdcQueryType.GetUserBySessionId,
                queryParams);

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

    @Override
    public VdcReturnValueBase logOff(VdcUser userToLogoff) {
        LogoutUserParameters params = new LogoutUserParameters(userToLogoff.getUserId());
        params.setSessionId(getSessionId());
        VdcReturnValueBase returnValue = getBackend().Logoff(params);
        return returnValue;
    }

    @Override
    public VdcReturnValueBase Login(String userName, String password, String domain) {
        LoginUserParameters params = new LoginUserParameters(userName, password, domain, null, null, null);
        params.setSessionId(getSessionId());
        params.setActionType(VdcActionType.LoginAdminUser);
        VdcReturnValueBase returnValue = getBackend().Login(params);
        return returnValue;
    }

    private String getSessionId() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();

        log.debug("IP [" + request.getRemoteAddr() + "], Session ID [" + session.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return session.getId();
    }

    @Override
    protected void doUnexpectedFailure (Throwable error) {
        // If the user is using a version of the application different to what
        // the server expects the names of the RPC serialization policy files
        // will not match, and in that case GWT just sends the exception to the
        // log, which is not very user or admin friendly, so we replace that
        // with a more friendly message:
        if (error instanceof SerializationException) {
            error = new SerializationException(
                "Can't find the serialization policy file. " + //$NON-NLS-1$
                "This probably means that the user has an old " + //$NON-NLS-1$
                "version of the application loaded in the " + //$NON-NLS-1$
                "browser. To solve the issue the user needs " + //$NON-NLS-1$
                "to close the browser and open it again, so " + //$NON-NLS-1$
                "that the application is reloaded.", //$NON-NLS-1$
                error
            );
        }

        // Now that we replaced the message let GWT do what it uses to do:
        super.doUnexpectedFailure(error);
    }

    private void debugQuery(VdcQueryType queryType, VdcQueryParametersBase parameters) {
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Query type: "); //$NON-NLS-1$
            builder.append(queryType);
            builder.append(", Parameters: "); //$NON-NLS-1$
            builder.append(parameters);
            log.debug(builder.toString());
        }
    }

    private void debugAction(VdcActionType actionType, VdcActionParametersBase params) {
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Action type: "); //$NON-NLS-1$
            builder.append(actionType);
            builder.append(", Parameters: "); //$NON-NLS-1$
            builder.append(params);
            log.debug(builder.toString());
        }
    }

}
