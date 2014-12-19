package org.ovirt.engine.core.bll.gluster;

import java.util.Arrays;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsValidator;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;

public class SyncStorageDevicesCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public SyncStorageDevicesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        VDSGroup cluster = getVdsGroup();
        if (!cluster.supportsGlusterService()
                || !GlusterFeatureSupported.glusterBrickProvisioning(cluster.getcompatibility_version())) {
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

        getStorageDeviceSyncJobInstance().refreshStorageDevicesFromServers(Arrays.asList(getVds()));
        setSucceeded(true);
    }

    private StorageDeviceSyncJob getStorageDeviceSyncJobInstance() {
        return Injector.get(StorageDeviceSyncJob.class);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SYNC_STORAGE_DEVICES_IN_HOST
                : AuditLogType.SYNC_STORAGE_DEVICES_IN_HOST_FAILED;
    }
}
