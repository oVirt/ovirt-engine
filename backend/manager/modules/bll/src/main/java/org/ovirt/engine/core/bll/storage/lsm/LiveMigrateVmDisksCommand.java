package org.ovirt.engine.core.bll.storage.lsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters.LiveMigrateStage;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@NonTransactiveCommandAttribute(forceCompensation = true)
@InternalCommandAttribute
public class LiveMigrateVmDisksCommand<T extends LiveMigrateVmDisksParameters> extends CommandBase<T>
        implements QuotaStorageDependent, SerialChildExecutingCommand {

    @Inject
    private DiskProfileHelper diskProfileHelper;

    @Inject
    private ImagesHandler imagesHandler;

    private Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
    private Map<Guid, StorageDomain> storageDomainsMap = new HashMap<>();
    private Set<Guid> movedVmDiskIds;

    public LiveMigrateVmDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);

        getParameters().setCommandType(getActionType());
        setVmId(getParameters().getVmId());
    }

    // ctor for compensation
    public LiveMigrateVmDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = diskImageDao.get(parameters.getImageId());
            if (diskImage != null) {
                permissionList.add(new PermissionSubject(diskImage.getId(),
                        VdcObjectType.Disk,
                        ActionGroup.DISK_LIVE_STORAGE_MIGRATION));
            }
        }
        return permissionList;
    }

    private Set<Guid> getMovedDiskIds() {
        if (movedVmDiskIds == null) {
            movedVmDiskIds = new LinkedHashSet<>();
            for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
                movedVmDiskIds.add(parameters.getImageGroupID());
            }
        }
        return movedVmDiskIds;
    }

    @Override
    protected void executeCommand() {
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(getMovedDiskIds(),
                ImageStatus.LOCKED,
                ImageStatus.OK,
                getCompensationContext());
        VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.CreateAllSnapshotsFromVm,
                getCreateSnapshotParameters(),
                ExecutionHandler.createInternalJobContext(getContext()));
        getParameters().setAutoGeneratedSnapshotId(vdcReturnValue.getActionReturnValue());
        persistCommand(getParameters().getParentCommand(), getCallback() != null);

        setSucceeded(true);
    }

    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    private void updateStage(LiveMigrateStage stage) {
        getParameters().setStage(stage);
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getStage() == LiveMigrateStage.CREATE_SNAPSHOT) {
            updateStage(LiveMigrateStage.LIVE_MIGRATE_DISK_EXEC_START);
            for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
                parameters.setSessionId(getParameters().getSessionId());
                parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                parameters.setParentCommand(getActionType());
                parameters.setParentParameters(getParameters());
                parameters.setDestinationImageId(((DiskImage)getDiskImageByDiskId(parameters.getImageGroupID()))
                        .getImageId());

                VdcReturnValueBase vdcReturnValue =
                        runInternalAction(VdcActionType.LiveMigrateDisk,
                                parameters, ExecutionHandler.createInternalJobContext());

                if (!vdcReturnValue.getSucceeded()) {
                    imageDao.updateStatusOfImagesByImageGroupId(parameters.getImageGroupID(), ImageStatus.OK);
                }
            }
            updateStage(LiveMigrateStage.LIVE_MIGRATE_DISK_EXEC_COMPLETED);
            return true;
        }
        if (isRemoveAutoGeneratedSnapshotRequired()) {
            updateStage(LiveMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_START);
            removeAutogeneratedSnapshot();
            updateStage(LiveMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_END);
            return true;
        }
        return false;
    }

    @Override
    public boolean ignoreChildCommandFailure() {
        return isRemoveAutoGeneratedSnapshotRequired();
    }

    private boolean isRemoveAutoGeneratedSnapshotRequired() {
        boolean removeSnapshotRequired = getParameters().getStage() != LiveMigrateStage.CREATE_SNAPSHOT &&
                getParameters().getStage() != LiveMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_END;
        if (removeSnapshotRequired) {
            if (!getVm().getStatus().isQualifiedForLiveSnapshotMerge()) {
                // If the VM is not qualified for live merge, i.e. its status is not up, the auto-generated snapshot
                // is not removed. Removing the snapshot while the VM isn't running will end up with cold merge
                // and this is not desired here.
                // Once cold merge enhanced to use qemu-img commit, this limit can be removed. See BZ 1246114.
                // This behavior can be tracked by BZ 1369942.
                log.warn("Auto-generated snapshot cannot be removed because VM isn't qualified for live merge. VM status is '{}'", getVm().getStatus());
                removeSnapshotRequired = false;
            }
        }

        return removeSnapshotRequired;
    }

    private void removeAutogeneratedSnapshot() {
        RemoveSnapshotParameters removeSnapshotParameters = new RemoveSnapshotParameters(getParameters().getAutoGeneratedSnapshotId(),
                getVmId());
        removeSnapshotParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        removeSnapshotParameters.setParentCommand(getActionType());
        removeSnapshotParameters.setParentParameters(getParameters());
        removeSnapshotParameters.setNeedsLocking(false);

        runInternalAction(VdcActionType.RemoveSnapshot,
                removeSnapshotParameters,
                ExecutionHandler.createInternalJobContext(getContext()));
    }

    private List<DiskImage> getMovedDisks() {
        Set<Guid> movedDiskIds = getMovedDiskIds();
        List<DiskImage> disks = new ArrayList<>();

        for (Guid diskId : movedDiskIds) {
            DiskImage disk = new DiskImage();
            disk.setId(diskId);
            disks.add(disk);
        }

        return disks;
    }

    protected CreateAllSnapshotsFromVmParameters getCreateSnapshotParameters() {
        CreateAllSnapshotsFromVmParameters params = new CreateAllSnapshotsFromVmParameters
                (getParameters().getVmId(), StorageConstants.LSM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION, false);

        params.setParentCommand(VdcActionType.LiveMigrateVmDisks);
        params.setSnapshotType(SnapshotType.REGULAR);
        params.setParentParameters(getParameters());
        params.setImagesParameters(getParameters().getImagesParameters());
        params.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
        params.setDisks(getMovedDisks());
        params.setDiskIdsToIgnoreInChecks(getMovedDiskIds());
        params.setNeedsLocking(false);
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);

        return params;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locksMap = new HashMap<>();
        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            locksMap.put(parameters.getImageGroupID().toString(), LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                    getDiskIsBeingMigratedMessage(getDiskImageByDiskId(parameters.getImageGroupID()))));
        }
        return locksMap;
    }

    private String getDiskIsBeingMigratedMessage(Disk disk) {
        return EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED.name()
                + String.format("$DiskName %1$s", disk != null ? disk.getDiskAlias() : "");
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm != null) {
            setVm(vm);
        }

        return vm;
    }

    private DiskImage getDiskImageByImageId(Guid imageId) {
        if (diskImagesMap.containsKey(imageId)) {
            return diskImagesMap.get(imageId);
        }

        DiskImage diskImage = diskImageDao.get(imageId);
        diskImagesMap.put(imageId, diskImage);

        return diskImage;
    }

    private Disk getDiskImageByDiskId(Guid diskId) {
        Disk disk = diskDao.get(diskId);
        if (disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage)disk;
            if (!diskImagesMap.containsKey(diskImage.getImageId())) {
                diskImagesMap.put(diskImage.getImageId(), (DiskImage)disk);
            }
        }
        return disk;
    }

    private StorageDomain getStorageDomainById(Guid storageDomainId, Guid storagePoolId) {
        if (storageDomainsMap.containsKey(storageDomainId)) {
            return storageDomainsMap.get(storageDomainId);
        }

        StorageDomain storageDomain = storageDomainDao.getForStoragePool(storageDomainId, storagePoolId);
        storageDomainsMap.put(storageDomainId, storageDomain);

        return storageDomain;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__MOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    protected boolean setAndValidateDiskProfiles() {
        Map<DiskImage, Guid> map = new HashMap<>();
        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageByImageId(parameters.getImageId());
            map.put(diskImage, diskImage.getStorageIds().get(0));
        }
        return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageByImageId(parameters.getImageId());

            list.add(new QuotaStorageConsumptionParameter(
                    parameters.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    parameters.getTargetStorageDomainId(),
                    (double) diskImage.getSizeInGigabytes()));

            if (diskImage.getQuotaId() != null && !Guid.Empty.equals(diskImage.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        diskImage.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        parameters.getSourceDomainId(),
                        (double) diskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    @Override
    protected boolean validate() {
        setStoragePoolId(getVm().getStoragePoolId());

        if (!isValidParametersList() || !validateDestDomainsSpaceRequirements()) {
            return false;
        }

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            getReturnValue().setValid(isDiskNotShareable(parameters.getImageId())
                    && isDiskSnapshotNotPluggedToOtherVmsThatAreNotDown(parameters.getImageId())
                    && isTemplateInDestStorageDomain(parameters.getImageId(), parameters.getTargetStorageDomainId())
                    && validateDestStorage(getStorageDomainById(parameters.getTargetStorageDomainId(), getStoragePoolId()))
                    && isSameSourceAndDest(parameters)
                    && validatePassDiscardSupportedOnDestinationStorageDomain(parameters));

            if (!getReturnValue().isValid()) {
                return false;
            }
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        return validateCreateAllSnapshotsFromVmCommand();
    }

    private boolean isValidParametersList() {
        if (getParameters().getParametersList().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        return true;
    }

    private boolean isSameSourceAndDest(LiveMigrateDiskParameters parameters) {
        if (parameters.getSourceStorageDomainId().equals(parameters.getTargetStorageDomainId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
        }

        return true;
    }

    protected boolean validatePassDiscardSupportedOnDestinationStorageDomain(LiveMigrateDiskParameters parameters) {
        DiskVmElementValidator validator =
                createDiskVmElementValidator(parameters.getImageGroupID(), parameters.getVmId());
        return validate(validator.isPassDiscardSupported(parameters.getTargetStorageDomainId()));
    }

    protected DiskVmElementValidator createDiskVmElementValidator(Guid diskId, Guid vmId) {
        return new DiskVmElementValidator(diskDao.get(diskId), diskVmElementDao.get(new VmDeviceId(diskId, vmId)));
    }

    private boolean isDiskNotShareable(Guid imageId) {
        DiskImage diskImage = getDiskImageByImageId(imageId);

        if (diskImage.isShareable()) {
            addValidationMessageVariable("diskAliases", diskImage.getDiskAlias());
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED);
        }

        return true;
    }

    private boolean isTemplateInDestStorageDomain(Guid imageId, Guid sourceDomainId) {
        Guid templateId = getDiskImageByImageId(imageId).getImageTemplateId();

        if (!Guid.Empty.equals(templateId)) {
            DiskImage templateImage = diskImageDao.get(templateId);
            if (!templateImage.getStorageIds().contains(sourceDomainId)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }

        return true;
    }

    private boolean validateDestStorage(StorageDomain destDomain) {
        StorageDomainValidator validator = new StorageDomainValidator(destDomain);
        return validate(validator.isDomainExistAndActive()) && validate(validator.domainIsValidDestination());
    }

    protected boolean validateDestDomainsSpaceRequirements() {
        Map<Guid, List<DiskImage>> storageDomainsImagesMap = new HashMap<>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            MultiValueMapUtils.addToMap(parameters.getTargetStorageDomainId(),
                    getDiskImageByImageId(parameters.getImageId()),
                    storageDomainsImagesMap);
        }

        for (Map.Entry<Guid, List<DiskImage>> entry : storageDomainsImagesMap.entrySet()) {
            Guid destDomainId = entry.getKey();
            List<DiskImage> disksList = entry.getValue();
            Guid storagePoolId = disksList.get(0).getStoragePoolId();
            StorageDomain destDomain = getStorageDomainById(destDomainId, storagePoolId);

            if (!isStorageDomainWithinThresholds(destDomain)) {
                return false;
            }

            for (DiskImage diskImage : disksList) {
                List<DiskImage> allImageSnapshots = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());

                diskImage.getSnapshots().addAll(allImageSnapshots);
            }

            StorageDomainValidator storageDomainValidator = createStorageDomainValidator(destDomain);
            if (!validate(storageDomainValidator.hasSpaceForClonedDisks(disksList))) {
                return false;
            }
        }

        return true;
    }

    protected boolean isDiskSnapshotNotPluggedToOtherVmsThatAreNotDown(Guid imageId) {
        return validate(createDiskValidator(getDiskImageByImageId(imageId)).isDiskPluggedToVmsThatAreNotDown(true, null));
    }

    protected boolean isStorageDomainWithinThresholds(StorageDomain storageDomain) {
        return validate(new StorageDomainValidator(storageDomain).isDomainWithinThresholds());
    }

    protected VmValidator createVmValidator() {
        return new VmValidator(getVm());
    }

    protected DiskValidator createDiskValidator(Disk disk) {
        return new DiskValidator(disk);
    }

    protected StorageDomainValidator createStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }

    protected boolean validateCreateAllSnapshotsFromVmCommand() {
        VdcReturnValueBase returnValue = CommandHelper.validate(VdcActionType.CreateAllSnapshotsFromVm,
                getCreateSnapshotParameters(), getContext().clone());
        if (!returnValue.isValid()) {
            getReturnValue().setValidationMessages(returnValue.getValidationMessages());
            return false;
        }
        return true;
    }
}
