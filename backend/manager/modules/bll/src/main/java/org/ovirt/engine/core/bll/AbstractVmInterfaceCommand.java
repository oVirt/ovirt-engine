package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("serial")
public abstract class AbstractVmInterfaceCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    public AbstractVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    protected boolean activateOrDeactivateNic(Guid nicId, PlugAction plugAction) {
        VdcReturnValueBase returnValue =
                getBackend().runInternalAction(VdcActionType.ActivateDeactivateVmNic,
                        createActivateDeactivateParameters(nicId, plugAction));
        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        return returnValue.getSucceeded();
    }

    private ActivateDeactivateVmNicParameters createActivateDeactivateParameters(Guid nicId,
            PlugAction plugAction) {
        ActivateDeactivateVmNicParameters parameters =
                new ActivateDeactivateVmNicParameters(nicId, plugAction);
        parameters.setVmId(getParameters().getVmId());

        return parameters;
    }

    private void propagateFailure(VdcReturnValueBase internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getCanDoActionMessages().addAll(internalReturnValue.getCanDoActionMessages());
        getReturnValue().setCanDoAction(internalReturnValue.getCanDoAction());
    }
}
