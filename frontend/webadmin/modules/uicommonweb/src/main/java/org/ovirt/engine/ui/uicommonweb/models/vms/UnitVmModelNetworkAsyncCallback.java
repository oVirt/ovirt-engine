package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class UnitVmModelNetworkAsyncCallback implements IFrontendActionAsyncCallback {

    private final UnitVmModel unitVmModel;
    private final VmInterfaceCreatingManager networkCreatingManager;
    private final Guid idToUpdate;

    public UnitVmModelNetworkAsyncCallback(final UnitVmModel unitVmModel,
            final VmInterfaceCreatingManager networkCreatingManager) {

        this(unitVmModel, networkCreatingManager, null);
    }

    public UnitVmModelNetworkAsyncCallback(final UnitVmModel unitVmModel,
            final VmInterfaceCreatingManager networkCreatingManager,
            final Guid idToUpdate) {

        this.unitVmModel = unitVmModel;
        this.networkCreatingManager = networkCreatingManager;
        this.idToUpdate = idToUpdate;
    }

    @Override
    public void executed(FrontendActionAsyncResult result) {
        VdcReturnValueBase returnValue = result.getReturnValue();
        if (returnValue != null && returnValue.getSucceeded()) {
            networkCreatingManager.updateVnics((idToUpdate == null) ? (Guid) returnValue.getActionReturnValue()
                    : idToUpdate, unitVmModel.getNicsWithLogicalNetworks().getItems());
        } else {
            networkCreatingManager.getCallback().queryFailed();
        }
    }

}
