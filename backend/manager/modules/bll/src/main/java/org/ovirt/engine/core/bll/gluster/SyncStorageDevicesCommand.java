package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.SyncGlusterStorageDevicesParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.di.Injector;

public class SyncStorageDevicesCommand<T extends SyncGlusterStorageDevicesParameter> extends VdsCommand<T> {

    public SyncStorageDevicesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withNoWait();
    }

    @Override
    protected boolean validate() {
        Cluster cluster = getCluster();
        if (!cluster.supportsGlusterService()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_PROVISIONING_NOT_SUPPORTED_BY_CLUSTER);
        }

        //Host status will not checked in case of force. Storage devices will be synced as part of host install/activation
        //and host status will not be up during host activation. So BLL will be called with force in this case.
        if(!getParameters().isForceAction()){
            HostValidator validator = HostValidator.createInstance(getVds());
            return validate(validator.isUp());
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__SYNC);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE_DEVICE);
        addValidationMessageVariable("VdsName", getVds().getName());
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

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.HOST_STORAGE_DEVICES,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_DEVICE_LOCKED));
    }
}
