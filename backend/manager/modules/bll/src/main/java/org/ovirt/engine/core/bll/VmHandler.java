package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.EditableField;
import org.ovirt.engine.core.common.businessentities.EditableOnVm;
import org.ovirt.engine.core.common.businessentities.EditableOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VmInitDAO;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmHandler {

    private static ObjectIdentityChecker mUpdateVmsStatic;
    private static OsRepository osRepository;

    private static final Logger log = LoggerFactory.getLogger(VmHandler.class);

    private static Set<VdcActionType> COMMANDS_ALLOWED_ON_EXTERNAL_VMS = new HashSet<>();
    private static Set<VdcActionType> COMMANDS_ALLOWED_ON_HOSTED_ENGINE = new HashSet<>();
    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#InitHandlers
     */
    public static void init() {
        Class<?>[] inspectedClassNames = new Class<?>[] {
                VmBase.class,
                VM.class,
                VmStatic.class,
                VmDynamic.class,
                VmManagementParametersBase.class };

        osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

        mUpdateVmsStatic =
                new ObjectIdentityChecker(VmHandler.class, Arrays.asList(inspectedClassNames));

        for (Pair<EditableField, Field> pair : BaseHandler.extractAnnotatedFields(EditableField.class,
                (inspectedClassNames))) {
            mUpdateVmsStatic.AddPermittedFields(pair.getSecond().getName());
        }

        for (Pair<EditableOnVm, Field> pair : BaseHandler.extractAnnotatedFields(EditableOnVm.class, inspectedClassNames)) {
            mUpdateVmsStatic.AddPermittedFields(pair.getSecond().getName());
        }

        for (Pair<EditableOnVmStatusField, Field> pair : BaseHandler.extractAnnotatedFields(EditableOnVmStatusField.class,
                inspectedClassNames)) {
            mUpdateVmsStatic.AddField(Arrays.asList(pair.getFirst().statuses()), pair.getSecond().getName());
            if (pair.getFirst().isHotsetAllowed()) {
                mUpdateVmsStatic.AddHotsetFields(pair.getSecond().getName());
            }
        }

        for (Pair<EditableDeviceOnVmStatusField, Field> pair : BaseHandler.extractAnnotatedFields(EditableDeviceOnVmStatusField.class,
                inspectedClassNames)) {
            mUpdateVmsStatic.AddField(Arrays.asList(pair.getFirst().statuses()), pair.getSecond().getName());
        }

        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.InternalMigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.VmLogon);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.StopVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.ShutdownVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.RemoveVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.RebootVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.InternalMigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.VmLogon);
    }

    public static boolean isUpdateValid(VmStatic source, VmStatic destination, VMStatus status) {
        return mUpdateVmsStatic.IsUpdateValid(source, destination, status);
    }

    public static List<String> getChangedFieldsForStatus(VmStatic source, VmStatic destination, VMStatus status) {
        return mUpdateVmsStatic.getChangedFieldsForStatus(source, destination, status);
    }

    public static boolean isUpdateValid(VmStatic source, VmStatic destination, VMStatus status, boolean hotsetEnabled) {
        return mUpdateVmsStatic.IsUpdateValid(source, destination, status, hotsetEnabled);
    }

    public static boolean isUpdateValid(VmStatic source, VmStatic destination) {
        return mUpdateVmsStatic.IsUpdateValid(source, destination);
    }

    public static boolean isUpdateValidForVmDevice(String fieldName, VMStatus status) {
        return mUpdateVmsStatic.IsFieldUpdatable(status, fieldName, null);
    }

    public static boolean copyNonEditableFieldsToDestination(VmStatic source, VmStatic destination, boolean hotSetEnabled) {
        return mUpdateVmsStatic.copyNonEditableFieldsToDestination(source, destination, hotSetEnabled);
    }

    /**
     * Verifies the add vm command .
     *
     * @param reasons
     *            The reasons.
     * @param nicsCount
     *            How many vNICs need to be allocated.
     * @return
     */
    public static boolean verifyAddVm(List<String> reasons,
            int nicsCount,
            int vmPriority,
            MacPoolManagerStrategy macPool) {
        boolean returnValue = true;
        if (macPool.getAvailableMacsCount() < nicsCount) {
            if (reasons != null) {
                reasons.add(VdcBllMessages.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES.toString());
            }
            returnValue = false;
        } else if (!VmTemplateCommand.isVmPriorityValueLegal(vmPriority, reasons)) {
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean isVmWithSameNameExistStatic(String vmName) {
        List<VmStatic> vmStatic = DbFacade.getInstance().getVmStaticDao().getAllByName(vmName);
        return (vmStatic.size() != 0);
    }

    /**
     * Lock the VM in a new transaction, saving compensation data of the old status.
     *
     * @param vm
     *            The VM to lock.
     * @param compensationContext
     *            Used to save the old VM status, for compensation purposes.
     */
    public static void lockVm(final VmDynamic vm, final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                compensationContext.snapshotEntityStatus(vm);
                lockVm(vm.getId());
                compensationContext.stateChanged();
                return null;
            }
        });
    }

    /**
     * Check VM status before locking it, If VM status is not down, we throw an exception.
     *
     * @param status
     *            - The status of the VM
     */
    private static void checkStatusBeforeLock(VMStatus status) {
        if (status == VMStatus.ImageLocked) {
            log.error("VM status cannot change to image locked, since it is already locked");
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }
    }

    /**
     * Lock VM after check its status, If VM status is locked, we throw an exception.
     *
     * @param vmId
     *            - The ID of the VM.
     */
    public static void checkStatusAndLockVm(Guid vmId) {
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        checkStatusBeforeLock(vmDynamic.getStatus());
        lockVm(vmId);
    }

    /**
     * Lock VM with compensation, after checking its status, If VM status is locked, we throw an exception.
     *
     * @param vmId
     *            - The ID of the VM, which we want to lock.
     * @param compensationContext
     *            - Used to save the old VM status for compensation purposes.
     */
    public static void checkStatusAndLockVm(Guid vmId, CompensationContext compensationContext) {
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        checkStatusBeforeLock(vmDynamic.getStatus());
        lockVm(vmDynamic, compensationContext);
    }

    public static void lockVm(Guid vmId) {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVmStatus,
                        new SetVmStatusVDSCommandParameters(vmId, VMStatus.ImageLocked));
    }

    /**
     * Unlock the VM in a new transaction, saving compensation data of the old status.
     *
     * @param vm
     *            The VM to unlock.
     * @param compensationContext
     *            Used to save the old VM status, for compensation purposes.
     */
    public static void unlockVm(final VM vm, final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                compensationContext.snapshotEntityStatus(vm.getDynamicData());
                unLockVm(vm);
                compensationContext.stateChanged();
                return null;
            }
        });
    }

    public static void unLockVm(VM vm) {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVmStatus,
                        new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Down));
        vm.setStatus(VMStatus.Down);
    }

    public static void updateDisksFromDb(VM vm) {
        List<Disk> imageList = DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId());
        vm.clearDisks();
        updateDisksForVm(vm, imageList);
    }

    public static void updateDisksForVm(VM vm, Collection<? extends Disk> disks) {
        for (Disk disk : disks) {
            if (disk.isAllowSnapshot() && !disk.isDiskSnapshot()) {
                DiskImage image = (DiskImage) disk;
                vm.getDiskMap().put(image.getId(), image);
                vm.getDiskList().add(image);
            } else {
                vm.getDiskMap().put(disk.getId(), disk);
            }
        }
    }

    /**
     * Fetch VmInit from Database
     * @param vm VmBase to set the VmInit into
     * @param secure if true don't return any password field
     * We want to set false only when running VM becase the VmInitDAO
     * decrypt the password.
     */
    public static void updateVmInitFromDB(VmBase vm, boolean secure) {
        VmInitDAO db = DbFacade.getInstance().getVmInitDao();
        vm.setVmInit(db.get(vm.getId()));
        if (vm.getVmInit() != null) {
            if (secure) {
                vm.getVmInit().setPasswordAlreadyStored(!StringUtils.isEmpty(vm.getVmInit().getRootPassword()));
                vm.getVmInit().setRootPassword(null);
            } else {
                vm.getVmInit().setPasswordAlreadyStored(false);
            }
        }
    }

    public static void addVmInitToDB(VmBase vm) {
        if (vm.getVmInit() != null) {
            vm.getVmInit().setId(vm.getId());
            VmInitDAO db = DbFacade.getInstance().getVmInitDao();
            VmInit oldVmInit = db.get(vm.getId());
            if (oldVmInit == null) {
                db.save(vm.getVmInit());
            } else {
                if (vm.getVmInit().isPasswordAlreadyStored()) {
                    // since we are not always returning the password in
                    // updateVmInitFromDB()
                    // method (we don't want to display it in the UI/API) we
                    // don't want to override
                    // the password if the flag is on
                    vm.getVmInit().setRootPassword(oldVmInit.getRootPassword());
                }
                db.update(vm.getVmInit());
            }
        }
    }

    public static void updateVmInitToDB(VmBase vm) {
        if (vm.getVmInit() != null) {
            VmHandler.addVmInitToDB(vm);
        } else {
            VmHandler.removeVmInitFromDB(vm);
        }
    }

    public static void removeVmInitFromDB(VmBase vm) {
        VmInitDAO db = DbFacade.getInstance().getVmInitDao();
        db.remove(vm.getId());
    }

    // if secure is true we don't return the stored password, only
    // indicate that the password is set via the PasswordAlreadyStored property
    public static List<VmInit> getVmInitByIds(List<Guid> ids, boolean secure) {
        VmInitDAO db = DbFacade.getInstance().getVmInitDao();
        List<VmInit> all = db.getVmInitByIds(ids);

        for (VmInit vmInit: all) {
            if (secure) {
                vmInit.setPasswordAlreadyStored(!StringUtils.isEmpty(vmInit.getRootPassword()));
                vmInit.setRootPassword(null);
            } else {
                vmInit.setPasswordAlreadyStored(false);
            }

        }
        return all;
    }

    /**
     * Filters the vm image disks/disk devices according to the given parameters
     * note: all the given parameters are relevant for image disks, luns will be filtered.
     */
    public static void filterImageDisksForVM(VM vm, boolean allowOnlyNotShareableDisks,
                                             boolean allowOnlySnapableDisks, boolean allowOnlyActiveDisks) {
        List<DiskImage> filteredDisks = ImagesHandler.filterImageDisks(vm.getDiskMap().values(),
                allowOnlyNotShareableDisks, allowOnlySnapableDisks, allowOnlyActiveDisks);
        Collection<? extends Disk> vmDisksToRemove = CollectionUtils.subtract(vm.getDiskMap().values(), filteredDisks);
        vm.clearDisks();
        updateDisksForVm(vm, filteredDisks);
        for (Disk diskToRemove : vmDisksToRemove) {
            vm.getManagedVmDeviceMap().remove(diskToRemove.getId());
        }
    }

    public static void updateNetworkInterfacesFromDb(VM vm) {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        vm.setInterfaces(interfaces);
    }

    private static Version getApplicationVersion(final String part, final String appName) {
        try {
            return new RpmVersion(part, getAppName(part, appName), true);
        } catch (Exception e) {
            log.debug("Failed to create rpm version object, part '{}' appName '{}': {}",
                    part,
                    appName,
                    e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private static String getAppName(final String part, final String appName) {
        if (StringUtils.contains(part, appName + "64")) { // 64 bit Agent has extension
            // to its name.
            return appName + "64";
        }
        return appName;
    }

    /**
     * Updates the {@link VM}'s {@link VM#getGuestAgentVersion()} and {@link VM#getSpiceDriverVersion()} based on the
     * VM's {@link VM#getAppList()} property.
     *
     * @param vm
     *            the VM
     */
    public static void updateVmGuestAgentVersion(final VM vm) {
        if (vm.getAppList() != null) {
            final String[] parts = vm.getAppList().split("[,]", -1);
            if (parts != null && parts.length != 0) {
                final List<String> possibleAgentAppNames = Config.<List<String>> getValue(ConfigValues.AgentAppName);
                final Map<String, String> spiceDriversInGuest =
                        Config.<Map<String, String>> getValue(ConfigValues.SpiceDriverNameInGuest);
                final String spiceDriverInGuest =
                        spiceDriversInGuest.get(osRepository.getOsFamily(vm.getOs()).toLowerCase());

                for (final String part : parts) {
                    for (String agentName : possibleAgentAppNames) {
                        if (StringUtils.containsIgnoreCase(part, agentName)) {
                            vm.setGuestAgentVersion(getApplicationVersion(part, agentName));
                        }
                        if (StringUtils.containsIgnoreCase(part, spiceDriverInGuest)) {
                            vm.setSpiceDriverVersion(getApplicationVersion(part, spiceDriverInGuest));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the validity of the given memory size according to OS type.
     *
     * @param vm
     *            a vm|template.
     * @param clusterVersion
     *            the vm's cluster version.
     * @return
     */
    public static void warnMemorySizeLegal(VmBase vm, Version clusterVersion) {
        if (! VmValidationUtils.isMemorySizeLegal(vm.getOsId(), vm.getMemSizeMb(), clusterVersion)) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.setVmId(vm.getId());
            logable.addCustomValue("VmName", vm.getName());
            logable.addCustomValue("VmMemInMb", String.valueOf(vm.getMemSizeMb()));
            logable.addCustomValue("VmMinMemInMb",
                    String.valueOf(VmValidationUtils.getMinMemorySizeInMb(vm.getOsId(), clusterVersion)));
            logable.addCustomValue("VmMaxMemInMb",
                    String.valueOf(VmValidationUtils.getMaxMemorySizeInMb(vm.getOsId(), clusterVersion)));

            AuditLogDirector.log(logable, AuditLogType.VM_MEMORY_NOT_IN_RECOMMENDED_RANGE);
        }
    }

    /**
     * Check if the OS type is supported.
     *
     * @param osId
     *            Type of the OS.
     * @param architectureType
     *            The architecture type.
     * @param reasons
     *            The reasons.VdsGroups
     * @return
     */
    public static boolean isOsTypeSupported(int osId,
                                            ArchitectureType architectureType,
                                            List<String> reasons) {
        boolean result = VmValidationUtils.isOsTypeSupported(osId, architectureType);
        if (!result) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_IS_NOT_SUPPORTED_BY_ARCHITECTURE_TYPE
                    .toString());
        }
        return result;
    }

    /**
     * Check if the display type is supported.
     *
     * @param osId
     *            Type of the OS.
     * @param defaultDisplayType
     *            The VM default display type.
     * @param reasons
     *            The reasons.VdsGroups
     * @param clusterVersion
     *            The cluster version.
     * @return
     */
    public static boolean isDisplayTypeSupported(int osId,
                                            DisplayType defaultDisplayType,
                                            List<String> reasons,
                                            Version clusterVersion) {
        boolean result = VmValidationUtils.isDisplayTypeSupported(osId, clusterVersion, defaultDisplayType);
        if (!result) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_VM_DISPLAY_TYPE_IS_NOT_SUPPORTED_BY_OS.name());
        }
        return result;
    }

    /**
     * Check if the OS type is supported for VirtIO-SCSI.
     *
     * @param osId
     *            Type of the OS
     * @param clusterVersion
     *            Cluster's version
     * @param reasons
     *            Reasons List
     * @return
     */
    public static boolean isOsTypeSupportedForVirtioScsi(int osId,
                                            Version clusterVersion,
                                            List<String> reasons) {
        boolean result = VmValidationUtils.isDiskInterfaceSupportedByOs(osId, clusterVersion, DiskInterface.VirtIO_SCSI);
        if (!result) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI.name());
        }
        return result;
    }

    /**
     * Check if the interface name is not duplicate in the list of interfaces.
     *
     * @param interfaces
     *            - List of interfaces the VM/Template got.
     * @param interfaceName
     *            - Candidate for interface name.
     * @param messages
     *            - Messages for CanDoAction().
     * @return - True , if name is valid, false, if name already exist.
     */
    public static boolean isNotDuplicateInterfaceName(List<VmNic> interfaces,
                                                      final String interfaceName,
                                                      List<String> messages) {

        // Interface iface = interfaces.FirstOrDefault(i => i.name ==
        // AddVmInterfaceParameters.Interface.name);
        VmNic iface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNic>() {
            @Override
            public boolean eval(VmNic i) {
                return i.getName().equals(interfaceName);
            }
        });

        if (iface != null) {
            messages.add(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREADY_IN_USE.name());
            return false;
        }
        return true;
    }

    /**
     * Checks number of monitors validation according to VM and Display types.
     *
     * @param displayType
     *            Display type : Spice or Vnc
     * @param numOfMonitors
     *            Number of monitors
     * @param reasons
     *            Messages for CanDoAction().
     * @return
     */
    public static boolean isNumOfMonitorsLegal(DisplayType displayType, int numOfMonitors, List<String> reasons) {
        boolean legal = true;
        if (displayType == DisplayType.vnc) {
            legal = (numOfMonitors <= 1);
        }
        else { // Spice
            legal = (numOfMonitors <= getMaxNumberOfMonitors());
        }
        if (!legal) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS.toString());
        }
        return legal;
    }

    public static boolean isSingleQxlDeviceLegal(DisplayType displayType, int osId, List<String> reasons,
            Version compatibilityVersion) {
        if (!FeatureSupported.singleQxlPci(compatibilityVersion)) {
             reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_INCOMPATIBLE_VERSION.toString());
             return false;
         }
        if (displayType != DisplayType.qxl) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_DISPLAY_TYPE.toString());
            return false;
        }
        if (!osRepository.isSingleQxlDeviceEnabled(osId)) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_SINGLE_DEVICE_OS_TYPE.toString());
            return false;
        }
        return true;
    }

    /**
     * get max of allowed monitors from config config value is a comma separated list of integers
     *
     * @return
     */
    private static int getMaxNumberOfMonitors() {
        int max = 0;
        String numOfMonitorsStr =
                Config.getValue(ConfigValues.ValidNumOfMonitors).toString().replaceAll("[\\[\\]]", "");
        String values[] = numOfMonitorsStr.split(",");
        for (String val : values) {
            val = val.trim();
            if (Integer.valueOf(val) > max) {
                max = Integer.valueOf(val);
            }
        }
        return max;
    }

    /**
     * Checks that the USB policy is legal for the VM. If it is ENABLED_NATIVE then it is legal only in case the cluster
     * level is >= 3.1. If it is ENABLED_LEGACY then it is not legal on Linux VMs.
     *
     * @param usbPolicy
     * @param osId
     * @param vdsGroup
     * @param messages
     *            - Messages for CanDoAction()
     * @return
     */
    public static boolean isUsbPolicyLegal(UsbPolicy usbPolicy,
            int osId,
            VDSGroup vdsGroup,
            List<String> messages) {
        boolean retVal = true;
        if (UsbPolicy.ENABLED_NATIVE.equals(usbPolicy)) {
            if (!Config.<Boolean> getValue(ConfigValues.NativeUSBEnabled, vdsGroup.getcompatibility_version()
                    .getValue())) {
                messages.add(VdcBllMessages.USB_NATIVE_SUPPORT_ONLY_AVAILABLE_ON_CLUSTER_LEVEL.toString());
                retVal = false;
            }
        } else if (UsbPolicy.ENABLED_LEGACY.equals(usbPolicy)) {
            if (osRepository.isLinux(osId)) {
                messages.add(VdcBllMessages.USB_LEGACY_NOT_SUPPORTED_ON_LINUX_VMS.toString());
                retVal = false;
            }
        }
        return retVal;
    }

    public static void updateImportedVmUsbPolicy(VmBase vmBase) {
        // Enforce disabled USB policy for Linux OS with legacy policy.
        if (osRepository.isLinux(vmBase.getOsId()) && vmBase.getUsbPolicy().equals(UsbPolicy.ENABLED_LEGACY)) {
            vmBase.setUsbPolicy(UsbPolicy.DISABLED);
        }
    }

    /**
     * Returns a <code>StorageDomain</code> in the given <code>StoragePool</code> that has
     * at least as much as requested free space and can be used to store memory images
     *
     * @param storagePoolId
     *           The storage pool where the search for a domain will be made
     * @param disksList
     *           Disks for which space is needed
     * @return storage domain in the given pool with at least the required amount of free space,
     *         or null if no such storage domain exists in the pool
     */
    public static StorageDomain findStorageDomainForMemory(Guid storagePoolId, List<DiskImage> disksList) {
        List<StorageDomain> domainsInPool = DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(storagePoolId);
        return findStorageDomainForMemory(domainsInPool, disksList);
    }

    protected static StorageDomain findStorageDomainForMemory(List<StorageDomain> domainsInPool, List<DiskImage> disksList) {
        for (StorageDomain currDomain : domainsInPool) {

            updateDisksStorage(currDomain, disksList);
            if (currDomain.getStorageDomainType().isDataDomain()
                    && currDomain.getStatus() == StorageDomainStatus.Active
                    && validateSpaceRequirements(currDomain, disksList)) {
                return currDomain;
            }
        }
        return null;
    }

    private static void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        for (DiskImage disk : disksList) {
            disk.setStorageIds(new ArrayList<Guid>(Collections.singletonList(storageDomain.getId())));
        }
        //There should be two disks in the disksList, first of which is memory disk. Only its volume type should be modified.
        updateDiskVolumeType(storageDomain.getStorageType(), disksList.get(0));
    }

    private static void updateDiskVolumeType(StorageType storageType, DiskImage disk) {
        VolumeType volumeType = storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        disk.setVolumeType(volumeType);
    }

    private static boolean validateSpaceRequirements(StorageDomain storageDomain, List<DiskImage> disksList) {
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);
        return (storageDomainValidator.isDomainWithinThresholds().isValid() &&
                storageDomainValidator.hasSpaceForClonedDisks(disksList).isValid());
    }

    public static ValidationResult canRunActionOnNonManagedVm(VM vm, VdcActionType actionType) {
        ValidationResult validationResult = ValidationResult.VALID;

        if ((vm.isHostedEngine() && !COMMANDS_ALLOWED_ON_HOSTED_ENGINE.contains(actionType)) ||
            (vm.isExternalVm() && !COMMANDS_ALLOWED_ON_EXTERNAL_VMS.contains(actionType))) {
            validationResult = new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_RUN_ACTION_ON_NON_MANAGED_VM);
        }

        return validationResult;
    }

    public static void decreasePendingVms(VM vm, Guid vdsId) {
        decreasePendingVms(vdsId, vm.getNumOfCpus(), vm.getMinAllocatedMem(), vm.getName());
    }

    public static void decreasePendingVms(Guid vdsId, int numOfCpus, int minAllocatedMem, String vmName) {
        getVdsDynamicDao().updatePartialVdsDynamicCalc(vdsId, 0, -numOfCpus, -minAllocatedMem, 0, 0);

        log.debug("Decreasing vds '{}' pending vcpu count by {} and vmem size by {} (Vm '{}')",
                vdsId, numOfCpus, minAllocatedMem, vmName);
    }

    private static VdsDynamicDAO getVdsDynamicDao() {
        return DbFacade.getInstance().getVdsDynamicDao();
    }

    public static void updateCurrentCd(Guid vdsId, VM vm, String currentCd) {
        VmDynamic vmDynamic = vm.getDynamicData();
        vmDynamic.setCurrentCd(currentCd);
        Backend.getInstance()
               .getResourceManager()
               .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                       new UpdateVmDynamicDataVDSCommandParameters(vdsId, vmDynamic));
    }

    public static void updateDefaultTimeZone(VmBase vmBase) {
        if (vmBase.getTimeZone() == null) {
            if (osRepository.isWindows(vmBase.getOsId())) {
                vmBase.setTimeZone(Config.<String> getValue(ConfigValues.DefaultWindowsTimeZone));
            } else {
                vmBase.setTimeZone(Config.<String> getValue(ConfigValues.DefaultGeneralTimeZone));
            }
        }
    }

    public static boolean isUpdateValidForVmDevices(Guid vmId, VMStatus vmStatus, Object objectWithEditableDeviceFields) {
        if (objectWithEditableDeviceFields == null) {
            return true;
        }
        return getVmDevicesFieldsToUpdateOnNextRun(vmId, vmStatus, objectWithEditableDeviceFields).isEmpty();
    }

    public static List<Pair<EditableDeviceOnVmStatusField, Boolean>> getVmDevicesFieldsToUpdateOnNextRun(
            Guid vmId, VMStatus vmStatus, Object objectWithEditableDeviceFields) {
        List<Pair<EditableDeviceOnVmStatusField, Boolean>> fieldList = new ArrayList<>();

        if (objectWithEditableDeviceFields == null) {
            return fieldList;
        }

        List<Pair<EditableDeviceOnVmStatusField , Field>> pairList = BaseHandler.extractAnnotatedFields(
                EditableDeviceOnVmStatusField.class, objectWithEditableDeviceFields.getClass());

        for (Pair<EditableDeviceOnVmStatusField, Field> pair : pairList) {
            EditableDeviceOnVmStatusField annotation = pair.getFirst();
            Field field = pair.getSecond();
            field.setAccessible(true);

            Boolean isEnabled = null;
            try {
                isEnabled = (Boolean) field.get(objectWithEditableDeviceFields);
            } catch (IllegalAccessException | ClassCastException e) {
                log.warn("VmHandler:: isUpdateValidForVmDevices: Reflection error");
                log.debug("Original exception was:", e);
            }

            // if device type is set to unknown, search by general type only
            // because some devices has more than one type, like sound can be ac97/ich6
            String device = null;
            if (annotation.type() != VmDeviceType.UNKNOWN) {
                device = annotation.type().getName();
            }

            if (isEnabled == null ||
                    !VmDeviceUtils.vmDeviceChanged(vmId, annotation.generalType(),
                            device, isEnabled)) {
                continue;
            }

            if (!VmHandler.isUpdateValidForVmDevice(field.getName(), vmStatus)) {
                fieldList.add(new Pair<>(annotation, isEnabled));
            }
        }

        return fieldList;
    }

    public static boolean isCpuSupported(int osId, Version version, String cpuName, ArrayList<String> canDoActionMessages) {
        if (osRepository.isCpuSupported(
                osId,
                version,
                CpuFlagsManagerHandler.getCpuId(cpuName, version))) {
            return true;
        }
        String unsupportedCpus = osRepository.getUnsupportedCpus(osId, version).toString();
        canDoActionMessages.add(VdcBllMessages.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS.name());
        canDoActionMessages.add("$unsupportedCpus " + StringUtils.strip(unsupportedCpus.toString(), "[]"));
        return false;
    }

    /**
     * preferred supports single pinned vnuma node (without that VM fails to run in libvirt).
     * used by add/update VM commands
     */
    public static ValidationResult checkNumaPreferredTuneMode(NumaTuneMode numaTuneMode,
            List<VmNumaNode> vmNumaNodes,
            Guid vmId) {
        // check tune mode
        if (numaTuneMode != NumaTuneMode.PREFERRED) {
            return ValidationResult.VALID;
        }

        if (vmNumaNodes == null && vmId != null) {
            vmNumaNodes = DbFacade.getInstance().getVmNumaNodeDAO().getAllVmNumaNodeByVmId(vmId);
        }

        // check single node pinned
        if (vmNumaNodes != null && vmNumaNodes.size() == 1) {
            List<Pair<Guid, Pair<Boolean, Integer>>> vdsNumaNodeList = vmNumaNodes.get(0).getVdsNumaNodeList();
            boolean pinnedToSingleNode = vdsNumaNodeList != null
                    && vdsNumaNodeList.size() == 1
                    && vdsNumaNodeList.get(0).getSecond() != null
                    && vdsNumaNodeList.get(0).getSecond().getFirst();
            if (pinnedToSingleNode) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(VdcBllMessages.VM_NUMA_NODE_PREFERRED_NOT_PINNED_TO_SINGLE_NODE);
    }
}
