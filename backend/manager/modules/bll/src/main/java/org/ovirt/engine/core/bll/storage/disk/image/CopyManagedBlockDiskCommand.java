package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDiskParameters;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters;
import org.ovirt.engine.core.common.action.ExternalLeaseParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.ManagedBlockStorageLocationInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ExternalLease;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LeaseJobStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ExternalLeaseDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CopyManagedBlockDiskCommand<T extends CopyImageGroupWithDataCommandParameters> extends CopyImageGroupWithDataCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ExternalLeaseDao externalLeaseDao;

    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    private StorageDomainType sourceDomainType;
    private StorageDomainType destDomainType;


    public CopyManagedBlockDiskCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        sourceDomainType = storageDomainDao.get(getParameters().getSrcDomain()).getStorageDomainType();
        destDomainType = storageDomainDao.get(getParameters().getDestDomain()).getStorageDomainType();

        super.init();
    }

    @Override
    protected boolean validate() {
        if (getParameters().getVdsRunningOn() == null) {
            log.error("No hosts available for copy were found");
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_HOSTS_FOUND);
        }

        return super.validate();
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        getParameters().setStage(CopyImageGroupWithDataCommandParameters.CopyStage.LEASE_CREATION);
        createJobLease();
        setSucceeded(true);
        persistCommandIfNeeded();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getStage()) {
        case LEASE_CREATION:
            if (!saveLease()) {
                    setCommandStatus(CommandStatus.FAILED);
            }

            getParameters().setStage(CopyImageGroupWithDataCommandParameters.CopyStage.DATA_COPY);
            break;
        case DATA_COPY:
            copyData();
            getParameters().setStage(CopyImageGroupWithDataCommandParameters.CopyStage.DETACH_VOLUME);
            break;
        case DETACH_VOLUME:
            detachVolume();
            getParameters().setStage(CopyImageGroupWithDataCommandParameters.CopyStage.UPDATE_VOLUME);
            break;
        case UPDATE_VOLUME:
            return false;
        }

        persistCommandIfNeeded();
        return true;
    }

    @Override
    protected void createVolumes() {
        DiskImage sourceDisk = diskImageDao.get(getParameters().getImageId());
        // Fallback to regular image creation if target is an image
        if (destDomainType.isDataDomain()) {
            super.createVolumes();
        } else if (destDomainType == StorageDomainType.ManagedBlockStorage) {
            // Create and attach target disk (if target is Managed Block Storage)
            ManagedBlockStorageDisk destDisk = addManagedBlockDisk(sourceDisk);
            getParameters().setDestinationImageId(destDisk.getId());
            getParameters().setDestImageGroupId(destDisk.getId());
            String targetPath = attachVolume(destDisk);
            getParameters().setTargetPath(targetPath);
            getParameters().setDestImageGroupId(getParameters().getDestinationImageId());
        }

        // Attach source to host (if source is Managed Block Storage)
        // TODO: handle failures
        if (sourceDomainType == StorageDomainType.ManagedBlockStorage) {
            String sourcePath = attachVolume(createManagedBlockDiskFromDiskImage(sourceDisk));
            getParameters().setSourcePath(sourcePath);
        }
    }

    @Override
    protected void copyData() {
        Integer weight = getParameters().getOperationsJobWeight()
                .get(CopyImageGroupWithDataCommandParameters.CopyStage.DATA_COPY.name());

        CopyDataCommandParameters parameters = new CopyDataCommandParameters(getParameters().getStoragePoolId(),
                buildEndpoint(getParameters().getSrcDomain(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getSourcePath(),
                        getParameters().getLeaseStorageId()),
                buildEndpoint(getParameters().getDestDomain(),
                        getParameters().getDestImageGroupId(),
                        getParameters().getDestinationImageId(),
                        getParameters().getTargetPath(),
                        getParameters().getLeaseStorageId()),
                false);
        parameters.setStorageJobId(getJobId());
        parameters.setVdsId(getParameters().getVdsRunningOn());
        parameters.setVdsRunningOn(getParameters().getVdsRunningOn());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(weight);

        runInternalActionWithTasksContext(ActionType.CopyData, parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__COPY);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    private void createJobLease() {
        Guid masterDomainId = storageDomainDao.getMasterStorageDomainIdForPool(getParameters().getStoragePoolId());
        Map<String, Object> leaseMetadata = new HashMap<>();
        leaseMetadata.put("job_status", LeaseJobStatus.Pending.getValue());
        leaseMetadata.put("generation", 0);

        // We use the job_id as the lease ID, this ID is stable but random, ensuring we can't accidentally
        // fetch an incorrect lease.
        leaseMetadata.put("job_id", getJobId().toString());

        // TODO: job is the only type currently, create an enum once other options exist
        leaseMetadata.put("type", "JOB");
        ExternalLeaseParameters params = new ExternalLeaseParameters(getParameters().getStoragePoolId(),
                masterDomainId,
                getJobId(),
                leaseMetadata);
        params.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getDestImageGroupId()));

        ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.AddExternalLease, params);
        if (!returnValue.getSucceeded()) {
            log.error("Failed to create external lease '{}'", getJobId());
            setSucceeded(false);
            return;
        }

        getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
        getParameters().setVdsmTaskIds(new ArrayList<>());
        getParameters().getVdsmTaskIds().addAll(returnValue.getInternalVdsmTaskIdList());

        // TODO: select a random data storage domain
        getParameters().setLeaseStorageId(masterDomainId);
        getParameters().setStage(CopyImageGroupWithDataCommandParameters.CopyStage.LEASE_CREATION);
    }

    private boolean saveLease() {
        Guid masterDomainId = storageDomainDao.getMasterStorageDomainIdForPool(getParameters().getStoragePoolId());

        // TODO: Rename to GetLeaseInfo
        VmLeaseVDSParameters params = new VmLeaseVDSParameters(getParameters().getStoragePoolId(),
                masterDomainId,
                getJobId());
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetVmLeaseInfo, params);
        if (!returnValue.getSucceeded()) {
            log.error("Couldn't get lease info after creation");
            return false;
        }

        ExternalLease externalLease = new ExternalLease(getJobId(), masterDomainId);

        TransactionSupport.executeInNewTransaction(() -> {
            externalLeaseDao.save(externalLease);
            return null;
        });

        return true;
    }

    private String attachVolume(ManagedBlockStorageDisk disk) {
        VDSReturnValue vdsReturnValue =
                managedBlockStorageCommandUtil.attachManagedBlockStorageDisk(disk,
                        vdsDao.get(getParameters().getVdsRunningOn()));
        if (!vdsReturnValue.getSucceeded()) {
            throw new RuntimeException(vdsReturnValue.getExceptionObject());
        }

        return (String) ((Map<String, Object>) vdsReturnValue.getReturnValue()).get("path");
    }

    private void detachVolume() {
        // TODO: handle detach failures
        if (sourceDomainType == StorageDomainType.ManagedBlockStorage) {
            DiskImage sourceDisk = diskImageDao.get(getParameters().getImageId());
            managedBlockStorageCommandUtil.disconnectManagedBlockStorageDeviceFromHost(sourceDisk,
                    getParameters().getVdsRunningOn());
        }

        if (destDomainType == StorageDomainType.ManagedBlockStorage) {
            DiskImage targetDisk = diskImageDao.get(getParameters().getDestinationImageId());
            managedBlockStorageCommandUtil.disconnectManagedBlockStorageDeviceFromHost(targetDisk,
                    getParameters().getVdsRunningOn());
        }
    }

    private LocationInfo buildEndpoint(Guid storageDomainId,
            Guid diskId,
            Guid imageId,
            String path,
            Guid leaseStorageDomainId) {
        if (storageDomainDao.get(storageDomainId).getStorageType() == StorageType.MANAGED_BLOCK_STORAGE) {
            Map<String, Object> lease = new HashMap<>();
            lease.put("lease_id", getJobId().toString());
            lease.put("sd_id", leaseStorageDomainId.toString());
            return new ManagedBlockStorageLocationInfo(path,
                    lease,
                    0,
                    VolumeFormat.RAW,
                    false,
                    getParameters().getDestDomain());
        }
        return new VdsmImageLocationInfo(storageDomainId, diskId, imageId, null);
    }

    private ManagedBlockStorageDisk addManagedBlockDisk(DiskImage sourceDisk) {
        ManagedBlockStorageDisk targetDisk = createManagedBlockDiskFromDiskImage(sourceDisk);

        AddManagedBlockStorageDiskParameters params = new AddManagedBlockStorageDiskParameters();
        params.setShouldPlugDiskToVm(false);
        params.setStorageDomainId(getParameters().getDestDomain());
        params.setDiskInfo(targetDisk);

        // TODO: handle failure
        runInternalAction(ActionType.AddManagedBlockStorageDisk, params);

        return targetDisk;
    }

    private ManagedBlockStorageDisk createManagedBlockDiskFromDiskImage(DiskImage diskImage) {
        ManagedBlockStorageDisk managedBlockDisk = new ManagedBlockStorageDisk(diskImage);

        managedBlockDisk.setId(getParameters().getDestinationImageId());
        managedBlockDisk.setImageId(getParameters().getDestinationImageId());
        managedBlockDisk.setDiskAlias(getParameters().getDiskAlias());
        managedBlockDisk.setDiskDescription(getParameters().getDescription());
        managedBlockDisk.getStorageIds().add(getParameters().getDestDomain());

        return managedBlockDisk;
    }

    private void removeExternalLease() {
        ExternalLease externalLease = externalLeaseDao.get(getJobId());
        ExternalLeaseParameters params = new ExternalLeaseParameters();
        params.setLeaseId(externalLease.getId());
        params.setStorageDomainId(externalLease.getStorageDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setEntityInfo(new EntityInfo(VdcObjectType.Storage, externalLease.getStorageDomainId()));
        runInternalAction(ActionType.RemoveExternalLease, params);

        TransactionSupport.executeInNewTransaction(() -> {
            externalLeaseDao.remove(externalLease.getId());

            return null;
        });
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        removeExternalLease();
        TransactionSupport.executeInNewTransaction(() -> {
            imagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.OK);
            return null;
        });

        super.endSuccessfully();
    }

    @Override
    protected void endWithFailure() {
        detachVolume();
        removeExternalLease();

        if (destDomainType == StorageDomainType.ManagedBlockStorage) {
            RemoveDiskParameters params = new RemoveDiskParameters();
            params.setStorageDomainId(getParameters().getDestDomain());
            params.setDiskId(getParameters().getDestImageGroupId());
            runInternalAction(ActionType.RemoveManagedBlockStorageDisk, params);
        }

        super.endWithFailure();
    }
}
