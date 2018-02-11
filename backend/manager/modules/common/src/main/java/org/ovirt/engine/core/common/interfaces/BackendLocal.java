package org.ovirt.engine.core.common.interfaces;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public interface BackendLocal {
    ActionReturnValue runAction(ActionType actionType, ActionParametersBase parameters);

    QueryReturnValue runQuery(QueryType actionType, QueryParametersBase parameters);

    ErrorTranslator getErrorsTranslator();

    ErrorTranslator getVdsErrorsTranslator();

    List<ActionReturnValue> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass, boolean waitForResult);

    List<ActionReturnValue> runMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters,
            boolean isRunOnlyIfAllValidationPass);

    QueryReturnValue runPublicQuery(QueryType actionType, QueryParametersBase parameters);

    ActionReturnValue logoff(ActionParametersBase parameters);
}
