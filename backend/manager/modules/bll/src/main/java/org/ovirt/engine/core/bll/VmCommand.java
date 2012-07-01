package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.TagDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationFailureReason;

@SuppressWarnings("serial")
public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

    private static int Kb = 1024;
    private Map<Pair<Guid, Guid>, Double> quotaForStorageConsumption;
    protected final static int MAX_NETWORK_INTERFACES_SUPPORTED = 8;
    private static final Map<VmPropertiesUtils.ValidationFailureReason, String> failureReasonsToVdcBllMessagesMap =
            new HashMap<VmPropertiesUtils.ValidationFailureReason, String>();
    private static final Map<VmPropertiesUtils.ValidationFailureReason, String> failureReasonsToFormatMessages =
            new HashMap<VmPropertiesUtils.ValidationFailureReason, String>();
    static {
        failureReasonsToVdcBllMessagesMap.put(ValidationFailureReason.DUPLICATE_KEY,
                VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_VM_PROPERTIES_DUPLICATE_KEYS.name());
        failureReasonsToVdcBllMessagesMap.put(ValidationFailureReason.KEY_DOES_NOT_EXIST,
                VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_VM_PROPERTIES_INVALID_KEYS.name());
        failureReasonsToVdcBllMessagesMap.put(ValidationFailureReason.INCORRECT_VALUE,
                VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_VM_PROPERTIES_INVALID_VALUES.name());
        failureReasonsToFormatMessages.put(ValidationFailureReason.DUPLICATE_KEY, "$DuplicateKeys %1$s");
        failureReasonsToFormatMessages.put(ValidationFailureReason.KEY_DOES_NOT_EXIST, "$MissingKeys %1$s");
        failureReasonsToFormatMessages.put(ValidationFailureReason.INCORRECT_VALUE, "$WrongValueKeys %1$s");

    }

    public VmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
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
        ExecuteVmCommand();
    }

    protected void ExecuteVmCommand() {
        // The default action is no action.
        // Other command may override this behavior.
    }

    @Override
    protected String getDescription() {
        return getVmName();
    }

    @Override
    protected List<tags> GetTagsAttachedToObject() {
        return getTagDAO().getAllForVm((getParameters()).getVmId().toString());
    }

    // 26 PCI slots: 31 total minus 5 saved for qemu (Host Bridge, ISA Bridge,
    // IDE, Agent, ACPI)
    private final static int MAX_PCI_SLOTS = 26;
    // 3 IDE slots: 4 total minus 1 for CD
    private final static int MAX_IDE_SLOTS = 3;

    /**
     * This method checks that with the given parameters, the max PCI and IDE limits defined are not passed.
     *
     * @param monitorsNumber
     * @param interfaces
     * @param disks
     * @return
     */
    public static <T extends Disk> boolean CheckPCIAndIDELimit(int monitorsNumber, List<VmNetworkInterface> interfaces,
            List<T> disks, ArrayList<String> messages) {
        boolean result = true;
        // this adds: monitors + 2 * (interfaces with type rtl_pv) + (all other
        // interfaces) + (all disks that are not IDE)
        int pciInUse = monitorsNumber;

        for (VmNetworkInterface a : interfaces) {
            if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.rtl8139_pv)
                pciInUse += 2;
            else
                pciInUse += 1;
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

    /**
     * This method create OVF for each vm in list and call updateVm in SPM
     *
     * @param storagePoolId
     * @param vmsList
     * @return Returns true if updateVm succeeded.
     */
    public static boolean UpdateVmInSpm(Guid storagePoolId, List<VM> vmsList) {
        return UpdateVmInSpm(storagePoolId, vmsList, Guid.Empty);
    }

    public static boolean UpdateVmInSpm(Guid storagePoolId,
            List<VM> vmsList,
            Guid storageDomainId) {
        HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndMetaDictionary =
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>(vmsList.size());
        OvfManager ovfManager = new OvfManager();
        for (VM vm : vmsList) {
            ArrayList<DiskImage> AllVmImages = new ArrayList<DiskImage>();
            VmHandler.updateDisksFromDb(vm);
            if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
                vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getId()));
            }
            for (Disk disk : vm.getDiskMap().values()) {
                if (disk.isAllowSnapshot()) {
                    DiskImage diskImage = (DiskImage) disk;
                    AllVmImages.addAll(ImagesHandler.getAllImageSnapshots(diskImage.getImageId(),
                            diskImage.getit_guid()));
                }
            }
            if (StringUtils.isEmpty(vm.getvmt_name())) {
                VmTemplate t = DbFacade.getInstance().getVmTemplateDAO().get(vm.getvmt_guid());
                vm.setvmt_name(t.getname());
            }
            String vmMeta = ovfManager.ExportVm(vm, AllVmImages);

            vmsAndMetaDictionary.put(
                    vm.getId(),
                    new KeyValuePairCompat<String, List<Guid>>(vmMeta, LinqUtils.foreach(vm.getDiskMap().values(),
                            new Function<Disk, Guid>() {
                                @Override
                                public Guid eval(Disk a) {
                                    return a.getId();
                                }
                            })));
        }
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, vmsAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    protected Map<Pair<Guid, Guid>, Double> getQuotaConsumeMap(Collection<DiskImage> diskInfoList) {
        if (quotaForStorageConsumption == null) {
            quotaForStorageConsumption =
                    QuotaHelper.getInstance().getQuotaConsumeMap(diskInfoList);
        }
        return quotaForStorageConsumption;
    }

    protected boolean RemoveVmInSpm(Guid storagePoolId, Guid vmID) {
        return RemoveVmInSpm(storagePoolId, vmID, Guid.Empty);
    }

    protected boolean RemoveVmInSpm(Guid storagePoolId, Guid vmID, Guid storageDomainId) {
        return runVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, vmID, storageDomainId)).getSucceeded();
    }

    protected void RemoveVmStatic() {
        getVmStaticDAO().remove(getVmId());
    }

    protected void RemoveVmNetwork() {
        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDAO().getAllForVm(getVmId());
        if (interfaces != null) {
            for (VmNetworkInterface iface : interfaces) {
                MacPoolManager.getInstance().freeMac(iface.getMacAddress());
            }
        }
    }

    protected void RemoveVmDynamic() {
        getVmDynamicDAO().remove(getVmId());
    }

    protected void RemoveVmStatistics() {
        getVmStatisticsDAO().remove(getVmId());
    }

    protected void RemoveVmUsers() {
        List<tags_vm_map> all = getTagDAO().getTagVmMapByVmIdAndDefaultTag(getVmId());
        for (tags_vm_map tagVm : all) {
            getTagDAO().detachVmFromTag(tagVm.gettag_id(), getVmId());
        }
    }

    protected void EndVmCommand() {
        EndActionOnDisks();

        if (getVm() != null) {
            if (getVm().getstatus() == VMStatus.ImageLocked) {
                VmHandler.unlockVm(getVm().getDynamicData(), getCompensationContext());
            }

            UpdateVmInSpm(getVm().getstorage_pool_id(), Arrays.asList(getVm()));
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing EndAction on Vm");
        }

        setSucceeded(true);
    }

    protected void EndActionOnDisks() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            if (overrideChildCommandSuccess()) {
                p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            }

            getBackend().EndAction(
                    p.getCommandType() == VdcActionType.Unknown ? getChildActionType() : p.getCommandType(), p);
        }
    }

    /**
     * @return By default, <code>true</code> to override the child's success flag with the command's success flag.
     */
    protected boolean overrideChildCommandSuccess() {
        return true;
    }

    @Override
    protected void EndSuccessfully() {
        EndVmCommand();
    }

    @Override
    protected void EndWithFailure() {
        EndVmCommand();
    }

    protected VdcActionType getChildActionType() {
        return VdcActionType.Unknown;
    }

    protected boolean HandleHibernatedVm(VdcActionType parentCommand, boolean startPollingTasks) {
        // this is temp code until it will be implmented in SPM
        String[] strings = getVm().gethibernation_vol_handle().split(",");
        List<Guid> guids = new LinkedList<Guid>();
        for (String string : strings) {
            guids.add(new Guid(string));
        }
        Guid[] imagesList = guids.toArray(new Guid[0]);
        if (imagesList.length == 6) {
            // get all vm disks in order to check post zero - if one of the
            // disks is marked with wipe_after_delete
            boolean postZero =
                    LinqUtils.filter(getDiskDAO().getAllForVm(getVm().getId()),
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
                            postZero, false, getVm().getvds_group_compatibility_version().toString()));

            if (!vdsRetValue1.getSucceeded()) {
                return false;
            }

            Guid guid1 = CreateTask(vdsRetValue1.getCreationInfo(), parentCommand);
            getTaskIdList().add(guid1);

            // delete second image
            // the next 'DeleteImageGroup' command should also take care of the
            // image removal:
            VDSReturnValue vdsRetValue2 = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(imagesList[1], imagesList[0], imagesList[4],
                            postZero, false, getVm().getvds_group_compatibility_version().toString()));

            if (!vdsRetValue2.getSucceeded()) {
                if (startPollingTasks) {
                    UpdateTasksWithActionParameters();
                    AsyncTaskManager.getInstance().StartPollingTask(guid1);
                }
                return false;
            }

            Guid guid2 = CreateTask(vdsRetValue2.getCreationInfo(), parentCommand);
            getTaskIdList().add(guid2);

            if (startPollingTasks) {
                UpdateTasksWithActionParameters();
                AsyncTaskManager.getInstance().StartPollingTask(guid1);
                AsyncTaskManager.getInstance().StartPollingTask(guid2);
            }
        }

        return true;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParameters(), asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        Guid taskID = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteImage, p, false);

        return taskID;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected int getBlockSparseInitSizeInGB() {
        return Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    protected List<ValidationError> validateCustomProperties(VmStatic vmStaticFromParams) {
        return VmPropertiesUtils.getInstance().validateVMProperties(
                getVdsGroup().getcompatibility_version(),
                vmStaticFromParams);
    }

    protected static void handleCustomPropertiesError(List<ValidationError> validationErrors, ArrayList<String> message) {
        String invalidSyntaxMsg = VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_VM_PROPERTIES_INVALID_SYNTAX.name();

        List<String> errorMessages =
                VmPropertiesUtils.getInstance().generateErrorMessages(validationErrors, invalidSyntaxMsg,
                        failureReasonsToVdcBllMessagesMap, failureReasonsToFormatMessages);
        message.addAll(errorMessages);
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
        String vmName = vm.getvm_name();

        // get the max VM name (configuration parameter)
        int maxVmNameLengthWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthWindows);
        int maxVmNameLengthNonWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthNonWindows);

        // names are allowed different lengths in Windows and non-Windows OSs,
        // consider this when setting the max length.
        int maxLength = vm.getvm_os().isWindows() ? maxVmNameLengthWindows : maxVmNameLengthNonWindows;

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

    /**
     * check that we number of Network-Interfaces does not exceed maximum (kvm limitation). This limitation is different
     * for RHEL-5.5 and RHEL-6.0. This is expresses in configuraiton parameters.
     *
     * @param interfaces
     * @return false if validation failed; i.e thera are more nics than allowed. true if validation succeeded.
     */
    public static boolean validateNumberOfNics(List<VmNetworkInterface> interfaces, VmNetworkInterface networkInterface) {
        int ifCount = 0;
        if (networkInterface != null && networkInterface.getType() != null) {
            ifCount += (VmInterfaceType.forValue(networkInterface.getType()) == VmInterfaceType.rtl8139_pv ? 2 : 1);
        }
        for (VmNetworkInterface i : interfaces) {
            if (i.getType() != null) {
                ifCount += (i.getType() == VmInterfaceType.rtl8139_pv.getValue()) ? 2 : 1;
            }
        }
        return (ifCount <= MAX_NETWORK_INTERFACES_SUPPORTED);
    }

    protected boolean canPerformHotPlug() {
        return isHotPlugSupported() && isOSSupportingHotPlug();
    }

    /**
     * check that hotplug is enabled via the 3.1 config paramter {@literal ConfigValues.HotPlugEnabled,
     * @return
     */
    protected boolean isHotPlugSupported() {
        if (Config.<Boolean> GetValue(ConfigValues.HotPlugEnabled, getVds().getvds_group_compatibility_version()
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
    protected boolean isOSSupportingHotPlug() {
        String vmOs = getVm().getos().name();
        String[] oses = Config.<String> GetValue(ConfigValues.HotPlugSupportedOsList).split(",");
        for (String os : oses) {
            if (os.equalsIgnoreCase(vmOs)) {
                return true;
            }
        }
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        return false;
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return getDbFacade().getVmDeviceDAO();
    }

    /** Overriding to allow spying from this package */
    @Override
    protected VmNetworkInterfaceDAO getVmNetworkInterfaceDAO() {
        return super.getVmNetworkInterfaceDAO();
    }

    protected VmDynamicDAO getVmDynamicDAO() {
        return getDbFacade().getVmDynamicDAO();
    }

    protected TagDAO getTagDAO() {
        return getDbFacade().getTagDAO();
    }

    protected DiskDao getDiskDAO() {
        return getDbFacade().getDiskDao();
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
        } else if (!StringUtils.isEmpty(isoPath) && payload.getType() == VmDeviceType.CDROM) {
            addCanDoActionMessage(VdcBllMessages.VMPAYLOAD_CDROM_EXCEEDED);
            returnValue = false;
        }
        return returnValue;
    }
}
