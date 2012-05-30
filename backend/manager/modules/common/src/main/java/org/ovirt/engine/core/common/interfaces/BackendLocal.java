package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public interface BackendLocal {
    VdcReturnValueBase RunAction(VdcActionType actionType, VdcActionParametersBase parameters);

    VDSBrokerFrontend getResourceManager();

    VdcQueryReturnValue RunQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    public VdcReturnValueBase EndAction(VdcActionType actionType, VdcActionParametersBase parameters);

    ErrorTranslator getErrorsTranslator();
    ErrorTranslator getVdsErrorsTranslator();

    void RunAsyncQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    java.util.ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters);


    void Initialize();

    VdcQueryReturnValue RunPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    VdcReturnValueBase Login(LoginUserParameters parameters);

    VdcReturnValueBase Logoff(LogoutUserParameters parameters);

    // for auto backend
    VdcReturnValueBase RunAutoAction(VdcActionType actionType, VdcActionParametersBase parameters);

    VdcQueryReturnValue RunAutoQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    // for ISession
    // void SetSessionDataBySessionId(String sessionId, String key, Object
    // value);
    //
    // void SetSessionData(String key, Object value);
}
