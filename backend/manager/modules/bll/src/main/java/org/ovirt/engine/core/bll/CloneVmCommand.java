package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VmInterfacesModifyParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class CloneVmCommand<T extends CloneVmParameters> extends AddVmAndCloneImageCommand<T>
        implements SerialChildExecutingCommand {

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    protected VmDeviceUtils vmDeviceUtils;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmInitDao vmInitDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    private VM vmFromConfiguration;
    private Collection<DiskImage> diskImagesFromConfiguration;

    private Guid oldVmId;

    private VM vm;

    private VM sourceVm;

    protected CloneVmCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    public CloneVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void init() {
        super.init();
        oldVmId = getParameters().getVmId();
        setVmName(getParameters().getNewName());
        storageToDisksMap = getParameters().getStorageToDisksMap();

        // init the parameters only at first instantiation (not subsequent for end action)
        if (Guid.isNullOrEmpty(getParameters().getNewVmGuid())) {
            setupParameters();
        } else {
            // the VM id has to be the new VM id - same as the getVm is always the new VM
            setVmId(getParameters().getNewVmGuid());
        }
        getParameters().setUseCinderCommandCallback(!getSourceDisks().isEmpty());
    }

    @Override
    protected void executeVmCommand() {
        getParameters().setStage(CloneVmParameters.CloneVmStage.CREATE_VM_SNAPSHOT);
        Guid snapshotId = createVmSnapshot();
        setSnapshotId(snapshotId);
        setSucceeded(snapshotId != null);
    }

    private void setSnapshotId(Guid snapshotId) {
        getParameters().setSourceSnapshotId(snapshotId);
        diskImagesFromConfiguration = null;
        diskInfoDestinationMap = new HashMap<>();
        fillDisksToParameters();
        storageToDisksMap =
                ImagesHandler.buildStorageToDiskMap(getImagesToCheckDestinationStorageDomains(),
                        diskInfoDestinationMap);
        getParameters().setStorageToDisksMap(storageToDisksMap);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch (getParameters().getStage()) {
            case CREATE_VM_SNAPSHOT:
                getParameters().setStage(CloneVmParameters.CloneVmStage.COPY_DISKS);
                break;

            case COPY_DISKS:
                getParameters().setStage(CloneVmParameters.CloneVmStage.CLONE_VM);
                break;

            case CLONE_VM:
                getParameters().setStage(CloneVmParameters.CloneVmStage.CREATE_SNAPSHOTS);
                break;

            case CREATE_SNAPSHOTS:
                getParameters().setStage(CloneVmParameters.CloneVmStage.REMOVE_VM_SNAPSHOT);
                break;

            case REMOVE_VM_SNAPSHOT:
                if (getParameters().getVnicsWithProfiles() == null) {
                    return false;
                }
                getParameters().setStage(CloneVmParameters.CloneVmStage.MODIFY_VM_INTERFACES);
                break;

            case MODIFY_VM_INTERFACES:
                return false;

            default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getStage()) {
            case COPY_DISKS:
                copyDisks();
                break;

            case CLONE_VM:
                cloneVm();
                break;

            case CREATE_SNAPSHOTS:
                createDestSnapshots();
                break;

            case REMOVE_VM_SNAPSHOT:
                removeVmSnapshot();
                break;

            case MODIFY_VM_INTERFACES:
                modifyVmInterfaces();
                break;

            default:
        }
    }

    private Guid createVmSnapshot() {
        Snapshot activeSnapshot = snapshotDao.get(getSourceVmId(), Snapshot.SnapshotType.ACTIVE);
        ActionReturnValue returnValue = runInternalAction(
                ActionType.CreateSnapshotForVm,
                buildCreateSnapshotParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.isValid()) {
            getReturnValue().getValidationMessages().addAll(returnValue.getValidationMessages());
            getReturnValue().setValid(false);
            return null;
        } else if (!returnValue.getSucceeded()) {
            log.error("Failed to create VM snapshot");
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }

        return activeSnapshot.getId();
    }

    private CreateSnapshotForVmParameters buildCreateSnapshotParameters() {
        CreateSnapshotForVmParameters parameters = new CreateSnapshotForVmParameters(
                getSourceVmId(),
                StorageConstants.CLONE_VM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION,
                false);
        parameters.setShouldBeLogged(false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setNeedsLocking(false);
        return parameters;
    }

    protected void copyDisks() {
    }

    private void cloneVm() {
        if (!buildAndCheckDestStorageDomains()) {
            setSucceeded(false);
            return;
        }

        super.executeVmCommand();

        if (getReturnValue().getSucceeded() && getParameters().isEdited()) {
            ActionReturnValue returnValue = runInternalAction(
                    ActionType.UpdateVm,
                    createUpdateVmParameters(),
                    cloneContextWithNoCleanupCompensation());

            returnValue.setActionReturnValue(getReturnValue().getActionReturnValue());
            setReturnValue(returnValue);
        }
    }

    protected void createDestSnapshots() {
    }

    protected void removeVmSnapshot() {
        unlockEntities();

        ActionReturnValue returnValue = runInternalAction(
                ActionType.RemoveSnapshot,
                createRemoveSnapshotParameters(),
                cloneContextAndDetachFromParent());

        if (!returnValue.getSucceeded()) {
            log.error("Failed to remove VM snapshot");
        }

        // If the command fails beyond this point, endWithFailure() do not need to delete the snapshot anymore
        setSnapshotId(null);
    }

    private RemoveSnapshotParameters createRemoveSnapshotParameters() {
        RemoveSnapshotParameters parameters =
                new RemoveSnapshotParameters(getParameters().getSourceSnapshotId(), getSourceVmId());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setNeedsLocking(false);
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    private void modifyVmInterfaces() {
        ActionReturnValue returnValue = runInternalAction(
                ActionType.VmInterfacesModify,
                buildVmInterfacesModifyParameters());

        if (!returnValue.getSucceeded()) {
            log.error("Failed to modify VM interfaces");
        }
    }

    private VmInterfacesModifyParameters buildVmInterfacesModifyParameters() {
        VmInterfacesModifyParameters parameters = new VmInterfacesModifyParameters();
        parameters.setVmId(getVmId());
        parameters.setVnicsWithProfiles(getParameters().getVnicsWithProfiles());
        parameters.setOsId(getVm().getVmOsId());
        parameters.setCompatibilityVersion(getVm().getClusterCompatibilityVersion());
        parameters.setAddingNewVm(true);
        return parameters;
    }

    @Override
    protected void endWithFailure() {
        if (getParameters().getSourceSnapshotId() != null) {
            removeVmSnapshot();
        }
        super.endWithFailure();
    }

    @Override
    protected boolean validateSpaceRequirements() {
        if (getParameters().getDestStorageDomainId() != null) {
            StorageDomain destStorageDomain = storageDomainDao.get(getParameters().getDestStorageDomainId());
            StorageDomainValidator storageDomainValidator = createStorageDomainValidator(destStorageDomain);
            return validateDomainsThreshold(storageDomainValidator) &&
                    validateFreeSpace(storageDomainValidator, storageToDisksMap.values().stream().
                            flatMap(Collection::stream).collect(Collectors.toList()));
        } else {
            return super.validateSpaceRequirements();
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.error("Can not found any default active domain for one of the disks of snapshot with id '{}'",
                oldVmId);
    }

    @Override
    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return getVm().getStoragePoolId();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();

        var parentLocks = super.getExclusiveLocks();
        if (parentLocks != null) {
            locks.putAll(parentLocks);
        }
        for (DiskImage image: getImagesToCheckDestinationStorageDomains()) {
            locks.put(image.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskSharedLockMessage()));
        }

        locks.put(getSourceVmFromDb().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_CLONED));

        return locks;
    }

    /**
     * Assumption - a snapshot can be locked only if in status OK, so if validate passed
     * this is the status of the snapshot. In addition the newly added VM is in down status
     */
    @Override
    protected void lockEntities() {
        TransactionSupport.executeInNewTransaction(() -> {
            Snapshot snapshot = snapshotDao.get(getParameters().getSourceSnapshotId(), null, false);
            getCompensationContext().snapshotEntityStatus(snapshot);
            snapshotDao.updateStatus(snapshot.getId(), Snapshot.SnapshotStatus.LOCKED);
            getCompensationContext().stateChanged();
            return null;
        });
        freeLock();
    }

    @Override
    protected void unlockEntities() {
        // Assumption - this is last DB change of command, no need for compensation here
        snapshotDao.updateStatus(getParameters().getSourceSnapshotId(), Snapshot.SnapshotStatus.OK);
        vmDynamicDao.updateStatus(getVmId(), VMStatus.Down);
    }

    private List<Disk> getVmDisks() {
        QueryReturnValue vdcReturnValue = runInternalQuery(
                QueryType.GetAllDisksByVmId,
                new IdQueryParameters(oldVmId));

        return vdcReturnValue.getReturnValue() != null ? (List<Disk>) vdcReturnValue.getReturnValue() : new ArrayList<>();
    }

    private List<DiskImage> getSnapshotDisks() {
        List<DiskImage> disks = diskImageDao.getAllSnapshotsForVmSnapshot(getParameters().getSourceSnapshotId());
        for (DiskImage disk : disks) {
            DiskVmElement dve = getDiskVmElement(disk);
            if (dve != null) {
                disk.setDiskVmElements(Collections.singletonList(dve));
            }
        }
        return disks;
    }

    private DiskVmElement getDiskVmElement(BaseDisk disk) {
        return diskVmElementDao.get(new VmDeviceId(disk.getId(), oldVmId));
    }

    @Override
    protected Collection<DiskImage> getSourceDisks() {
        if (diskImagesFromConfiguration == null) {
            Collection<? extends Disk> loadedImages =
                    getParameters().getSourceSnapshotId() != null ? getSnapshotDisks() : getVmDisks();
            Predicate<Disk> activity = getParameters().getSourceSnapshotId() != null ? (d -> true) : ONLY_ACTIVE;

            diskImagesFromConfiguration = DisksFilter.filterImageDisks(loadedImages, ONLY_SNAPABLE, activity);
            diskImagesFromConfiguration.addAll(DisksFilter.filterCinderDisks(loadedImages, ONLY_PLUGGED));
            diskImagesFromConfiguration.addAll(DisksFilter.filterManagedBlockStorageDisks(loadedImages, ONLY_PLUGGED));
        }
        return diskImagesFromConfiguration;
    }

    private Collection<DiskImage> getTargetDisks() {
        Collection<DiskImage> diskImages = getSourceDisks();
        if (!getParameters().isEdited()) {
            return diskImages;
        }

        List<DiskImage> destDiskImages = new ArrayList<>();
        for (DiskImage diskImage : diskImages) {
            DiskImage paramDiskImage = getParameters().getDiskInfoDestinationMap().get(diskImage.getId());
            if (paramDiskImage == null) {
                continue;
            }
            DiskImage destDiskImage = DiskImage.copyOf(diskImage);
            destDiskImage.setStorageIds(paramDiskImage.getStorageIds());
            destDiskImage.setDiskAlias(paramDiskImage.getDiskAlias());
            destDiskImage.setDiskProfileId(paramDiskImage.getDiskProfileId());
            if (paramDiskImage.getQuotaId() != null) {
                destDiskImage.setQuotaId(paramDiskImage.getQuotaId());
            }
            if (paramDiskImage.getVolumeType() != null) {
                destDiskImage.setVolumeType(paramDiskImage.getVolumeType());
            }
            if (paramDiskImage.getVolumeFormat() != null) {
                destDiskImage.setVolumeFormat(paramDiskImage.getVolumeFormat());
            }
            destDiskImages.add(destDiskImage);
        }
        return destDiskImages;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    StringUtils.defaultString(getParameters().getNewName()));
            jobProperties.put("sourcevm",
                    StringUtils.defaultString(getSourceVmFromDb().getName()));
        }
        return jobProperties;
    }

    @Override
    protected Guid getSourceVmId() {
        return oldVmId;
    }

    @Override
    protected VM getVmFromConfiguration() {
        if (getParameters().getSourceSnapshotId() == null) {
            return getVm();
        }
        if (vmFromConfiguration == null) {
            QueryReturnValue queryReturnValue =
                    runInternalQuery(QueryType.GetVmConfigurationBySnapshot,
                            new IdQueryParameters(getParameters().getSourceSnapshotId()));
            if (queryReturnValue.getSucceeded()) {
                vmFromConfiguration = queryReturnValue.getReturnValue();
            }
        }
        return vmFromConfiguration;
    }

    @Override
    protected VM getSourceVmFromDb() {
        if (sourceVm == null) {
            sourceVm = vmDao.get(oldVmId);
        }

        return sourceVm;
    }

    @Override
    public VM getVm() {
        if (vm == null) {
            vm = vmDao.get(oldVmId);
            getVmDeviceUtils().setVmDevices(vm.getStaticData());
            vmHandler.updateDisksFromDb(vm);
            vmHandler.updateVmGuestAgentVersion(vm);
            vmHandler.updateNetworkInterfacesFromDb(vm);
            vmHandler.updateVmInitFromDB(vm.getStaticData(), true);

            vm.setName(getParameters().getNewName());
            vm.setId(getVmId());
        }

        return vm;
    }

    @Override
    protected VmInit loadOriginalVmInitWithRootPassword() {
        return vmInitDao.get(oldVmId);
    }

    private void fillDisksToParameters() {
        for (Disk image : getTargetDisks()) {
            diskInfoDestinationMap.put(image.getId(), (DiskImage) image);
        }

        getParameters().setDiskInfoDestinationMap(diskInfoDestinationMap);
    }

    @Override
    protected void removeVmRelatedEntitiesFromDb() {
        detachDisks();
        super.removeVmRelatedEntitiesFromDb();
    }

    @Override
    protected void addVmImages() {
        super.addVmImages();
        attachDisks();
    }

    private void detachDisks() {
        attachDetachDisks(ActionType.DetachDiskFromVm);
    }

    protected void attachDisks() {
        attachDetachDisks(ActionType.AttachDiskToVm);
    }

    private void attachDetachDisks(ActionType actionType) {
        QueryReturnValue vdcReturnValue = runInternalQuery(
                QueryType.GetAllDisksByVmId,
                new IdQueryParameters(oldVmId));

        List<Disk> loadedImages = vdcReturnValue.getReturnValue() != null ? (List<Disk>) vdcReturnValue.getReturnValue() : new ArrayList<>();

        for (Disk disk : loadedImages) {
            // All LUN disks, including shareable ones, are filtered out here just like when creating a template
            // from a VM. The behaviour should be consistent in both cases.
            if (disk.isShareable() && disk.getDiskStorageType() != DiskStorageType.LUN) {
                attachDetachDisk(disk, actionType);
            }
        }
    }

    private void attachDetachDisk(Disk disk, ActionType actionType) {
        DiskVmElement oldDve = disk.getDiskVmElementForVm(oldVmId);
        DiskVmElement newDve = new DiskVmElement(disk.getId(), getParameters().getNewVmGuid());
        newDve.setDiskInterface(oldDve.getDiskInterface());
        runInternalAction(
                actionType,
                new AttachDetachVmDiskParameters(newDve, oldDve.isPlugged())
        );
    }

    private void setupParameters() {
        setVmId(Guid.newGuid());
        getParameters().setNewVmGuid(getVmId());
        VM vmToClone = getVm();
        getParameters().setVm(vmToClone);

        if (!getParameters().isEdited()) {
            List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(oldVmId);
            vmDeviceUtils.updateVmDevicesInParameters(getParameters(), devices);
            fillDisksToParameters();
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));

        return permissionList;
    }

    @Override
    protected boolean validate() {
        if (getSourceVmFromDb().isStateless() && !getSourceVmFromDb().isDown()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS);
        }

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesFromConfiguration);
        if (!validate(diskImagesValidator.diskImagesNotIllegal())) {
            return false;
        }

        return super.validate();
    }

    @Override
    protected void updateOriginalTemplate(VmStatic vmStatic) {
        vmStatic.setOriginalTemplateGuid(getVm().getOriginalTemplateGuid());
        vmStatic.setOriginalTemplateName(getVm().getOriginalTemplateName());
        vmStatic.setVmtGuid(getVm().getVmtGuid());
    }

    @Override
    protected Guid getDestStorageDomain(Guid diskImageID){
        return getParameters().getDestStorageDomainId() != null ?
                getParameters().getDestStorageDomainId() :
                super.getDestStorageDomain(diskImageID);
    }

    private VmManagementParametersBase createUpdateVmParameters() {
        VM editedVM = getParameters().getVm();
        editedVM.setId(getVmId());
        editedVM.setVmCreationDate(getVm().getVmCreationDate());
        editedVM.setCreatedByUserId(getVm().getCreatedByUserId());
        editedVM.setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);

        VmManagementParametersBase params = new VmManagementParametersBase(getParameters());
        params.setLockProperties(LockProperties.create(LockProperties.Scope.None));
        params.setVm(editedVM);
        params.setVmId(editedVM.getId());
        return params;
    }
}
