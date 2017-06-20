package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncAllUsedLunsCommand<T extends SyncLunsParameters> extends AbstractSyncLunsCommand<T> {

    public SyncAllUsedLunsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        List<LUNs> deviceList = getDeviceList();
        syncStorageDomainsLuns(deviceList);
        syncDirectLunsAttachedToVmsInPool(deviceList);

        setSucceeded(true);
    }

    private void syncStorageDomainsLuns(List<LUNs> deviceList) {
        SyncLunsParameters parameters = new SyncLunsParameters(getParameters().getStoragePoolId(), deviceList);
        runInternalAction(ActionType.SyncStorageDomainsLuns, parameters);
    }

    private void syncDirectLunsAttachedToVmsInPool(List<LUNs> deviceList) {
        SyncDirectLunsParameters parameters = new SyncDirectLunsParameters(getParameters().getStoragePoolId());
        parameters.setDeviceList(deviceList);
        runInternalAction(ActionType.SyncDirectLuns, parameters);
    }
}
