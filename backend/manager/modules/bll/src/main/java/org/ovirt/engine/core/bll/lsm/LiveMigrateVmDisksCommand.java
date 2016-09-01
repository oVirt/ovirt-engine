package org.ovirt.engine.core.bll.lsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.LiveSnapshotValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@NonTransactiveCommandAttribute(forceCompensation = true)
@InternalCommandAttribute
public class LiveMigrateVmDisksCommand<T extends LiveMigrateVmDisksParameters> extends CommandBase<T>
        implements TaskHandlerCommand<LiveMigrateVmDisksParameters>, QuotaStorageDependent {

    @Inject
    private DiskProfileHelper diskProfileHelper;

    private Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
    private Map<Guid, StorageDomain> storageDomainsMap = new HashMap<>();

    public LiveMigrateVmDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);

        getParameters().setCommandType(getActionType());
        setVmId(getParameters().getVmId());
    }

    public LiveMigrateVmDisksCommand(T parameters) {
        this(parameters, null);
    }

    // ctor for compensation
    protected LiveMigrateVmDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return Arrays.<SPMAsyncTaskHandler> asList(
                new LiveSnapshotTaskHandler(this),
                new LiveMigrateDisksTaskHandler(this)
                );
    }

    /**
     * Ugly hack, but it is needed as the endAction method in this command is called only
     * once and in that case it executes the next task handler, so we have no other option
     * but to release the lock right after executing the next task handler, assuming it'll
     * take the VM lock by himself so we won't end up in a state where the VM is not locked.
     */
    @Override
    public VdcReturnValueBase endAction() {
        try {
            return super.endAction();
        }
        finally {
            freeLock();
        }
    }

    /* Overridden stubs declared as public in order to implement ITaskHandlerCommand */

    @Override
    public T getParameters() {
        return super.getParameters();
    }

    @Override
    public Guid createTask(
            Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
    }

    @Override
    public void preventRollback() {
        getParameters().setExecutionIndex(0);
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void taskEndSuccessfully() {
        // Not implemented
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageDao().get(parameters.getImageId());
            if (diskImage != null) {
                permissionList.add(new PermissionSubject(diskImage.getId(),
                        VdcObjectType.Disk,
                        ActionGroup.DISK_LIVE_STORAGE_MIGRATION));
            }
        }
        return permissionList;
    }

    @Override
    protected void executeCommand() {
        setSucceeded(true);
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

    protected DiskImageDao getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    private DiskImage getDiskImageByImageId(Guid imageId) {
        if (diskImagesMap.containsKey(imageId)) {
            return diskImagesMap.get(imageId);
        }

        DiskImage diskImage = getDiskImageDao().get(imageId);
        diskImagesMap.put(imageId, diskImage);

        return diskImage;
    }

    private Disk getDiskImageByDiskId(Guid diskId) {
        Disk disk = getDiskDao().get(diskId);
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

        StorageDomain storageDomain = getStorageDomainDao().getForStoragePool(storageDomainId, storagePoolId);
        storageDomainsMap.put(storageDomainId, storageDomain);

        return storageDomain;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__MOVE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM_DISK);
    }

    protected boolean setAndValidateDiskProfiles() {
        Map<DiskImage, Guid> map = new HashMap<>();
        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageByImageId(parameters.getImageId());
            map.put(diskImage, diskImage.getStorageIds().get(0));
        }
        return validate(diskProfileHelper.setAndValidateDiskProfiles(map,
                getStoragePool().getCompatibilityVersion(), getCurrentUser()));
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
    protected boolean canDoAction() {
        setStoragePoolId(getVm().getStoragePoolId());

        LiveSnapshotValidator validator = new LiveSnapshotValidator(getStoragePool().getCompatibilityVersion(), getVds());
        if (!validate(validator.canDoSnapshot())) {
            return false;
        }

        if (!isValidParametersList() || !checkImagesStatus() || !validateSpaceRequirements()
                || !performVmRelatedChecks()) {
            return false;
        }

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            getReturnValue().setCanDoAction(isDiskNotShareable(parameters.getImageId())
                    && isDiskSnapshotNotPluggedToOtherVmsThatAreNotDown(parameters.getImageId())
                    && isTemplateInDestStorageDomain(parameters.getImageId(), parameters.getTargetStorageDomainId())
                    && performStorageDomainsChecks(parameters)
                    && isSameSourceAndDest(parameters));

            if (!getReturnValue().getCanDoAction()) {
                return false;
            }
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        return true;
    }

    private boolean performStorageDomainsChecks(LiveMigrateDiskParameters parameters) {
        StorageDomain sourceDomain = getImageSourceDomain(parameters.getImageId());
        StorageDomain destDomain = getStorageDomainById(parameters.getTargetStorageDomainId(), getStoragePoolId());

        return validateSourceStorageDomain(sourceDomain)
                && validateDestStorage(destDomain)
                && (liveStorageMigrationSupportedBetweenDifferentStorageTypes() ||
                validateDestStorageAndSourceStorageOfSameTypes(destDomain, sourceDomain));
    }

    private StorageDomain getImageSourceDomain(Guid imageId) {
        DiskImage diskImage = getDiskImageByImageId(imageId);
        Guid domainId = diskImage.getStorageIds().get(0);
        return getStorageDomainById(domainId, getStoragePoolId());
    }

    private boolean liveStorageMigrationSupportedBetweenDifferentStorageTypes() {
        return FeatureSupported.liveStorageMigrationBetweenDifferentStorageTypesSupported(getStoragePool().getCompatibilityVersion());
    }

    private boolean validateDestStorageAndSourceStorageOfSameTypes(StorageDomain destDomain, StorageDomain sourceDomain) {
        if (destDomain.getStorageType().getStorageSubtype() != sourceDomain.getStorageType().getStorageSubtype()) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_DESTINATION_AND_SOURCE_STORAGE_SUB_TYPES_DIFFERENT);
        }
        return true;
    }

    private boolean isValidParametersList() {
        if (getParameters().getParametersList().isEmpty()) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        return true;
    }

    protected boolean checkImagesStatus() {
        List<DiskImage> disksToCheck = ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(getVmId()), true, false, true);
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(disksToCheck);
        return validate(diskImagesValidator.diskImagesNotLocked());
    }

    private boolean isSameSourceAndDest(LiveMigrateDiskParameters parameters) {
        if (parameters.getSourceStorageDomainId().equals(parameters.getTargetStorageDomainId())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
        }

        return true;
    }

    private boolean isDiskNotShareable(Guid imageId) {
        DiskImage diskImage = getDiskImageByImageId(imageId);

        if (diskImage.isShareable()) {
            addCanDoActionMessageVariable("diskAliases", diskImage.getDiskAlias());
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED);
        }

        return true;
    }

    private boolean isTemplateInDestStorageDomain(Guid imageId, Guid sourceDomainId) {
        Guid templateId = getDiskImageByImageId(imageId).getImageTemplateId();

        if (!Guid.Empty.equals(templateId)) {
            DiskImage templateImage = getDiskImageDao().get(templateId);
            if (!templateImage.getStorageIds().contains(sourceDomainId)) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }

        return true;
    }

    private boolean validateSourceStorageDomain(StorageDomain sourceDomain) {
        StorageDomainValidator validator = new StorageDomainValidator(sourceDomain);
        return validate(validator.isDomainExistAndActive());
    }

    private boolean validateDestStorage(StorageDomain destDomain) {
        StorageDomainValidator validator = new StorageDomainValidator(destDomain);
        return validate(validator.isDomainExistAndActive()) && validate(validator.domainIsValidDestination());
    }

    protected boolean validateSpaceRequirements() {
        return validateDestDomainsSpaceRequirements()
            && validateSourceDomainsSpaceRequirements();
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
                List<DiskImage> allImageSnapshots =
                        ImagesHandler.getAllImageSnapshots(diskImage.getImageId());

                diskImage.getSnapshots().addAll(allImageSnapshots);
            }

            StorageDomainValidator storageDomainValidator = createStorageDomainValidator(destDomain);
            if (!validate(storageDomainValidator.hasSpaceForClonedDisks(disksList))) {
                return false;
            }
        }

        return true;
    }

    protected boolean validateSourceDomainsSpaceRequirements() {
        Map<Guid, List<DiskImage>> storageDomainsActiveImagesMap = new HashMap<>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            DiskImage diskImage = getDiskImageByImageId(parameters.getImageId());
            MultiValueMapUtils.addToMap(parameters.getSourceStorageDomainId(),
                    diskImage,
                    storageDomainsActiveImagesMap);
        }

        for (Map.Entry<Guid, List<DiskImage>> entry : storageDomainsActiveImagesMap.entrySet()) {
            Guid sourceDomainId = entry.getKey();
            List<DiskImage> disksList = entry.getValue();
            Guid storagePoolId = disksList.get(0).getStoragePoolId();
            StorageDomain sourceDomain = getStorageDomainById(sourceDomainId, storagePoolId);

            StorageDomainValidator storageDomainValidator = createStorageDomainValidator(sourceDomain);
            List<DiskImage> dummyDisksList = ImagesHandler.getDisksDummiesForStorageAllocations(disksList);
            if (!validate(storageDomainValidator.hasSpaceForNewDisks(dummyDisksList))) {
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

    private boolean performVmRelatedChecks() {
        VmValidator vmValidator = createVmValidator();
        SnapshotsValidator snapshotValidator = createSnapshotsValidator();
        return validate(vmValidator.vmNotRunningStateless()) &&
               validate(snapshotValidator.vmNotDuringSnapshot(getVmId())) &&
               isVmNotInPreview();
    }

    protected VmValidator createVmValidator() {
        return new VmValidator(getVm());
    }

    private boolean isVmNotInPreview() {
        return validate(createSnapshotsValidator().vmNotInPreview(getVmId()));
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    protected DiskValidator createDiskValidator(Disk disk) {
        return new DiskValidator(disk);
    }

    protected StorageDomainValidator createStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }
}
