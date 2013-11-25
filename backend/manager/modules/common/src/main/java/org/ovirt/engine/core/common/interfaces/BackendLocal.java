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
    VdcReturnValueBase runAction(VdcActionType actionType, VdcActionParametersBase parameters);

    VDSBrokerFrontend getResourceManager();

    VdcQueryReturnValue runQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    public VdcReturnValueBase endAction(VdcActionType actionType, VdcActionParametersBase parameters);

    ErrorTranslator getErrorsTranslator();

    ErrorTranslator getVdsErrorsTranslator();

    java.util.ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllCanDoPass);

    void initialize();

    VdcQueryReturnValue runPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    VdcReturnValueBase login(LoginUserParameters parameters);

    VdcReturnValueBase logoff(LogoutUserParameters parameters);

    VdcQueryReturnValue RunAutoQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);
}
