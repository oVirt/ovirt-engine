package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.GuidUtils;

public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

    private static final int Kb = 1024;

    @Inject
    protected MacPoolPerDc poolPerDc;

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    protected VmStaticDao vmStaticDao;

    protected final OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
    private Boolean skipCommandExecution;

    private MacPool macPool;

    public VmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(parameters.getVmId());
    }

    protected MacPool getMacPool() {
        if (this.macPool == null) {
            this.macPool = poolPerDc.getMacPoolForDataCenter(getStoragePoolId(), getContext());
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

    @Override
    protected void executeCommand() {
        if (shouldSkipCommandExecutionCached()) {
            setSucceeded(true);
            return;
        }
        executeVmCommand();
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

    // The maximum number of VirtIO SCSI disks that libvirt
    // allows without creating another controller
    public static final int MAX_VIRTIO_SCSI_DISKS = 16383;

    // The maximum number of sPAPR VSCSI disks that
    // can be detected by the Linux kernel of PPC64 guests
    public static final int MAX_SPAPR_SCSI_DISKS = 7;

    private List<VmNic> interfaces;

    /**
     * This method checks that with the given parameters, the max PCI and IDE limits defined are not passed.
     */
    public static boolean checkPciAndIdeLimit(
            int osId,
            Version clusterVersion,
            int monitorsNumber,
            List<? extends VmNic> interfaces,
            List<DiskVmElement> diskVmElements,
            boolean virtioScsiEnabled,
            boolean hasWatchdog,
            boolean isBalloonEnabled,
            boolean isSoundDeviceEnabled,
            ArrayList<String> messages) {

        boolean result = true;
        // this adds: monitors + 2 * (interfaces with type rtl_pv) + (all other
        // interfaces) + (all disks that are not IDE)
        int pciInUse = monitorsNumber;

        for (VmNic a : interfaces) {
            if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.rtl8139_pv) {
                pciInUse += 2;
            } else if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.spaprVlan) {
                // Do not count sPAPR VLAN devices since they are not PCI
            } else {
                pciInUse += 1;
            }
        }

        pciInUse += diskVmElements.stream().filter(dve -> dve.getDiskInterface() == DiskInterface.VirtIO).count();

        // VirtIO SCSI controller requires one PCI slot
        pciInUse += virtioScsiEnabled ? 1 : 0;

        // VmWatchdog controller requires one PCI slot
        pciInUse += hasWatchdog ? 1 : 0;

        // Balloon controller requires one PCI slot
        pciInUse += isBalloonEnabled ? 1 : 0;

        // Sound device controller requires one PCI slot
        pciInUse += isSoundDeviceEnabled ? 1 : 0;

        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

        int maxPciSlots = osRepository.getMaxPciDevices(osId, clusterVersion);

        if (pciInUse > maxPciSlots) {
            result = false;
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS.name());
        }
        else if (MAX_IDE_SLOTS < diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.IDE).count()) {
            result = false;
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS.name());
        }
        else if (MAX_VIRTIO_SCSI_DISKS <
                diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.VirtIO_SCSI).count()) {
            result = false;
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_VIRTIO_SCSI_DISKS.name());
        }
        else if (MAX_SPAPR_SCSI_DISKS <
                diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.SPAPR_VSCSI).count()) {
            result = false;
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_SPAPR_VSCSI_DISKS.name());
        }
        return result;
    }

    protected void removeVmStatic() {
        removeVmStatic(true);
    }

    protected void removeVmStatic(boolean removePermissions) {
        getVmStaticDao().remove(getVmId(), removePermissions);
    }

    protected List<VmNic> getInterfaces() {
        if (interfaces == null) {
            interfaces = getVmNicDao().getAllForVm(getVmId());
        }

        return interfaces;
    }

    protected void removeVmNetwork() {
        if (getInterfaces() != null) {
            for (VmNic iface : getInterfaces()) {
                getMacPool().freeMac(iface.getMacAddress());
            }
        }
    }

    protected void removeVmSnapshots() {
        new SnapshotsManager().removeSnapshots(getVmId());
    }

    protected void removeVmUsers() {
        List<TagsVmMap> all = getTagDao().getTagVmMapByVmIdAndDefaultTag(getVmId());
        for (TagsVmMap tagVm : all) {
            getTagDao().detachVmFromTag(tagVm.getTagId(), getVmId());
        }
    }

    protected void endVmCommand() {
        if (getVm() != null) {
            getVmStaticDao().incrementDbGeneration(getVm().getId());
        }
        endActionOnDisks();
        unlockVm();

        setSucceeded(true);
    }

    protected List<VdcReturnValueBase> endActionOnDisks() {
        List<VdcReturnValueBase> returnValues = new ArrayList<>();
        for (VdcActionParametersBase p : getParametersForChildCommand()) {
            if (overrideChildCommandSuccess()) {
                p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            }

            VdcReturnValueBase returnValue = getBackend().endAction(
                    p.getCommandType() == VdcActionType.Unknown ? getChildActionType() : p.getCommandType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
            returnValues.add(returnValue);
        }
        return returnValues;
    }

    protected List<VdcActionParametersBase> getParametersForChildCommand() {
        return getParameters().getImagesParameters();
    }

    protected void unlockVm() {
        // Set VM property to null in order to refresh it from db
        setVm(null);
        if (getVm() != null) {
            if (getVm().getStatus() == VMStatus.ImageLocked) {
                VmHandler.unlockVm(getVm(), getCompensationContext());
            }
        } else {
            setLoggingForCommand();
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing endAction on Vm");
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

    protected VdcActionType getChildActionType() {
        return VdcActionType.Unknown;
    }

    protected boolean removeMemoryDisks(String memory) {
        List<Guid> guids = GuidUtils.getGuidListFromString(memory);

        RemoveDiskParameters removeMemoryDumpDiskParameters = new RemoveDiskParameters(guids.get(2));
        removeMemoryDumpDiskParameters.setShouldBeLogged(false);
        VdcReturnValueBase retVal = runInternalAction(VdcActionType.RemoveDisk, removeMemoryDumpDiskParameters);
        if (!retVal.getSucceeded()) {
            return false;
        }

        RemoveDiskParameters removeMemoryMetadataDiskParameters = new RemoveDiskParameters(guids.get(4));
        removeMemoryMetadataDiskParameters.setShouldBeLogged(false);
        retVal = runInternalAction(VdcActionType.RemoveDisk, removeMemoryMetadataDiskParameters);
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
        boolean nameLengthValid = vm.getName().length() <= maxLength;

        return nameLengthValid;
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
            VmHandler.checkStatusAndLockVm(getVmId());
        } else {
            VmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());
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
        if (diskVmElement.getDiskInterface() == DiskInterface.IDE) {
            addValidationMessageVariable("diskAlias", diskAlias);
            addValidationMessageVariable("vmName", getVm().getName());
            return failValidation(EngineMessage.HOT_PLUG_IDE_DISK_IS_NOT_SUPPORTED);
        }
        Set<String> diskHotpluggableInterfaces = osRepository.getDiskHotpluggableInterfaces(getVm().getOs(),
                getVm().getCompatibilityVersion());

        if (CollectionUtils.isEmpty(diskHotpluggableInterfaces)
                || !diskHotpluggableInterfaces.contains(diskVmElement.getDiskInterface().name())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        }

        return true;
    }

    protected boolean checkPayload(VmPayload payload, String isoPath) {
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
            return failValidation(nonManagedVmValidationResult.getMessage());
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
        return getSnapshotDao().get(getVm().getId(), SnapshotType.ACTIVE);
    }

    /** Helper method for failing validate on invalid VM status */
    protected boolean failVmStatusIllegal() {
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(getVm().getStatus()));
    }

    protected void unlockSnapshot(Guid snapshotId) {
        // if we got here, the target snapshot exists for sure
        getSnapshotDao().updateStatus(snapshotId, Snapshot.SnapshotStatus.OK);
    }
}
