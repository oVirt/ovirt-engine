package org.ovirt.engine.core.common.interfaces;

import java.util.List;

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

    List<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass, boolean waitForResult);

    List<VdcReturnValueBase> runMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass);

    VdcQueryReturnValue runPublicQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    VdcReturnValueBase logoff(VdcActionParametersBase parameters);
}
