package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfDataUpdater;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeleteAllVmCheckpointsParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.VmBackupType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsAndPoolIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSuccessListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

    private static final int Kb = 1024;

    @Inject
    protected MacPoolPerCluster macPoolPerCluster;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Inject
    private SnapshotsManager snapshotsManager;
    @Inject
    protected SnapshotsValidator snapshotsValidator;
    @Inject
    protected VmHandler vmHandler;
    @Inject
    protected VmTemplateHandler vmTemplateHandler;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private TagDao tagDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    protected StorageDomainDao storageDomainDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    private OvfDataUpdater ovfDataUpdater;
    @Inject
    private VmBackupDao vmBackupDao;
    @Inject
    private VmCheckpointDao vmCheckpointDao;

    @Inject
    protected ImagesHandler imagesHandler;

    @Inject
    protected OsRepository osRepository;

    private Boolean skipCommandExecution;

    private MacPool macPool;

    public VmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(parameters.getVmId());
    }

    protected VmManager getVmManager() {
        return resourceManager.getVmManager(getVmId());
    }

    protected MacPool getMacPool() {
        if (this.macPool == null) {
            this.macPool = macPoolPerCluster.getMacPoolForCluster(getClusterId(), getContext());
        }

        return this.macPool;
    }

    protected CpuFlagsManagerHandler getCpuFlagsManagerHandler() {
        return cpuFlagsManagerHandler;
    }

    @Override
    public Guid getStoragePoolId() {
        if (super.getStoragePoolId() == null) {
            VM vm = getVm();
            if (vm != null) {
                setStoragePoolId(vm.getStoragePoolId());
            }
        }
        return super.getStoragePoolId();
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmCommand(Guid commandId) {
        super(commandId);
    }

    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        VM vm = getVm();
        if (vm != null && vm.getOrigin() == OriginType.HOSTED_ENGINE && !isSystemSuperUser()) {
            addValidationMessage(EngineMessage.NON_ADMIN_USER_NOT_AUTHORIZED_TO_PERFORM_ACTION_ON_HE);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        if (shouldSkipCommandExecutionCached()) {
            setSucceeded(true);
            return;
        }
        executeVmCommand();

        if (shouldUpdateHostedEngineOvf() && getVm() != null && getVm().isHostedEngine() && getSucceeded()) {
            updateHeOvf();
        }
    }

    protected void executeVmCommand() {
        // The default action is no action.
        // Other command may override this behavior.
    }

    @Override
    protected String getDescription() {
        return getVmName();
    }

    // 3 IDE slots: 4 total minus 1 for CD
    public static final int MAX_IDE_SLOTS = 3;

    // 5 SATA slots: 6 total minus 1 for CD
    public static final int MAX_SATA_SLOTS = 5;

    // The maximum number of VirtIO SCSI disks that libvirt
    // allows without creating another controller
    public static final int MAX_VIRTIO_SCSI_DISKS = 16383;

    // The maximum number of sPAPR VSCSI disks that
    // can be detected by the Linux kernel of PPC64 guests
    public static final int MAX_SPAPR_SCSI_DISKS = 7;

    // The maximum number of virtio disks that
    // can be detected by the Linux kernel of S390X guests
    public static final int MAX_VIRTIO_CCW_DISKS = 4 * 65536;

    private List<VmNic> interfaces;

    protected void removeVmStatic() {
        removeVmStatic(true);
    }

    protected void removeVmStatic(boolean removePermissions) {
        vmStaticDao.remove(getVmId(), removePermissions);
    }

    protected List<VmNic> getInterfaces() {
        if (interfaces == null) {
            interfaces = vmNicDao.getAllForVm(getVmId());
        }

        return interfaces;
    }

    protected void removeVmNetwork() {
        MacPool macPool = getMacPool();
        if (getInterfaces() != null) {
            for (VmNic iface : getInterfaces()) {
                macPool.freeMac(iface.getMacAddress());
            }
        }
    }

    protected void removeVmSnapshots() {
        getSnapshotsManager().removeSnapshots(getVmId());
    }

    protected void removeVmUsers() {
        List<TagsVmMap> all = tagDao.getTagVmMapByVmIdAndDefaultTag(getVmId());
        for (TagsVmMap tagVm : all) {
            tagDao.detachVmFromTag(tagVm.getTagId(), getVmId());
        }
    }

    protected void endVmCommand() {
        endActionOnDisks();
        if (getVm() != null) {
            vmStaticDao.incrementDbGeneration(getVm().getId());
        }
        unlockVm();

        setSucceeded(true);
    }

    protected List<ActionReturnValue> endActionOnDisks() {
        List<ActionReturnValue> returnValues = new ArrayList<>();
        for (ActionParametersBase p : getParametersForChildCommand()) {
            if (overrideChildCommandSuccess()) {
                p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            }

            ActionReturnValue returnValue = backend.endAction(
                    p.getCommandType() == ActionType.Unknown ? getChildActionType() : p.getCommandType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
            returnValues.add(returnValue);
        }
        return returnValues;
    }

    protected List<ActionParametersBase> getParametersForChildCommand() {
        return getParameters().getImagesParameters();
    }

    protected void unlockVm() {
        // Set VM property to null in order to refresh it from db
        setVm(null);
        if (getVm() != null) {
            if (getVm().getStatus() == VMStatus.ImageLocked) {
                vmHandler.unlockVm(getVm(), getCompensationContext());
            }
        } else {
            setLoggingForCommand();
            log.warn("VM is null - no unlocking");
        }
    }

    protected void setLoggingForCommand() {
        setCommandShouldBeLogged(false);
    }

    /**
     * @return By default, <code>true</code> to override the child's success flag with the command's success flag.
     */
    protected boolean overrideChildCommandSuccess() {
        return true;
    }

    @Override
    protected void endSuccessfully() {
        endVmCommand();
    }

    @Override
    protected void endWithFailure() {
        endVmCommand();
    }

    protected ActionType getChildActionType() {
        return ActionType.Unknown;
    }

    protected boolean removeMemoryDisks(Snapshot snapshot) {
        return removeMemoryDisks(snapshot.getMemoryDiskId(), snapshot.getMetadataDiskId());
    }

    protected boolean removeMemoryDisks(Guid memoryDiskId, Guid metadataDiskId) {
        RemoveDiskParameters removeMemoryDumpDiskParameters = new RemoveDiskParameters(memoryDiskId);
        removeMemoryDumpDiskParameters.setShouldBeLogged(false);
        ActionReturnValue retVal = runInternalAction(ActionType.RemoveDisk, removeMemoryDumpDiskParameters);
        if (!retVal.getSucceeded()) {
            return false;
        }

        RemoveDiskParameters removeMemoryMetadataDiskParameters = new RemoveDiskParameters(metadataDiskId);
        removeMemoryMetadataDiskParameters.setShouldBeLogged(false);
        retVal = runInternalAction(ActionType.RemoveDisk, removeMemoryMetadataDiskParameters);
        if (!retVal.getSucceeded()) {
            return false;
        }

        return true;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    /**
     * Checks if VM name has valid length (check that it's too long). This is used for validation by descending
     * commands.
     *
     * @param vm
     *            the VM to check
     * @return true if the name is valid; false if it's too long
     */
    protected boolean isVmNameValidLength(VM vm) {
        int maxLength = Config.<Integer> getValue(ConfigValues.MaxVmNameLength);
        return vm.getName().length() <= maxLength;
    }

    /**
     * Lock the VM.<br>
     * If the command is run internally then compensation won't be used, since it might cause a deadlock if the calling
     * command has already updated the VM's row in the DB but hasn't committed before calling the child command.<br>
     * Otherwise, compensation will be used, and the VM will be locked in a new transaction, so that the lock gets
     * reflected in the DB immediately.
     */
    protected void lockVmWithCompensationIfNeeded() {
        log.info("Locking VM(id = '{}') {} compensation.", getVmId(), isInternalExecution() ? "without" : "with");

        if (isInternalExecution()) {
            vmHandler.checkStatusAndLockVm(getVmId());
        } else {
            vmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());
        }
    }

    /**
     * The following method should check if os of guest is supported for nic hot plug/unplug operation
     */
    protected boolean isNicSupportedForPlugUnPlug() {
        if (osRepository.hasNicHotplugSupport(getVm().getOs(), getVm().getCompatibilityVersion())) {
            return true;
        }

        return failValidation(EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
    }

    /**
     * The following method should check if os of guest is supported for disk hot plug/unplug operation
     */
    protected boolean isDiskSupportedForPlugUnPlug(DiskVmElement diskVmElement, String diskAlias) {
        switch (diskVmElement.getDiskInterface()) {
            case IDE:
                addValidationMessageVariable("diskAlias", diskAlias);
                addValidationMessageVariable("vmName", getVm().getName());
                return failValidation(EngineMessage.HOT_PLUG_IDE_DISK_IS_NOT_SUPPORTED);
            case SPAPR_VSCSI:
                addValidationMessageVariable("diskAlias", diskAlias);
                addValidationMessageVariable("vmName", getVm().getName());
                return failValidation(EngineMessage.HOT_PLUG_SPAPR_VSCSI_DISK_IS_NOT_SUPPORTED);
            default:
        }

        Set<String> diskHotpluggableInterfaces = osRepository.getDiskHotpluggableInterfaces(getVm().getOs(),
                getVm().getCompatibilityVersion());

        if (CollectionUtils.isEmpty(diskHotpluggableInterfaces)
                || !diskHotpluggableInterfaces.contains(diskVmElement.getDiskInterface().name())) {
            return failValidation(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED,
                    String.format("$osName %s", osRepository.getOsName(getVm().getOs())));
        }

        return true;
    }

    protected boolean checkPayload(VmPayload payload) {
        boolean returnValue = true;
        if (payload.getDeviceType() != VmDeviceType.CDROM && payload.getDeviceType() != VmDeviceType.FLOPPY) {
            addValidationMessage(EngineMessage.VMPAYLOAD_INVALID_PAYLOAD_TYPE);
            returnValue = false;
        } else {
            for (String content : payload.getFiles().values()) {
                // Check each file individually, no constraint on total size
                if (!VmPayload.isPayloadSizeLegal(content)) {
                    Integer lengthInKb = 2 * Config.<Integer> getValue(ConfigValues.PayloadSize) / Kb;
                    addValidationMessage(EngineMessage.VMPAYLOAD_SIZE_EXCEEDED);
                    addValidationMessageVariable("size", lengthInKb.toString());
                    returnValue = false;
                    break;
                }
            }
        }
        return returnValue;
    }

    protected boolean canRunActionOnNonManagedVm() {
        ValidationResult nonManagedVmValidationResult = VmHandler.canRunActionOnNonManagedVm(getVm(), this.getActionType());
        if (!nonManagedVmValidationResult.isValid()) {
            return failValidation(nonManagedVmValidationResult.getMessages());
        }
        return true;
    }

    /**
     * use this method to get the result of shouldSkipCommandExecution
     * as it is also allow to cache the result for multiple calls
     */
    protected final boolean shouldSkipCommandExecutionCached() {
        if (skipCommandExecution == null) {
            skipCommandExecution = shouldSkipCommandExecution();
        }
        return skipCommandExecution;
    }

    /**
     * check for special conditions that will cause the command to skip its validate and execution
     * this method should be overridden with specific logic for each command
     * using the result should be done with shouldSkipCommandExecutionCached method that cache the result in the command
     * @return true if the command should not execute any logic and should not return errors to the user
     */
    protected boolean shouldSkipCommandExecution() {
        return false;
    }

    protected Snapshot getActiveSnapshot() {
        return snapshotDao.get(getVm().getId(), SnapshotType.ACTIVE);
    }

    /** Helper method for failing validate on invalid VM status */
    protected boolean failVmStatusIllegal() {
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(getVm().getStatus()));
    }

    protected void unlockSnapshot(Guid snapshotId) {
        // if we got here, the target snapshot exists for sure
        snapshotDao.updateStatus(snapshotId, Snapshot.SnapshotStatus.OK);
    }

    protected VmDeviceUtils getVmDeviceUtils() {
        return vmDeviceUtils;
    }

    protected VmHandler getVmHandler() {
        return vmHandler;
    }

    protected SnapshotsManager getSnapshotsManager() {
        return snapshotsManager;
    }

    protected boolean validateQuota(Guid quotaId) {
        // QuotaManager will use default quota if the id is null or empty
        QuotaValidator validator = createQuotaValidator(quotaId);
        return validate(validator.isValid()) &&
                validate(validator.isDefinedForStoragePool(getStoragePoolId()));
    }

    protected String cdPathWindowsToLinux(String windowsPath, Guid storagePoolId, Guid vdsId) {
        if (StringUtils.isEmpty(windowsPath)) {
            return ""; // empty string is used for 'eject'
        }

        if (windowsPath.matches(ValidationUtils.GUID)) {
            BaseDisk disk = diskDao.get(Guid.createGuidFromString(windowsPath));
            if (disk != null) {
                return vmInfoBuildUtils.getPathToImage((DiskImage) disk);
            }
        }
        return cdPathWindowsToLinux(windowsPath, getIsoPrefix(storagePoolId, vdsId));
    }

    private String cdPathWindowsToLinux(String windowsPath, String isoPrefix) {
        String fileName = new File(windowsPath).getName();
        return String.format("%1$s/%2$s", isoPrefix, fileName);
    }

    protected Map<String, String> buildCdPdivFromPath(Guid diskGuid) {
        Map<String, String> pdiv = null;
        DiskImage disk = (DiskImage) diskDao.get(diskGuid);
        if (disk != null) {
            pdiv = PDIVMapBuilder.create()
                    .setPoolId(disk.getStoragePoolId())
                    .setDomainId(disk.getStorageIds().get(0))
                    .setImageGroupId(disk.getId())
                    .setVolumeId(disk.getImageId()).build();
        }

        return pdiv;
    }

    protected boolean removeVmLease(Guid leaseStorageDomainId, Guid vmId) {
        if (leaseStorageDomainId == null) {
            return true;
        }
        VmLeaseParameters params = new VmLeaseParameters(getStoragePoolId(), leaseStorageDomainId, vmId);
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEntityInfo(getParameters().getEntityInfo() != null ? getParameters().getEntityInfo()
                : new EntityInfo(VdcObjectType.VM, vmId));
        ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.RemoveVmLease, params);
        if (returnValue.getSucceeded()) {
            getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
        }
        return returnValue.getSucceeded();
    }

    protected boolean shouldAddLease(VmStatic vm) {
        return vm.getLeaseStorageDomainId() != null;
    }

    protected boolean validateLeaseStorageDomain(Guid leaseStorageDomainId) {
        StorageDomain domain = storageDomainDao.getForStoragePool(leaseStorageDomainId, getStoragePoolId());
        StorageDomainValidator validator = new StorageDomainValidator(domain);
        return validate(validator.isDomainExistAndActive()) && validate(validator.isDataDomain());
    }

    protected boolean addVmLease(Guid leaseStorageDomainId, Guid vmId, boolean hotPlugLease) {
        if (leaseStorageDomainId == null) {
            return true;
        }
        VmLeaseParameters params = new VmLeaseParameters(getStoragePoolId(), leaseStorageDomainId, vmId);
        if (hotPlugLease) {
            params.setVdsId(getVm().getRunOnVds());
            params.setHotPlugLease(true);
        }
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEntityInfo(getParameters().getEntityInfo() != null ? getParameters().getEntityInfo()
                : new EntityInfo(VdcObjectType.VM, vmId));
        ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.AddVmLease, params);
        if (returnValue.getSucceeded()) {
            getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
        }
        return returnValue.getSucceeded();
    }

    protected String getIsoPrefix(Guid storagePoolId, Guid vdsId) {
        return (String) runVdsCommand(VDSCommandType.IsoPrefix,
                new VdsAndPoolIDVDSParametersBase(vdsId, storagePoolId)).getReturnValue();
    }

    public QuotaValidator createQuotaValidator(Guid quotaId) {
        return QuotaValidator.createInstance(quotaId, true);
    }

    /**
     * Override if the command should immediately update the OVF of the hosted engine VM.
     *
     * @return false by default
     */
    protected boolean shouldUpdateHostedEngineOvf() {
        return false;
    }

    private void updateHeOvf() {
        // If there is no current transaction, trigger ovf update immediately
        if (TransactionSupport.current() == null) {
            ovfDataUpdater.triggerNow();
            return;
        }

        // If there is a transaction, trigger update after it is committed.
        registerRollbackHandler((TransactionSuccessListener) () -> ovfDataUpdater.triggerNow());
    }

    /**
     Verifies that the operation allowed on a managed block storage domain
     */
    public boolean isSupportedByManagedBlockStorageDomain(StorageDomain storageDomain) {
        if (storageDomain.getStorageType().isManagedBlockStorage()) {
            return validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()));
        }
        return true;
    }

    public boolean isVmDuringBackup() {
        List<VmBackup> vmBackups = vmBackupDao.getAllForVm(getVmId());

        // No need to block during hybrid backup
        boolean allBackupsHybrid = !CollectionUtils.isEmpty(vmBackups) && vmBackups
                .stream()
                .allMatch(vmBackup -> VmBackupType.Hybrid == vmBackup.getBackupType());
        if (allBackupsHybrid && !shouldBlockDuringBackup()) {
            return false;
        }

        return !CollectionUtils.isEmpty(vmBackups) && vmBackups
                .stream()
                .anyMatch(vmBackup -> !vmBackup.getPhase().isBackupFinished());
    }

    protected boolean shouldBlockDuringBackup() {
        return true;
    }

    protected VdsManager getVdsManager(Guid vdsId) {
        return resourceManager.getVdsManager(vdsId);
    }

    public void clearDiskBitmaps(List<DiskImage> diskImages) {
        // Remove the bitmap from all the disk snapshots.
        for (DiskImage image : diskImages) {
            // RAW volumes do not support bitmaps
            if (!image.isQcowFormat()) {
                continue;
            }

            log.info("Clear all bitmaps from VM '{}' for volume '{}'.", getVmName(), image.getId());
            VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(
                    image.getStorageIds().get(0),
                    image.getId(),
                    image.getImageId(),
                    null);

            VolumeBitmapCommandParameters parameters =
                    new VolumeBitmapCommandParameters(
                            getStoragePoolId(),
                            locationInfo,
                            null);
            parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());

            ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.ClearVolumeBitmaps, parameters);
            if (!returnValue.getSucceeded()) {
                log.error("Failed to remove all bitmaps for VM '{}' volume '{}'", getVmName(), image.getId());
            }
        }
    }

    /**
     * Removes the given bitmap from a volume of the disk, best effort - failures are logged, not thrown.
     *
     * @return {@code true} when the bitmap was removed successfully
     */
    protected boolean removeDiskBitmap(DiskImage volume, String bitmapName) {
        return removeDiskBitmap(volume, bitmapName, null);
    }

    /**
     * Removes the given bitmap from a volume of the disk, best effort - failures are logged, not thrown.
     *
     * @param vdsId a specific host to run the removal on, {@code null} to let the engine pick one
     * @return {@code true} when the bitmap was removed successfully
     */
    protected boolean removeDiskBitmap(DiskImage volume, String bitmapName, Guid vdsId) {
        VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(
                volume.getStorageIds().get(0),
                volume.getId(),
                volume.getImageId(),
                null);

        VolumeBitmapCommandParameters parameters =
                new VolumeBitmapCommandParameters(getStoragePoolId(), locationInfo, bitmapName);
        if (vdsId != null) {
            parameters.setVdsId(vdsId);
        }
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());

        ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.RemoveVolumeBitmap, parameters);
        if (!returnValue.getSucceeded()) {
            log.warn("Failed to remove bitmap '{}' from VM '{}' volume '{}'",
                    bitmapName,
                    getVmName(),
                    volume.getImageId());
            return false;
        }
        return true;
    }

    /**
     * Marks all the checkpoints of the VM as invalid: the bitmaps backing them cannot be used anymore, so a full VM
     * backup is required before the next incremental one.
     * <p>
     * This is all-or-nothing by design, even when only one bitmap looks broken: every checkpoint of the chain has a
     * recording segment on the active volume (each snapshot clones all the previous bitmaps to the new leaf), and an
     * unclean shutdown marks that volume's entire bitmap directory as in-use. A single broken checkpoint therefore
     * leaves no older checkpoint that an incremental backup could safely start from.
     */
    protected void invalidateAllCheckpoints() {
        log.info("Invalidating all VM '{}' checkpoints, full VM backup is now needed.", getVmName());
        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.invalidateAllCheckpointsByVmId(getVmId());
            return null;
        });
    }

    /**
     * Removes all the checkpoints of the VM: from libvirt (best effort, only meaningful for a running VM), the
     * Engine database and the bitmaps left behind on storage.
     */
    protected void removeAllCheckpoints() {
        List<VmCheckpoint> vmCheckpoints = vmCheckpointDao.getAllForVm(getVmId());
        if (vmCheckpoints.isEmpty()) {
            return;
        }

        // Best effort to remove all the checkpoints in the chain from libvirt,
        // starting from the oldest checkpoint to the leaf. Without this the
        // libvirt checkpoints would stay defined on the running VM forever,
        // referencing bitmaps that no longer exist on storage.
        if (getVdsId() != null) {
            for (VmCheckpoint vmCheckpoint : vmCheckpoints) {
                try {
                    runVdsCommand(VDSCommandType.DeleteVmCheckpoints,
                            new VmCheckpointsVDSParameters(getVdsId(), getVmId(), List.of(vmCheckpoint)));
                } catch (RuntimeException e) {
                    log.warn("Failed to remove VM '{}' checkpoint '{}' from libvirt: {}",
                            getVmId(),
                            vmCheckpoint.getId(),
                            e.getMessage());
                }
            }
        }

        // The disks are taken from the engine's own checkpoints and not from what libvirt reported: when libvirt has
        // no checkpoints defined at all we still have to clear the bitmaps that are left behind on storage.
        List<DiskImage> imagesWithCheckpoints = vmCheckpoints.stream()
                .map(vmCheckpoint -> vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId()))
                .flatMap(List::stream)
                .filter(distinctByImageId())
                .collect(Collectors.toCollection(ArrayList::new));

        DeleteAllVmCheckpointsParameters deleteAllVmCheckpointsParameters =
                new DeleteAllVmCheckpointsParameters(getVmId(), imagesWithCheckpoints);
        deleteAllVmCheckpointsParameters.setParentCommand(getActionType());
        deleteAllVmCheckpointsParameters.setParentParameters(getParameters());
        deleteAllVmCheckpointsParameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        // The bitmaps are already known to be broken, a best effort cleanup is better than leaving them behind.
        deleteAllVmCheckpointsParameters.setForce(true);

        runInternalAction(ActionType.DeleteAllVmCheckpoints, deleteAllVmCheckpointsParameters);
    }

    private static Predicate<DiskImage> distinctByImageId() {
        Set<Guid> seen = new HashSet<>();
        return diskImage -> seen.add(diskImage.getImageId());
    }
}
