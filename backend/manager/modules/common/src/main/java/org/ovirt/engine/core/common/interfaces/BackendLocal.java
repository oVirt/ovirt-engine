package org.ovirt.engine.core.common.interfaces;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public interface BackendLocal {
    VdcReturnValueBase runAction(ActionType actionType, ActionParametersBase parameters);

    VDSBrokerFrontend getResourceManager();

    QueryReturnValue runQuery(QueryType actionType, QueryParametersBase parameters);

    ErrorTranslator getErrorsTranslator();

    ErrorTranslator getVdsErrorsTranslator();

    List<VdcReturnValueBase> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass, boolean waitForResult);

    List<VdcReturnValueBase> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass);

    QueryReturnValue runPublicQuery(QueryType actionType, QueryParametersBase parameters);

    VdcReturnValueBase logoff(ActionParametersBase parameters);
}
