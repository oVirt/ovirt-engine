package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationFailureReason;

public abstract class VmCommand<T extends VmOperationParameterBase> extends CommandBase<T> {

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
    }

    @Override
    protected String getDescription() {
        return getVmName();
    }

    @Override
    protected List<tags> GetTagsAttachedToObject() {

        return DbFacade
                .getInstance()
                .getTagDAO()
                .getAllForVm((getParameters()).getVmId().toString());
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
    public static boolean CheckPCIAndIDELimit(int monitorsNumber, List<VmNetworkInterface> interfaces,
            List<DiskImageBase> disks, ArrayList<String> messages) {
        boolean result = true;
        // this adds: monitors + 2 * (interfaces with type rtl_pv) + (all other
        // interfaces) + (all disks that are not IDE)
        // LINQ 29456
        // int pciInUse = monitorsNumber + interfaces.Select(a =>
        // (a.type.HasValue &&
        // (VmInterfaceType)a.type.Value == VmInterfaceType.rtl8139_pv) ? 2 :
        // 1).Sum() +
        // disks.Where(a => a.disk_interface != DiskInterface.IDE).Count();

        int pciInUse = monitorsNumber;

        for (VmNetworkInterface a : interfaces) {
            if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.rtl8139_pv)
                pciInUse += 2;
            else
                pciInUse += 1;
        }

        pciInUse += LinqUtils.filter(disks, new Predicate<DiskImageBase>() {
            @Override
            public boolean eval(DiskImageBase a) {
                return a.getdisk_interface() != DiskInterface.IDE;
            }
        }).size();

        // LINQ 29456

        if (pciInUse > MAX_PCI_SLOTS) {
            result = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS.name());
        }
        // LINQ 29456
        // else if (disks.Where(a => a.disk_interface ==
        // DiskInterface.IDE).Count() > MAX_IDE_SLOTS)
        else if (MAX_IDE_SLOTS < LinqUtils.filter(disks, new Predicate<DiskImageBase>() {
            @Override
            public boolean eval(DiskImageBase a) {
                return a.getdisk_interface() == DiskInterface.IDE;
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

    public static boolean UpdateVmInSpm(Guid storagePoolId, List<VM> vmsList, Guid storageDomainId) {
        java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndMetaDictionary =
                new java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>(
                        vmsList.size());
        OvfManager ovfManager = new OvfManager();
        for (VM vm : vmsList) {
            java.util.ArrayList<DiskImage> AllVmImages = new java.util.ArrayList<DiskImage>();
            VmHandler.updateDisksFromDb(vm);
            if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
                vm.setInterfaces(DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(vm.getvm_guid()));
            }
            for (DiskImage disk : vm.getDiskMap().values()) {
                AllVmImages.addAll(ImagesHandler.getAllImageSnapshots(disk.getId(), disk.getit_guid()));
            }
            if (StringHelper.isNullOrEmpty(vm.getvmt_name())) {
                VmTemplate t = DbFacade.getInstance().getVmTemplateDAO().get(vm.getvmt_guid());
                vm.setvmt_name(t.getname());
            }
            String vmMeta = "";

            // OVF Uncomment next line when OVF support is added
            RefObject<String> tempRefObject = new RefObject<String>(vmMeta);
            ovfManager.ExportVm(tempRefObject, vm, AllVmImages);
            vmMeta = tempRefObject.argvalue;

            // LINQ 29456
            // vmsAndMetaDictionary.Add(vm.vm_guid, new KeyValuePair<string,
            // List<Guid>>
            // (vmMeta, vm.DiskMap.Values.Select(a =>
            // a.image_group_id.Value).ToList()));
            vmsAndMetaDictionary.put(
                    vm.getvm_guid(),
                    new KeyValuePairCompat<String, List<Guid>>(vmMeta, LinqUtils.foreach(vm.getDiskMap().values(),
                            new Function<DiskImage, Guid>() {
                                @Override
                                public Guid eval(DiskImage a) {
                                    return a.getimage_group_id().getValue();
                                }
                            })));
        }
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, vmsAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    protected boolean RemoveVmInSpm(Guid storagePoolId, Guid vmID) {
        return RemoveVmInSpm(storagePoolId, vmID, Guid.Empty);
    }

    protected boolean RemoveVmInSpm(Guid storagePoolId, Guid vmID, Guid storageDomainId) {
        return runVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, vmID, storageDomainId)).getSucceeded();
    }

    protected void RemoveVmStatic() {
        DbFacade.getInstance().getVmStaticDAO().remove(getVmId());
    }

    protected void RemoveVmNetwork() {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getVmId());
        if (interfaces != null) {
            for (VmNetworkInterface iface : interfaces) {
                MacPoolManager.getInstance().freeMac(iface.getMacAddress());
                // \\DbFacade.Instance.RemoveVmInterfaceById(iface.id);
                // \\DbFacade.Instance.RemoveInterfaceStatistics(iface.id);
            }
        }
    }

    protected void RemoveVmDynamic() {
        DbFacade.getInstance().getVmDynamicDAO().remove(getVmId());
    }

    protected void RemoveVmStatistics() {
        DbFacade.getInstance().getVmStatisticsDAO().remove(getVmId());
    }

    protected void RemoveVmUsers() {
        List<tags_vm_map> all = DbFacade.getInstance().getTagDAO().getTagVmMapByVmIdAndDefaultTag(getVmId());
        for (tags_vm_map tagVm : all) {
            DbFacade.getInstance().getTagDAO().detachVmFromTag(tagVm.gettag_id(), getVmId());
        }
    }

    protected void EndVmCommand() {
        EndActionOnDisks();

        if (getVm() != null) {
            VmHandler.unlockVm(getVm().getDynamicData(), getCompensationContext());

            UpdateVmInSpm(getVm().getstorage_pool_id(),
                    new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing EndAction on Vm");
        }

        setSucceeded(true);
    }

    protected void EndActionOnDisks() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(getChildActionType(), p);
        }
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
        // LINQ 29456
        // Guid[] imagesList = Vm.hibernation_vol_handle.Split(',').Select(a =>
        // new Guid(a)).ToArray();
        String[] strings = getVm().gethibernation_vol_handle().split(",");
        List<Guid> guids = new LinkedList<Guid>();
        for (String string : strings) {
            guids.add(new Guid(string));
        }
        Guid[] imagesList = guids.toArray(new Guid[0]);
        if (imagesList.length == 6) {
            // get all vm disks in order to check post zero - if one of the
            // disks is marked with wipe_after_delete
            // boolean postZero = false; //LINQ
            // DbFacade.Instance.GetImagesByVmGuid(Vm.vm_guid).Exists(a =>
            // a.wipe_after_delete);
            boolean postZero =
                    LinqUtils.filter(DbFacade.getInstance().getDiskImageDAO().getAllForVm(getVm().getvm_guid()),
                            new Predicate<DiskImage>() {
                                @Override
                                public boolean eval(DiskImage diskImage) {
                                    return diskImage.getwipe_after_delete();
                                }
                            }).size() > 0;

            // delete first image
            // the next 'DeleteImageGroup' command should also take care of the
            // image removal:
            VDSReturnValue vdsRetValue1 = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
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
            VDSReturnValue vdsRetValue2 = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
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
                getParameters()));
        p.setEntityId(getParameters().getEntityId());
        Guid taskID = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteImage, p, false);

        return taskID;
    }

    private static LogCompat log = LogFactoryCompat.getLog(VmCommand.class);

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getParameters().getVmId(), VdcObjectType.VM);
    }

    protected static void handleCustomPropertiesError(List<ValidationError> validationErrors, ArrayList<String> message) {
        String invalidSyntaxMsg = VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_VM_PROPERTIES_INVALID_SYNTAX.name();

        List<String> errorMessages = VmPropertiesUtils.generateErrorMessages(validationErrors, invalidSyntaxMsg,
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

}
