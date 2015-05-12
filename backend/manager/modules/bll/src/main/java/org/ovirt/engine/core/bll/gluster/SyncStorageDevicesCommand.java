package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsValidator;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

public class SyncStorageDevicesCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public SyncStorageDevicesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        VDSGroup cluster = getVdsGroup();
        if (!cluster.supportsGlusterService()
                || (!getGlusterUtil().isGlusterBrickProvisioningSupported(cluster.getcompatibility_version(),
                        getVdsGroup().getId()))) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_PROVISIONING_NOT_SUPPORTED_BY_CLUSTER);
        }

        VdsValidator validator = new VdsValidator(getVds());
        return validate(validator.isUp());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SYNC);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE_DEVICE);
        addCanDoActionMessageVariable("VdsName", getVds().getName());
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetStorageDeviceList, new VdsIdVDSCommandParametersBase(getVds().getId()));
        if (returnValue.getSucceeded()){
            List<StorageDevice> storageDevices = (List<StorageDevice>) returnValue.getReturnValue();
            getStorageDeviceSyncJobInstance().updateStorageDevices(getVds(), storageDevices);
            setSucceeded(true);
        } else {
            handleVdsError(returnValue);
            setSucceeded(false);
        }

    }

    private StorageDeviceSyncJob getStorageDeviceSyncJobInstance() {
        return Injector.get(StorageDeviceSyncJob.class);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SYNC_STORAGE_DEVICES_IN_HOST
                : AuditLogType.SYNC_STORAGE_DEVICES_IN_HOST_FAILED;
    }

    protected GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }

}
