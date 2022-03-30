package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConvertDiskCommandParameters;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.CreateVolumeContainerCommandParameters;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ConvertDiskCommand<T extends ConvertDiskCommandParameters> extends BaseImagesCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ConvertDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConvertDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        if (getDiskImage() == null) {
            DiskImage diskImage = diskImageDao.getSnapshotById(getLocationInfo().getImageId());
            setDiskImage(diskImage);
        }
    }

    @Override
    protected boolean validate() {
        StorageDomain storageDomain = storageDomainDao.get(getDiskImage().getStorageIds().get(0));
        if (storageDomain == null) {
            return failValidation(EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST);
        }

        // We do not support chains currently
        if (diskImageDao.getAllSnapshotsForImageGroup(getDiskImage().getId()).stream().count() > 1) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_WITH_CHAIN);
        }

        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);
        if (!validate(storageDomainValidator.hasSpaceForNewDisk(getDiskImage()))) {
            return false;
        }

        if (getDiskImage().getImageStatus() == ImageStatus.LOCKED) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_LOCKED);
        }

        // We do not support live stuff currently
        Map<Boolean, List<VM>> vms = vmDao.getForDisk(getDiskImage().getId(), true);

        boolean runningVms = vms.computeIfAbsent(Boolean.TRUE, b -> Collections.emptyList())
                .stream()
                .anyMatch(vm -> vm.isRunningOrPaused());
        if (runningVms) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }

        // Make sure unsupported combinations aren't used (sparse-raw on block, etc)
        return ImagesHandler.checkImageConfiguration(storageDomain.getStorageStaticData(),
                getVolumeType(getDiskImage()),
                getVolumeFormat(getDiskImage()),
                getParameters().getBackup() != null ? getParameters().getBackup() : getDiskImage().getBackup(),
                getReturnValue().getValidationMessages());
    }

    @Override
    protected void executeCommand() {
        Guid hostForExecution = vdsCommandsHelper.getHostForExecution(getDiskImage().getStoragePoolId(),
                FeatureSupported::isConvertDiskSupported);
        if (Guid.isNullOrEmpty(hostForExecution)) {
            log.error("Could not find a host to perform conversion");
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        getParameters().setVdsRunningOn(hostForExecution);

        // Disable pre-polling nonsense
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.Disk, getDiskImage().getId()));
        getParameters().setNewVolGuid(Guid.newGuid());

        imagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(getDiskImage().getId(),
                ImageStatus.LOCKED,
                ImageStatus.OK,
                getCompensationContext());

        // Measure volume for new format
        ActionReturnValue actionReturnValue =
                runInternalAction(ActionType.MeasureVolume, createMeasureVolumeParameters(),
                        ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!actionReturnValue.getSucceeded()) {
            log.error("Failed to measure volume '{}'", getDiskImage().getImageId());
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        long requiredSize =  actionReturnValue.getActionReturnValue();

        // Create volume in the same disk
        runInternalAction(ActionType.CreateVolumeContainer, createVolumeCreationParameters(getDiskImage(), requiredSize));
        updatePhase(ConvertDiskCommandParameters.ConvertDiskPhase.CONVERT_VOLUME);

        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        // Copy data old -> new
        if (getParameters().getConvertDiskPhase() == ConvertDiskCommandParameters.ConvertDiskPhase.CONVERT_VOLUME) {
            convertDisk();
            updatePhase(ConvertDiskCommandParameters.ConvertDiskPhase.SWITCH_IMAGE);

            return true;
        }

        // Switch image
        if (getParameters().getConvertDiskPhase() == ConvertDiskCommandParameters.ConvertDiskPhase.SWITCH_IMAGE) {
            updatePhase(ConvertDiskCommandParameters.ConvertDiskPhase.REMOVE_SOURCE_VOLUME);

            return true;
        }

        // Delete old image
        if (getParameters().getConvertDiskPhase() == ConvertDiskCommandParameters.ConvertDiskPhase.REMOVE_SOURCE_VOLUME) {
            if (!switchImage()) {
                setCommandStatus(CommandStatus.FAILED);
                return true;
            }
            runInternalAction(ActionType.DestroyImage, createDestroyImageParameters(getDiskImage().getImageId()));

            updatePhase(ConvertDiskCommandParameters.ConvertDiskPhase.COMPLETE);

            return true;
        }

        return false;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getDiskImage().getId(),
                VdcObjectType.Disk,
                getActionType().getActionGroup()));
    }

    private CreateVolumeContainerCommandParameters createVolumeCreationParameters(DiskImage diskImage, long requiredSize) {
        CreateVolumeContainerCommandParameters parameters =
                new CreateVolumeContainerCommandParameters(diskImage.getStoragePoolId(),
                        getLocationInfo().getStorageDomainId(),
                        diskImage.getId(),
                        Guid.Empty,
                        diskImage.getId(),
                        getParameters().getNewVolGuid(),
                        getVolumeFormat(diskImage),
                        getVolumeType(diskImage),
                        imagesHandler.getJsonDiskDescription(diskImage),
                        diskImage.getSize(),
                        requiredSize,
                        imageDao.getMaxSequenceNumber(diskImage.getId()) + 1);
        parameters.setLegal(false);
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.Disk, diskImage.getId()));
        return parameters;
    }

    private DestroyImageParameters createDestroyImageParameters(Guid imageId) {
        DestroyImageParameters parameters = new DestroyImageParameters(getParameters().getVdsRunningOn(),
                Guid.Empty,
                getDiskImage().getStoragePoolId(),
                getDiskImage().getStorageIds().get(0),
                getDiskImage().getId(),
                Arrays.asList(imageId),
                false,
                false);
        parameters.setParentParameters(getParameters());
        parameters.setParentCommand(getActionType());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);

        return parameters;
    }

    private void convertDisk() {
        VdsmImageLocationInfo destLocationInfo = new VdsmImageLocationInfo();
        destLocationInfo.setStorageDomainId(getDiskImage().getStorageIds().get(0));
        destLocationInfo.setImageGroupId(getDiskImage().getId());
        destLocationInfo.setImageId(getParameters().getNewVolGuid());
        CopyDataCommandParameters parameters = new CopyDataCommandParameters(getDiskImage().getStoragePoolId(),
                getParameters().getLocationInfo(),
                destLocationInfo,
                false);

        parameters.setVdsId(getParameters().getVdsRunningOn());
        parameters.setVdsRunningOn(getParameters().getVdsRunningOn());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        runInternalActionWithTasksContext(ActionType.CopyData, parameters);
    }

    private boolean switchImage() {
        DiskImage info = imagesHandler.getVolumeInfoFromVdsm(getDiskImage().getStoragePoolId(),
                getDiskImage().getStorageIds().get(0),
                getDiskImage().getId(),
                getParameters().getNewVolGuid());

        DiskImage newImage = DiskImage.copyOf(getDiskImage());
        newImage.setImageId(getParameters().getNewVolGuid());
        newImage.setSize(info.getSize());

        if (getParameters().getVolumeFormat() != null) {
            if (info.getVolumeFormat() != getParameters().getVolumeFormat()) {
                log.error("Requested format '{}' doesn't match format on storage '{}'",
                        getParameters().getVolumeFormat(), info.getVolumeFormat());
                return false;
            }

            newImage.setVolumeFormat(getParameters().getVolumeFormat());
        }

        if (getParameters().getPreallocation() != null) {
            if (info.getVolumeType() != getParameters().getPreallocation()) {
                log.error("Requested allocation policy '{}' doesn't match allocation policy on storage '{}'",
                        getParameters().getPreallocation(), info.getVolumeType());
                return false;
            }

            newImage.setVolumeType(getParameters().getPreallocation());
        }

        getDiskImage().setActive(false);
        newImage.getImage().setSequenceNumber(imageDao.getMaxSequenceNumber(getDiskImage().getId()) + 1);
        TransactionSupport.executeInNewTransaction(() -> {
            addDiskImageToDb(newImage, getCompensationContext(), true);
            imageDao.update(getDiskImage().getImage());

            return null;
        });

        return true;
    }

    private void updatePhase(ConvertDiskCommandParameters.ConvertDiskPhase phase) {
        getParameters().setConvertDiskPhase(phase);
        persistCommandIfNeeded();
    }

    private VolumeType getVolumeType(DiskImage diskImage) {
        return getParameters().getPreallocation() == null ? diskImage.getVolumeType() :
                getParameters().getPreallocation();
    }

    private VolumeFormat getVolumeFormat(DiskImage diskImage) {
        return getParameters().getVolumeFormat() == null ? diskImage.getVolumeFormat() :
                getParameters().getVolumeFormat();
    }

    private VdsmImageLocationInfo getLocationInfo() {
        return (VdsmImageLocationInfo) getParameters().getLocationInfo();
    }

    private MeasureVolumeParameters createMeasureVolumeParameters() {
        Guid storageDomainId = ((VdsmImageLocationInfo) getParameters().getLocationInfo()).getStorageDomainId();
        MeasureVolumeParameters parameters = new MeasureVolumeParameters(getDiskImage().getStoragePoolId(),
                storageDomainId,
                getDiskImage().getId(),
                getDiskImage().getImageId(),
                getVolumeFormat(getDiskImage()).getValue());
        parameters.setParentCommand(getActionType());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.PARENT_MANAGED);
        parameters.setVdsRunningOn(getParameters().getVdsRunningOn());
        parameters.setCorrelationId(getCorrelationId());
        return parameters;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CONVERT_DISK);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getDiskImage().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_CONVERTED)
                                .with("DiskAlias", getDiskImage().getDiskAlias())));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskname", getDiskImage().getName());
            jobProperties.put("allocationpolicy", getParameters().getPreallocation().toString());
            jobProperties.put("diskformat", getParameters().getVolumeFormat().toString());
        }

        return super.getJobMessageProperties();

    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        imageDao.remove(getDiskImage().getImageId());

        freeLock();
        TransactionSupport.executeInNewTransaction(() -> {
            imagesHandler.updateImageStatus(getParameters().getNewVolGuid(), ImageStatus.OK);
            DiskImage convertedImage = diskImageDao.get(getParameters().getNewVolGuid());
            if (getParameters().getBackup() != null) {
                convertedImage.setBackup(getParameters().getBackup());
                baseDiskDao.update(convertedImage);
            }

            return null;
        });

        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // Now we need to check where we failed, if we failed after the new image is created
        // in the database - there is no need to try and remove it, the old image should be removed.
        // If it wasn't created, we can try removing it from the database
        if (getParameters().getConvertDiskPhase() == ConvertDiskCommandParameters.ConvertDiskPhase.CONVERT_VOLUME ||
                getParameters().getConvertDiskPhase() == ConvertDiskCommandParameters.ConvertDiskPhase.SWITCH_IMAGE) {
            if (diskImageDao.get(getDiskImage().getImageId()) != null) {
                runInternalAction(ActionType.DestroyImage, createDestroyImageParameters(getParameters().getNewVolGuid()),
                        cloneContextAndDetachFromParent());
            }
        }

        imagesHandler.updateImageStatus(getDiskImage().getImageId(), ImageStatus.OK);

        setSucceeded(true);
    }
}
