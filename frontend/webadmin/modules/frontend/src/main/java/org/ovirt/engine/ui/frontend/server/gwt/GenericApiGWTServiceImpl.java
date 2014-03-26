package org.ovirt.engine.ui.frontend.server.gwt;

import java.util.ArrayList;
import java.util.Random;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;

import com.google.gwt.user.client.rpc.SerializationException;

public class GenericApiGWTServiceImpl extends XsrfProtectedRpcServlet implements GenericApiGWTService {

    private static final long serialVersionUID = 7395780289048030855L;

    static Random r = new Random();
    boolean noBackend = false;

    private static final Logger log = Logger.getLogger(GenericApiGWTServiceImpl.class);

    private static final String UI_PREFIX = "UI_"; //$NON-NLS-1$

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
    public VdcQueryReturnValue RunQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters) {
        log.debug("Server: RunQuery invoked!"); //$NON-NLS-1$
        debugQuery(search, searchParameters);
        searchParameters.setSessionId(getSession().getId());
        return getBackend().runQuery(search, searchParameters);
    }

    @Override
    public VdcQueryReturnValue RunPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params) {
        log.debug("Server: runPublicQuery invoked! " + queryType); //$NON-NLS-1$
        debugQuery(queryType, params);
        return getBackend().runPublicQuery(queryType, params);
    }

    @Override
    public ArrayList<VdcQueryReturnValue> RunMultipleQueries(
            ArrayList<VdcQueryType> queryTypeList,
            ArrayList<VdcQueryParametersBase> queryParamsList) {
        int size = queryTypeList == null ? 0 : queryTypeList.size();
        log.debug("Server: RunMultipleQuery invoked! [amount of queries: " + size + "]"); //$NON-NLS-1$ //$NON-NLS-2$
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
        return RunMultipleActions(actionType, multipleParams, isRunOnlyIfAllCanDoPass, false);
    }

    @Override
    public ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams, boolean isRunOnlyIfAllCanDoPass, boolean isWaitForResult) {
        log.debug("Server: RunMultipleAction invoked! [amount of actions: " + multipleParams.size() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        for (VdcActionParametersBase params : multipleParams) {
            params.setSessionId(getSession().getId());
        }

        ArrayList<VdcReturnValueBase> returnValues =
                getBackend().runMultipleActions(actionType, multipleParams, isRunOnlyIfAllCanDoPass, isWaitForResult);

        return returnValues;
    }

    @Override
    public VdcReturnValueBase RunAction(VdcActionType actionType,
            VdcActionParametersBase params) {
        log.debug("Server: RunAction invoked!"); //$NON-NLS-1$
        debugAction(actionType, params);
        params.setSessionId(getSession().getId());

        if (noBackend) {
            VdcReturnValueBase rValue = new VdcReturnValueBase();
            rValue.setSucceeded(true);
            return rValue;
        }

        return getBackend().runAction(actionType, params);
    }

    @Override
    public DbUser getLoggedInUser() {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(getSession().getId());
        queryParams.setHttpSessionId(getSession().getId());

        VdcQueryReturnValue vqrv = RunQuery(VdcQueryType.GetUserBySessionId,
                queryParams);

        if (!vqrv.getSucceeded()) {
            return null;
        } else if (vqrv.getSucceeded()) {
            if (vqrv.getReturnValue() == null) {
                return null;
            }
            return (DbUser) vqrv.getReturnValue();
        } else {
            // For unknown reason the result was failed be returned.
            return null;
        }
    }

    @Override
    public VdcReturnValueBase logOff(DbUser userToLogoff) {
        LogoutUserParameters params = new LogoutUserParameters(userToLogoff.getId());
        params.setSessionId(getSession().getId());
        VdcReturnValueBase returnValue = getBackend().logoff(params);
        return returnValue;
    }

    @Override
    public VdcReturnValueBase Login(String userName, String password, String profileName, VdcActionType loginType) {
        LoginUserParameters params = new LoginUserParameters(profileName, userName, password);
        HttpSession originalSession = getSession();
        // Prevent session fixation.
        getSession().invalidate();
        // Calling getSession again after invalidating it should create a new session.
        HttpSession newSession = getSession();
        assert !newSession.equals(originalSession) : "new session the same as old session"; //$NON-NLS-1$

        params.setSessionId(getSession().getId());
        params.setActionType(loginType);
        VdcReturnValueBase returnValue = getBackend().login(params);
        if (returnValue.getSucceeded()) {
            this.getThreadLocalResponse().addHeader("OVIRT-SSO-TOKEN", getSession().getId()); //$NON-NLS-1$
        }
        return returnValue;
    }

    private HttpSession getSession() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();

        log.debug("IP [" + request.getRemoteAddr() + "], Session ID [" + session.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return session;
    }

    @Override
    public void storeInHttpSession(String key, String value) {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        session.setAttribute(UI_PREFIX + key, value);
    }

    @Override
    public String retrieveFromHttpSession(String key) {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        Object value = session.getAttribute(UI_PREFIX + key);
        String result = null;
        if (value instanceof String) {
            result = (String)value;
        } else {
            log.error("Retrieving non string value from session"); //$NON-NLS-1$
        }
        return result;
    }

    @Override
    protected void doUnexpectedFailure(Throwable error) {
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
