package org.ovirt.engine.core.bll;

import javax.ejb.Remote;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

@Remote
public interface BackendRemote {

    VdcReturnValueBase RunAction(VdcActionType actionType, VdcActionParametersBase parameters);

    VDSBrokerFrontend getResourceManager();

    VdcQueryReturnValue RunQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    public VdcReturnValueBase EndAction(VdcActionType actionType, VdcActionParametersBase parameters);

    ErrorTranslator getErrorsTranslator();
    ErrorTranslator getVdsErrorsTranslator();

    java.util.ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters);

    void Initialize();
}
