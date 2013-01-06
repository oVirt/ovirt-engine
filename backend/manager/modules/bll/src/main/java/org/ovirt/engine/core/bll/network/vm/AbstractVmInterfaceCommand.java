package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

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

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
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

    protected boolean addMacToPool(String macAddress) {
        if (MacPoolManager.getInstance().addMac(macAddress)) {
            return true;
        } else {
            throw new VdcBLLException(VdcBllErrors.MAC_ADDRESS_IS_IN_USE);
        }
    }
}
