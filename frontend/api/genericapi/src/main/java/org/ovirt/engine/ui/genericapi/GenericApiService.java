package org.ovirt.engine.ui.genericapi;

import java.util.ArrayList;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.AsyncQueryResults;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;
import org.ovirt.engine.ui.genericapi.returnvalues.UIQueryReturnValue;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryBase;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryType;
import org.ovirt.engine.core.utils.CXFContextInterceptor;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

@Stateless
@Interceptors({ CXFContextInterceptor.class })
public class GenericApiService {

    @EJB(name = "Backend")
    private static BackendLocal backend;

    public VdcReturnValueBase EndAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        VdcReturnValueBase returnValue = backend.EndAction(actionType, parameters);
        return returnValue;
    }

    public VdcReturnValueBase RunAction(VdcActionType actionType, VdcActionParametersBase parameters) {
        VdcReturnValueBase returnValue = backend.RunAction(actionType, parameters);
        return returnValue;
    }

    public void RunAsyncQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        backend.RunAsyncQuery(actionType, parameters);

    }

    public ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters) {
        java.util.ArrayList<VdcReturnValueBase> returnValue = backend.RunMultipleActions(
                actionType, parameters);
        return returnValue;
    }

    public VdcQueryReturnValue RunQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        VdcQueryReturnValue returnValue = backend.RunQuery(actionType, parameters);
        return returnValue;
    }

    public AsyncQueryResults GetAsyncQueryResults() {
        // AsyncQueryResults returnValue =
        // Backend.getInstance().GetAsyncQueryResults();
        return backend.GetAsyncQueryResults();
    }

    public VdcReturnValueBase Login(LoginUserParameters parameters) {
        VdcReturnValueBase returnValue = backend.Login(parameters);
        return returnValue;
    }

    public VdcReturnValueBase Logoff(LogoutUserParameters parameters) {
        VdcReturnValueBase returnValue = backend.Logoff(parameters);
        return returnValue;
    }

    public VdcQueryReturnValue RunPublicQuery(VdcQueryType actionType,
            VdcQueryParametersBase parameters) {
        VdcQueryReturnValue returnValue = backend.RunPublicQuery(actionType, parameters);
        return returnValue;
    }

    public VdcReturnValueBase RunAutoAction(VdcActionType actionType,
            VdcActionParametersBase parameters) {
        VdcReturnValueBase returnValue = backend.RunAutoAction(actionType, parameters);
        return returnValue;
    }

    public VdcQueryReturnValue RunAutoQuery(VdcQueryType actionType,
            VdcQueryParametersBase parameters) {
        VdcQueryReturnValue returnValue = backend.RunAutoQuery(actionType, parameters);
        return returnValue;
    }

    public UIQueryReturnValue RunUIQuery(UIQueryType actionType, UIQueryParametersBase parameters) {
        UIQueryBase query = UICommandsFactory.CreateQueryCommand(actionType, parameters);
        query.executeQuery();
        return query.getReturnValue();
    }

    public ArrayList<VdcQueryReturnValue> RunMultipleQueries(ArrayList<VdcQueryType> queryTypeList, ArrayList<VdcQueryParametersBase> queryParamsList) {
        ArrayList<VdcQueryReturnValue> ret = new ArrayList<VdcQueryReturnValue>();

        if(queryTypeList == null || queryParamsList == null) {
            // TODO: LOG: "queryTypeList and/or queryParamsList is null."
        }

        else if(queryTypeList.size() != queryParamsList.size()) {
            // TODO: LOG: "queryTypeList and queryParamsList don't have the same amount of items."
        }

        else if(queryTypeList.size() == 0) {
            // TODO: LOG: no queries to execute.
        }

        else {
            // TODO: next section is a temporary hack for setting the session ID
            // for all queries to be the correct one.
            VdcQueryParametersBase firstParams = queryParamsList.get(0);
            if(StringHelper.isNullOrEmpty(firstParams.getHttpSessionId()) && StringHelper.isNullOrEmpty(firstParams.getSessionId())) {
                String sessionID = ThreadLocalParamsContainer.getHttpSessionId();
                for(VdcQueryParametersBase queryParams : queryParamsList) {
                    queryParams.setSessionId(sessionID);
                    queryParams.setHttpSessionId(sessionID);
                }
            }

            for(int i=0; i<queryTypeList.size() ; i++) {
                ret.add(RunQuery(queryTypeList.get(i), queryParamsList.get(i)));
            }
        }

        return ret;
    }
}
