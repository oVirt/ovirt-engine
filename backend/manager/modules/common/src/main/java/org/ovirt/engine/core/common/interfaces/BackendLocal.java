package org.ovirt.engine.core.common.interfaces;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.LoginUserParameters;
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

    ErrorTranslator getErrorsTranslator();

    ErrorTranslator getVdsErrorsTranslator();

    ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllCanDoPass, boolean waitForResult);

    ArrayList<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllCanDoPass);

    void initialize();

    VdcQueryReturnValue runPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    VdcReturnValueBase login(LoginUserParameters parameters);

    VdcReturnValueBase logoff(VdcActionParametersBase parameters);
}
