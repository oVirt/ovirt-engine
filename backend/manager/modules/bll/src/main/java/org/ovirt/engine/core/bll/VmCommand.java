package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.TagDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;

@SuppressWarnings("serial")
public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

    private static final int Kb = 1024;
    protected final static int MAX_NETWORK_INTERFACES_SUPPORTED = 8;

    public VmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    @Override
    public NGuid getStoragePoolId() {
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

    @Override
    protected List<tags> getTagsAttachedToObject() {
        return getTagDao().getAllForVm((getParameters()).getVmId().toString());
    }

    // 26 PCI slots: 31 total minus 5 saved for qemu (Host Bridge, ISA Bridge,
    // IDE, Agent, ACPI)
    public final static int MAX_PCI_SLOTS = 26;
    // 3 IDE slots: 4 total minus 1 for CD
    public final static int MAX_IDE_SLOTS = 3;

    /**
     * This method checks that with the given parameters, the max PCI and IDE limits defined are not passed.
     *
     * @param monitorsNumber
     * @param interfaces
     * @param disks
     * @return
     */
    public static <T extends Disk> boolean checkPciAndIdeLimit(int monitorsNumber, List<VmNetworkInterface> interfaces,
            List<T> disks, ArrayList<String> messages) {
        boolean result = true;
        // this adds: monitors + 2 * (interfaces with type rtl_pv) + (all other
        // interfaces) + (all disks that are not IDE)
        int pciInUse = monitorsNumber;

        for (VmNetworkInterface a : interfaces) {
            if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.rtl8139_pv) {
                pciInUse += 2;
            } else {
                pciInUse += 1;
            }
        }

        pciInUse += LinqUtils.filter(disks, new Predicate<T>() {
            @Override
            public boolean eval(T a) {
                return a.getDiskInterface() != DiskInterface.IDE;
            }
        }).size();

        if (pciInUse > MAX_PCI_SLOTS) {
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
        return result;
    }

    protected boolean removeVmInSpm(Guid storagePoolId, Guid vmID, Guid storageDomainId) {
        return runVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, vmID, storageDomainId)).getSucceeded();
    }

    protected void removeVmStatic() {
        getVmStaticDAO().remove(getVmId());
    }

    protected void removeVmNetwork() {
        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
        if (interfaces != null) {
            for (VmNetworkInterface iface : interfaces) {
                MacPoolManager.getInstance().freeMac(iface.getMacAddress());
            }
        }
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

            getBackend().EndAction(
                    p.getCommandType() == VdcActionType.Unknown ? getChildActionType() : p.getCommandType(), p);
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
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing EndAction on Vm");
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

    protected boolean handleHibernatedVm(VdcActionType parentCommand, boolean startPollingTasks) {
        // this is temp code until it will be implemented in SPM
        String[] strings = getVm().getHibernationVolHandle().split(",");
        List<Guid> guids = new LinkedList<Guid>();
        for (String string : strings) {
            guids.add(new Guid(string));
        }
        Guid[] imagesList = guids.toArray(new Guid[0]);
        if (imagesList.length == 6) {
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

            // delete first image
            // the next 'DeleteImageGroup' command should also take care of the
            // image removal:
            VDSReturnValue vdsRetValue1 = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(imagesList[1], imagesList[0], imagesList[2],
                            postZero, false, getVm().getVdsGroupCompatibilityVersion().toString()));

            if (!vdsRetValue1.getSucceeded()) {
                return false;
            }

            Guid guid1 =
                    createTask(vdsRetValue1.getCreationInfo(), parentCommand, VdcObjectType.Storage, imagesList[0]);
            getTaskIdList().add(guid1);

            // delete second image
            // the next 'DeleteImageGroup' command should also take care of the
            // image removal:
            VDSReturnValue vdsRetValue2 = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(imagesList[1], imagesList[0], imagesList[4],
                            postZero, false, getVm().getVdsGroupCompatibilityVersion().toString()));

            if (!vdsRetValue2.getSucceeded()) {
                if (startPollingTasks) {
                    AsyncTaskManager.getInstance().StartPollingTask(guid1);
                }
                return false;
            }

            Guid guid2 = createTask(vdsRetValue2.getCreationInfo(), parentCommand);
            getTaskIdList().add(guid2);

            if (startPollingTasks) {
                AsyncTaskManager.getInstance().StartPollingTask(guid1);
                AsyncTaskManager.getInstance().StartPollingTask(guid2);
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
        return Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    protected List<ValidationError> validateCustomProperties(VmStatic vmStaticFromParams) {
        return VmPropertiesUtils.getInstance().validateVMProperties(
                getVdsGroup().getcompatibility_version(),
                vmStaticFromParams);
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
        int maxVmNameLengthWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthWindows);
        int maxVmNameLengthNonWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthNonWindows);

        // names are allowed different lengths in Windows and non-Windows OSs,
        // consider this when setting the max length.
        int maxLength = vm.getVmOs().isWindows() ? maxVmNameLengthWindows : maxVmNameLengthNonWindows;

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

    protected boolean canPerformHotPlug() {
        return isHotPlugSupported() && isOsSupportingHotPlug();
    }

    /**
     * check that hotplug is enabled via the 3.1 config paramter {@literal ConfigValues.HotPlugEnabled,
     * @return
     */
    protected boolean isHotPlugSupported() {
        if (Config.<Boolean> GetValue(ConfigValues.HotPlugEnabled, getVds().getVdsGroupCompatibilityVersion()
                .getValue())) {
            return true;
        }
        addCanDoActionMessage(VdcBllMessages.HOT_PLUG_IS_NOT_SUPPORTED);
        return false;
    }

    /**
     * The following method should check if os of guest is supported for hot plug/unplug operation
     * @return
     */
    protected boolean isOsSupportingHotPlug() {
        String vmOs = getVm().getOs().name();
        String[] unsupportedOSs = Config.<String> GetValue(ConfigValues.HotPlugUnsupportedOsList).split(",");
        for (String os : unsupportedOSs) {
            if (os.equalsIgnoreCase(vmOs)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
                return false;
            }
        }
        return true;
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

    /** Overriding to allow spying from this package */
    @Override
    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return super.getVmNetworkInterfaceDao();
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

    protected boolean checkPayload(VmPayload payload, String isoPath) {
        boolean returnValue = true;
        if (payload.getType() != VmDeviceType.CDROM && payload.getType() != VmDeviceType.FLOPPY) {
            addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_INVALID_PAYLOAD_TYPE);
            returnValue = false;
        } else if (!VmPayload.isPayloadSizeLegal(payload.getContent())) {
            Integer lengthInKb = 2 * Config.<Integer> GetValue(ConfigValues.PayloadSize) / Kb;
            addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_SIZE_EXCEEDED);
            addCanDoActionMessage(String.format("$size %1$s", lengthInKb.toString()));
            returnValue = false;
        }
        return returnValue;
    }
}
