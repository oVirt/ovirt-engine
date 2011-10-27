package org.ovirt.engine.core.bll.interfaces;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.DateTime;

public interface BackendInternal extends BackendLocal {

    public VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters);
    public VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters,CompensationContext context);

    /**
     * End the command with the passed compensation context, so that the calling command can compensate the internal
     * changes if need to.
     *
     * @param actionType
     *            The type of command to end.
     * @param parameters
     *            The command's parameters.
     * @param compensationContext
     *            The compensation context to use.
     * @return The result of the command ending.
     */
    public VdcReturnValueBase endAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CompensationContext compensationContext);

    public VdcQueryReturnValue runInternalQuery(VdcQueryType actionType, VdcQueryParametersBase parameters);

    java.util.ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters);

    public DateTime getStartedAt();
}
