package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.tasks.TaskManagerUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.TagDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.springframework.util.CollectionUtils;

public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

    public static final String DELETE_PRIMARY_IMAGE_TASK_KEY = "DELETE_PRIMARY_IMAGE_TASK_KEY";
    public static final String DELETE_SECONDARY_IMAGES_TASK_KEY = "DELETE_SECONDARY_IMAGES_TASK_KEY";
    private static final int Kb = 1024;
    protected final static int MAX_NETWORK_INTERFACES_SUPPORTED = 8;

    protected final OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public VmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(parameters.getVmId());
    }


    public VmCommand(T parameters) {
        this(parameters, null);
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
     *
     * @param commandId
     */
    protected VmCommand(Guid commandId) {
        super(commandId);
    }

    public VmCommand() {
    }


    @Override
    protected void executeCommand() {
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
    public final static int MAX_IDE_SLOTS = 3;

    // The maximum number of VirtIO SCSI disks that libvirt
    // allows without creating another controller
    public final static int MAX_VIRTIO_SCSI_DISKS = 16383;

    // The maximum number of sPAPR VSCSI disks that
    // can be detected by the Linux kernel of PPC64 guests
    public final static int MAX_SPAPR_SCSI_DISKS = 8;

    private List<VmNic> interfaces;

    /**
     * This method checks that with the given parameters, the max PCI and IDE limits defined are not passed.
     *
     * @param osId
     * @param clusterVersion
     * @param monitorsNumber
     * @param interfaces
     * @param disks
     * @param virtioScsiEnabled
     * @param hasWatchdog
     * @param isBalloonEnabled
     * @param isSoundDeviceEnabled
     * @param messages
     * @return a boolean
     */
    public static <T extends Disk> boolean checkPciAndIdeLimit(
            int osId,
            Version clusterVersion,
            int monitorsNumber,
            List<VmNic> interfaces,
            List<T> disks,
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
            } else {
                pciInUse += 1;
            }
        }

        pciInUse += LinqUtils.filter(disks, new Predicate<T>() {
            @Override
            public boolean eval(T a) {
                return a.getDiskInterface() == DiskInterface.VirtIO;
            }
        }).size();

        // VirtIO SCSI controller requires one PCI slot
        pciInUse += virtioScsiEnabled ? 1 : 0;

        // VmWatchdog controller requires one PCI slot
        pciInUse += hasWatchdog ? 1 : 0;

        // Balloon controller requires one PCI slot
        pciInUse += isBalloonEnabled ? 1 : 0;

        // Sound device controller requires one PCI slot
        pciInUse += isSoundDeviceEnabled ? 1 : 0;

        OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

        int maxPciSlots = osRepository.getMaxPciDevices(osId, clusterVersion);

        if (pciInUse > maxPciSlots) {
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS.name());
        }
        else if (MAX_IDE_SLOTS < LinqUtils.filter(disks, new Predicate<T>() {
            @Override
            public boolean eval(T a) {
                return a.getDiskInterface() == DiskInterface.IDE;
            }
        }).size()) {
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS.name());
        }
        else if (MAX_VIRTIO_SCSI_DISKS < LinqUtils.filter(disks, new Predicate<T>() {
            @Override
            public boolean eval(T a) {
                return a.getDiskInterface() == DiskInterface.VirtIO_SCSI;
            }
        }).size()) {
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_VIRTIO_SCSI_DISKS.name());
        }
        else if (MAX_SPAPR_SCSI_DISKS < LinqUtils.filter(disks, new Predicate<T>() {
            @Override
            public boolean eval(T a) {
                return a.getDiskInterface() == DiskInterface.SPAPR_VSCSI;
            }
        }).size()) {
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_SPAPR_VSCSI_DISKS.name());
        }
        return result;
    }

    protected void removeVmStatic() {
        removeVmStatic(true);
    }

    protected void removeVmStatic(boolean removePermissions) {
        getVmStaticDAO().remove(getVmId(), removePermissions);
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

    protected Set<String> removeVmSnapshots() {
        return new SnapshotsManager().removeSnapshots(getVmId());
    }

    protected void removeVmUsers() {
        List<TagsVmMap> all = getTagDao().getTagVmMapByVmIdAndDefaultTag(getVmId());
        for (TagsVmMap tagVm : all) {
            getTagDao().detachVmFromTag(tagVm.gettag_id(), getVmId());
        }
    }

    protected void endVmCommand() {
        if (getVm() != null) {
            getVmStaticDAO().incrementDbGeneration(getVm().getId());
        }
        endActionOnDisks();
        unlockVm();
        setSucceeded(true);
    }

    protected void endActionOnDisks() {
        for (VdcActionParametersBase p : getParametersForChildCommand()) {
            if (overrideChildCommandSuccess()) {
                p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            }

            getBackend().endAction(
                    p.getCommandType() == VdcActionType.Unknown ? getChildActionType() : p.getCommandType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    protected List<VdcActionParametersBase> getParametersForChildCommand() {
        return getParameters().getImagesParameters();
    }

    protected void unlockVm() {
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

    protected boolean removeMemoryVolumes(String memVols, VdcActionType parentCommand, boolean startPollingTasks) {
        // this is temp code until it will be implemented in SPM
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() == 6) {
            // get all vm disks in order to check post zero - if one of the
            // disks is marked with wipe_after_delete
            boolean postZero =
                    LinqUtils.filter(getDiskDao().getAllForVm(getVm().getId()),
                            new Predicate<Disk>() {
                                @Override
                                public boolean eval(Disk disk) {
                                    return disk.isWipeAfterDelete();
                                }
                            }).size() > 0;

            Guid taskId1 = persistAsyncTaskPlaceHolder(parentCommand, DELETE_PRIMARY_IMAGE_TASK_KEY);

            // delete first image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            VDSReturnValue vdsRetValue = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(guids.get(1),
                            guids.get(0), guids.get(2), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                return false;
            }

            Guid guid1 =
                    createTask(taskId1, vdsRetValue.getCreationInfo(), parentCommand, VdcObjectType.Storage, guids.get(0));
            getTaskIdList().add(guid1);

            Guid taskId2 = persistAsyncTaskPlaceHolder(parentCommand, DELETE_SECONDARY_IMAGES_TASK_KEY);
            // delete second image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            vdsRetValue = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(guids.get(1),
                            guids.get(0), guids.get(4), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                if (startPollingTasks) {
                    TaskManagerUtil.startPollingTask(guid1);
                }
                return false;
            }

            Guid guid2 = createTask(taskId2, vdsRetValue.getCreationInfo(), parentCommand);
            getTaskIdList().add(guid2);

            if (startPollingTasks) {
                TaskManagerUtil.startPollingTask(guid1);
                TaskManagerUtil.startPollingTask(guid2);
            }
        }

        return true;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected int getBlockSparseInitSizeInGb() {
        return Config.<Integer> getValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    protected List<ValidationError> validateCustomProperties(VmStatic vmStaticFromParams) {
        return VmPropertiesUtils.getInstance().validateVmProperties(
                getVdsGroup().getcompatibility_version(),
                vmStaticFromParams.getCustomProperties());
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

        // get VM name
        String vmName = vm.getName();

        // get the max VM name (configuration parameter)
        int maxVmNameLengthWindows = Config.<Integer> getValue(ConfigValues.MaxVmNameLengthWindows);
        int maxVmNameLengthNonWindows = Config.<Integer> getValue(ConfigValues.MaxVmNameLengthNonWindows);

        // names are allowed different lengths in Windows and non-Windows OSs,
        // consider this when setting the max length.
        int maxLength = osRepository.isWindows(vm.getVmOsId()) ? maxVmNameLengthWindows : maxVmNameLengthNonWindows;

        // check if name is longer than allowed name
        boolean nameLengthValid = (vmName.length() <= maxLength);

        // return result
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
        log.infoFormat("Locking VM(id = {0}) {1} compensation.", getVmId(), isInternalExecution() ? "without" : "with");

        if (isInternalExecution()) {
            VmHandler.checkStatusAndLockVm(getVmId());
        } else {
            VmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());
        }
    }

    protected boolean canPerformDiskHotPlug(Disk disk) {
        return isHotPlugSupported()
                && isDiskSupportedForPlugUnPlug(disk);
    }

    protected boolean canPerformNicHotPlug() {
        return isHotPlugSupported()
                && isNicSupportedForPlugUnPlug();
    }

    /**
     * check that hotplug is enabled via the 3.1 config paramter {@literal ConfigValues.HotPlugEnabled,
     * @return
     */
    protected boolean isHotPlugSupported() {
        if (FeatureSupported.hotPlug(getVm().getVdsGroupCompatibilityVersion())) {
            return true;
        }

        return failCanDoAction(VdcBllMessages.HOT_PLUG_IS_NOT_SUPPORTED);
    }

    /**
     * The following method should check if os of guest is supported for nic hot plug/unplug operation
     * @return
     */
    protected boolean isNicSupportedForPlugUnPlug() {
        if (osRepository.hasNicHotplugSupport(getVm().getOs(), getVm().getVdsGroupCompatibilityVersion())) {
            return true;
        }

        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
    }

    /**
     * The following method should check if os of guest is supported for disk hot plug/unplug operation
     * @param disk
     * @return
     */
    protected boolean isDiskSupportedForPlugUnPlug(Disk disk) {
        Set<String> diskHotpluggableInterfaces = osRepository.getDiskHotpluggableInterfaces(getVm().getOs(),
                getVm().getVdsGroupCompatibilityVersion());

        if (CollectionUtils.isEmpty(diskHotpluggableInterfaces)
                || !diskHotpluggableInterfaces.contains(disk.getDiskInterface().name())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        }

        return true;
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

    /** Overriding to allow spying from this package */
    @Override
    protected VmNicDao getVmNicDao() {
        return super.getVmNicDao();
    }

    protected VmDynamicDAO getVmDynamicDao() {
        return getDbFacade().getVmDynamicDao();
    }

    protected TagDAO getTagDao() {
        return getDbFacade().getTagDao();
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    protected boolean checkPayload(VmPayload payload, String isoPath) {
        boolean returnValue = true;
        if (payload.getType() != VmDeviceType.CDROM && payload.getType() != VmDeviceType.FLOPPY) {
            addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_INVALID_PAYLOAD_TYPE);
            returnValue = false;
        } else {
            for (String content : payload.getFiles().values()) {
                // Check each file individually, no constraint on total size
                if (!VmPayload.isPayloadSizeLegal(content)) {
                    Integer lengthInKb = 2 * Config.<Integer> getValue(ConfigValues.PayloadSize) / Kb;
                    addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_SIZE_EXCEEDED);
                    addCanDoActionMessageVariable("size", lengthInKb.toString());
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
            return failCanDoAction(nonManagedVmValidationResult.getMessage());
        }
        return true;
    }

    protected MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(List<DiskImage> disksList) {
        return new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksList));
    }
}
