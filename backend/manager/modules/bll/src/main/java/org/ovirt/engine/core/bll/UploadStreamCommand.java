package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
@LockIdNameAttribute
public class UploadStreamCommand<T extends UploadStreamParameters> extends BaseImagesCommand<T> {
    Guid cachedSpmId;

    public UploadStreamCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
    }

    private Guid getPoolSpmId() {
        if (cachedSpmId == null) {
            cachedSpmId = getStoragePool().getspm_vds_id();
        }
        return cachedSpmId;
    }

    @Override
    protected boolean canDoAction() {
        if (getPoolSpmId() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_SPM);
        }

        setStoragePool(null);
        if (getStoragePool() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (!getPoolSpmId().equals(getStoragePool().getspm_vds_id())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SPM_CHANGED);
        }

        VdsDynamic vdsDynamic = getVdsDynamicDao().get(getPoolSpmId());
        if (vdsDynamic == null || vdsDynamic.getStatus() != VDSStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.VAR__HOST_STATUS__UP);
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        setVdsId(vdsDynamic.getId());

        DiskImage targetDisk = getDiskImage();
        //Currently we'd like to support only preallocated disks to avoid possible extend on vdsm side.
        if (targetDisk.getVolumeType() != VolumeType.Preallocated) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_VOLUME_TYPE_UNSUPPORTED,
                    String.format("$volumeType %1$s", targetDisk.getVolumeType().toString()),
                    String.format("$supportedVolumeTypes %1$s", VolumeType.Preallocated));
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        UploadStreamVDSCommandParameters vdsCommandParameters =
                new UploadStreamVDSCommandParameters(
                        getVdsId(),
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getStreamLength(),
                        getParameters().getInputStream());

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.UploadStream, vdsCommandParameters);

        if (vdsReturnValue.getSucceeded()) {
            Guid taskId = getAsyncTaskId();
            getReturnValue().getInternalVdsmTaskIdList().add(createTask(taskId,
                    vdsReturnValue.getCreationInfo(),
                    getParameters().getParentCommand(),
                    VdcObjectType.Storage,
                    getParameters().getStorageDomainId(),
                    getParameters().getDestinationImageId()));

            setSucceeded(true);
        }
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.downloadImageFromStream;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getStoragePool() != null && getPoolSpmId() != null) {
            return Collections.singletonMap(getPoolSpmId().toString(),
                    new Pair<>(LockingGroup.VDS_EXECUTION.toString(),
                            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString()));
        }
        return null;
    }
}
