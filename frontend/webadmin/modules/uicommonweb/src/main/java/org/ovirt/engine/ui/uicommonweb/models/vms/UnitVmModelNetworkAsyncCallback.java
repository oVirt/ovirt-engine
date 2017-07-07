package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class UnitVmModelNetworkAsyncCallback implements IFrontendActionAsyncCallback {

    private final UnitVmModel unitVmModel;
    private final BaseInterfaceCreatingManager networkCreatingManager;
    private final Guid idToUpdate;

    public UnitVmModelNetworkAsyncCallback(final UnitVmModel unitVmModel,
            final BaseInterfaceCreatingManager networkCreatingManager) {

        this(unitVmModel, networkCreatingManager, null);
    }

    public UnitVmModelNetworkAsyncCallback(final UnitVmModel unitVmModel,
            final BaseInterfaceCreatingManager networkCreatingManager,
            final Guid idToUpdate) {

        this.unitVmModel = unitVmModel;
        this.networkCreatingManager = networkCreatingManager;
        this.idToUpdate = idToUpdate;
    }

    @Override
    public void executed(FrontendActionAsyncResult result) {
        ActionReturnValue returnValue = result.getReturnValue();
        if (returnValue != null && returnValue.getSucceeded()) {
            networkCreatingManager.updateVnics((idToUpdate == null) ? (Guid) returnValue.getActionReturnValue()
                    : idToUpdate, unitVmModel.getNicsWithLogicalNetworks().getItems(), unitVmModel);
        } else {
            networkCreatingManager.getCallback().queryFailed();
        }
    }

}
