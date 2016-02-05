package org.ovirt.engine.ui.frontend.server.gwt;

import java.util.ArrayList;
import java.util.Random;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;

public class GenericApiGWTServiceImpl extends XsrfProtectedRpcServlet implements GenericApiGWTService {

    private static final long serialVersionUID = 7395780289048030855L;

    static Random r = new Random();
    boolean noBackend = false;

    private static final Logger log = LoggerFactory.getLogger(GenericApiGWTServiceImpl.class);

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
    public VdcQueryReturnValue runQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters) {
        log.debug("Server: RunQuery invoked!"); //$NON-NLS-1$
        debugQuery(search, searchParameters);
        searchParameters.setSessionId(getEngineSessionId());
        return getBackend().runQuery(search, searchParameters);
    }

    @Override
    public VdcQueryReturnValue runPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params) {
        log.debug("Server: runPublicQuery invoked! '{}'", queryType); //$NON-NLS-1$
        debugQuery(queryType, params);
        return getBackend().runPublicQuery(queryType, params);
    }

    @Override
    public ArrayList<VdcQueryReturnValue> runMultipleQueries(
            ArrayList<VdcQueryType> queryTypeList,
            ArrayList<VdcQueryParametersBase> queryParamsList) {
        int size = queryTypeList == null ? 0 : queryTypeList.size();
        log.debug("Server: RunMultipleQuery invoked! [amount of queries: {}]", size); //$NON-NLS-1$
        ArrayList<VdcQueryReturnValue> ret = new ArrayList<>();

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
                ret.add(runQuery(queryTypeList.get(i), queryParamsList.get(i)));
            }
        }

        for (VdcQueryReturnValue vqrv : ret) {
            log.debug("VdcQueryReturnValue '{}'", vqrv); //$NON-NLS-1$
        }

        log.debug("Server: RunMultipleQuery result [amount of queries: {}]", ret.size()); //$NON-NLS-1$

        return ret;
    }

    @Override
    public ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams, boolean isRunOnlyIfAllValidationPass) {
        return runMultipleActions(actionType, multipleParams, isRunOnlyIfAllValidationPass, false);
    }

    @Override
    public ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams, boolean isRunOnlyIfAllValidationPass, boolean isWaitForResult) {
        log.debug("Server: RunMultipleAction invoked! [amount of actions: {}]", multipleParams.size()); //$NON-NLS-1$

        for (VdcActionParametersBase params : multipleParams) {
            params.setSessionId(getEngineSessionId());
        }

        ArrayList<VdcReturnValueBase> returnValues =
                getBackend().runMultipleActions(actionType, multipleParams, isRunOnlyIfAllValidationPass, isWaitForResult);

        return returnValues;
    }

    @Override
    public VdcReturnValueBase runAction(VdcActionType actionType,
            VdcActionParametersBase params) {
        log.debug("Server: RunAction invoked!"); //$NON-NLS-1$
        debugAction(actionType, params);
        params.setSessionId(getEngineSessionId());

        if (noBackend) {
            VdcReturnValueBase rValue = new VdcReturnValueBase();
            rValue.setSucceeded(true);
            return rValue;
        }

        return getBackend().runAction(actionType, params);
    }

    private String getEngineSessionId() {
        return (String) getSession().getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
    }

    private HttpSession getSession() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();

        log.debug("IP '{}', Session ID '{}'", request.getRemoteAddr(), session.getId()); //$NON-NLS-1$

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
        } else if (value != null) {
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
        log.debug("Query type '{}', Parameters '{}'", queryType, parameters); //$NON-NLS-1$
    }

    private void debugAction(VdcActionType actionType, VdcActionParametersBase params) {
        log.debug("Action type '{}', Parameters '{}'", actionType, params); //$NON-NLS-1$
    }

}
