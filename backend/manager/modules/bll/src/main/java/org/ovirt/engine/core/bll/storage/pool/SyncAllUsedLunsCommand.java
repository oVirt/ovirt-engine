package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.SyncAttachedDirectLunsParameters;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
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
        syncAttachedDirectLuns(deviceList);

        setSucceeded(true);
    }

    private void syncStorageDomainsLuns(List<LUNs> deviceList) {
        SyncLunsParameters parameters = new SyncLunsParameters(getParameters().getStoragePoolId(), deviceList);
        runInternalAction(VdcActionType.SyncStorageDomainsLuns, parameters);
    }

    private void syncAttachedDirectLuns(List<LUNs> deviceList) {
        SyncAttachedDirectLunsParameters parameters = new SyncAttachedDirectLunsParameters(
                getParameters().getStoragePoolId());
        parameters.setDeviceList(deviceList);
        runInternalAction(VdcActionType.SyncAttachedDirectLuns, parameters);
    }
}
