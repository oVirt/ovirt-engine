package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.EditableField;
import org.ovirt.engine.core.common.businessentities.EditableOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
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
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class VmHandler {

    private static ObjectIdentityChecker mUpdateVmsStatic;
    private static OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);
    private static final Log log = LogFactory.getLog(VmHandler.class);

    private static Set<VdcActionType> COMMANDS_ALLOWED_ON_NON_MANAGED_VMS = new HashSet<>();
    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#InitHandlers
     */
    public static void Init() {
        Class<?>[] inspectedClassNames = new Class<?>[] {
                VmBase.class,
                VM.class,
                VmStatic.class,
                VmDynamic.class };

        mUpdateVmsStatic =
                new ObjectIdentityChecker(VmHandler.class, Arrays.asList(inspectedClassNames), VMStatus.class);

        for (Pair<EditableField, String> pair : BaseHandler.extractAnnotatedFields(EditableField.class,
                (inspectedClassNames))) {
            mUpdateVmsStatic.AddPermittedFields(pair.getSecond());
        }

        for (Pair<EditableOnVmStatusField, String> pair : BaseHandler.extractAnnotatedFields(EditableOnVmStatusField.class,
                inspectedClassNames)) {
            mUpdateVmsStatic.AddField(Arrays.asList(pair.getFirst().statuses()), pair.getSecond());
        }
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.InternalMigrateVm);
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.add(VdcActionType.VmLogon);
    }

    public static boolean isUpdateValid(VmStatic source, VmStatic destination, VMStatus status) {
        return mUpdateVmsStatic.IsUpdateValid(source, destination, status);
    }

    public static boolean isUpdateValid(VmStatic source, VmStatic destination) {
        return mUpdateVmsStatic.IsUpdateValid(source, destination);
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
    public static boolean VerifyAddVm(List<String> reasons,
            int nicsCount,
            int vmPriority) {
        boolean returnValue = true;
        if (MacPoolManager.getInstance().getAvailableMacsCount() < nicsCount) {
            if (reasons != null) {
                reasons.add(VdcBllMessages.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES.toString());
            }
            returnValue = false;
        } else if (!VmTemplateCommand.IsVmPriorityValueLegal(vmPriority, reasons)) {
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean isVmWithSameNameExistStatic(String vmName) {
        List<VmStatic> vmStatic = DbFacade.getInstance().getVmStaticDao().getAllByName(vmName);
        return (vmStatic.size() != 0);
    }

    public static void QueueAndLockVm(Guid vmId) {
        LockVm(vmId);
    }

    /**
     * Lock the VM in a new transaction, saving compensation data of the old status.
     *
     * @param vm
     *            The VM to lock.
     * @param compensationContext
     *            Used to save the old VM status, for compensation purposes.
     */
    public static void LockVm(final VmDynamic vm, final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                compensationContext.snapshotEntityStatus(vm, vm.getStatus());
                LockVm(vm.getId());
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
        LockVm(vmId);
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
        LockVm(vmDynamic, compensationContext);
    }

    public static void LockVm(Guid vmId) {
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
                compensationContext.snapshotEntityStatus(vm.getDynamicData(), vm.getStatus());
                UnLockVm(vm);
                compensationContext.stateChanged();
                return null;
            }
        });
    }

    public static void UnLockVm(VM vm) {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVmStatus,
                        new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Down));
        vm.setStatus(VMStatus.Down);
    }

    public static void MarkVmAsIllegal(Guid vmId) {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVmStatus,
                        new SetVmStatusVDSCommandParameters(vmId, VMStatus.ImageIllegal));
    }

    public static void updateDisksFromDb(VM vm) {
        List<Disk> imageList = DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId());
        vm.getDiskList().clear();
        vm.getDiskMap().clear();
        updateDisksForVm(vm, imageList);
    }

    public static void updateDisksForVm(VM vm, List<? extends Disk> diskList) {
        for (Disk disk : diskList) {
            if (disk.isAllowSnapshot()) {
                DiskImage image = (DiskImage) disk;
                if (image.getActive() != null && image.getActive()) {
                    vm.getDiskMap().put(image.getId(), image);
                    vm.getDiskList().add(image);
                }
            } else {
                vm.getDiskMap().put(disk.getId(), disk);
            }
        }
    }

    public static void updateNetworkInterfacesFromDb(VM vm) {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        vm.setInterfaces(interfaces);
    }

    private static Version GetApplicationVersion(final String part, final String appName) {
        try {
            return new RpmVersion(part, getAppName(part, appName), true);
        } catch (Exception e) {
            log.debugFormat("Failed to create rpm version object, part: {0} appName: {1}, error: {2}",
                    part,
                    appName,
                    e.toString());
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
    public static void UpdateVmGuestAgentVersion(final VM vm) {
        if (vm.getAppList() != null) {
            final String[] parts = vm.getAppList().split("[,]", -1);
            if (parts != null && parts.length != 0) {
                final List<String> possibleAgentAppNames = Config.<List<String>> GetValue(ConfigValues.AgentAppName);
                final Map<String, String> spiceDriversInGuest =
                        Config.<Map<String, String>> GetValue(ConfigValues.SpiceDriverNameInGuest);
                final String spiceDriverInGuest =
                        spiceDriversInGuest.get(osRepository.getOsFamily(vm.getOs()).toLowerCase());

                for (final String part : parts) {
                    for (String agentName : possibleAgentAppNames) {
                        if (StringUtils.containsIgnoreCase(part, agentName)) {
                            vm.setGuestAgentVersion(GetApplicationVersion(part, agentName));
                        }
                        if (StringUtils.containsIgnoreCase(part, spiceDriverInGuest)) {
                            vm.setSpiceDriverVersion(GetApplicationVersion(part, spiceDriverInGuest));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the validity of the given memory size according to OS type.
     *
     * @param osId
     *            Type of the os.
     * @param memSizeInMB
     *            The mem size in MB.
     * @param reasons
     *            The reasons.VdsGroups
     * @return
     */
    public static boolean isMemorySizeLegal(int osId,
                                            int memSizeInMB,
                                            List<String> reasons,
                                            Version clusterVersion) {
        boolean result = VmValidationUtils.isMemorySizeLegal(osId, memSizeInMB, clusterVersion);
        if (!result) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_MEMORY_SIZE.toString());
            reasons.add(String.format("$minMemorySize %s", VmValidationUtils.getMinMemorySizeInMb(osId, clusterVersion)));
            reasons.add(String.format("$maxMemorySize %s",
                    VmValidationUtils.getMaxMemorySizeInMb(osId, clusterVersion)));
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
    public static boolean IsNotDuplicateInterfaceName(List<VmNic> interfaces,
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
                Config.GetValue(ConfigValues.ValidNumOfMonitors).toString().replaceAll("[\\[\\]]", "");
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
            if (!Config.<Boolean> GetValue(ConfigValues.NativeUSBEnabled, vdsGroup.getcompatibility_version()
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
     * remove VMs unmanaged devices that are created during run-once or stateless run.
     *
     * @param vmId
     */
    public static void removeStatelessVmUnmanagedDevices(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);

        if (vm != null && vm.isStateless() || isRunOnce(vmId)) {

            final List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getUnmanagedDevicesByVmId(vmId);

            for (VmDevice device : vmDevices) {
                // do not remove device if appears in white list
                if (!VmDeviceCommonUtils.isInWhiteList(device.getType(), device.getDevice())) {
                    DbFacade.getInstance().getVmDeviceDao().remove(device.getId());
                }
            }
        }
    }

    /**
     * This method checks if we are stopping a VM that was started by run-once In such case we will may have 2 devices,
     * one managed and one unmanaged for CD or Floppy This is not supported currently by libvirt that allows only one
     * CD/Floppy This code should be removed if libvirt will support in future multiple CD/Floppy
     */
    private static boolean isRunOnce(Guid vmId) {
        List<VmDevice> cdList =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vmId,
                                VmDeviceGeneralType.DISK,
                                VmDeviceType.CDROM.getName());
        List<VmDevice> floppyList =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vmId,
                                VmDeviceGeneralType.DISK,
                                VmDeviceType.FLOPPY.getName());

        return (cdList.size() > 1 || floppyList.size() > 1);
    }

    /**
     * Returns a <code>StorageDomain</code> in the given <code>StoragePool</code> that has
     * at least as much as requested free space and can be used to store memory images
     *
     * @param storagePoolId
     *           The storage pool where the search for a domain will be made
     * @param sizeRequested
     *           The free size we need to have in the domain, in gigabytes
     * @return storage domain in the given pool with at least the required amount of free space,
     *         or null if no such storage domain exists in the pool
     */
    public static StorageDomain findStorageDomainForMemory(Guid storagePoolId, long sizeRequested) {
        return findStorageDomainForMemory(storagePoolId, sizeRequested, Collections.<StorageDomain, Integer>emptyMap());
    }

    /**
     * Returns a <code>StorageDomain</code> in the given <code>StoragePool</code> that has
     * at least as much as requested free space and can be used to store memory images
     *
     * @param storagePoolId
     *           The storage pool where the search for a domain will be made
     * @param sizeRequested
     *           The free size we need to have in the domain, in gigabytes
     * @param domain2reservedSpaceInDomain
     *           Maps storage domain to size we already reserved on it
     * @return storage domain in the given pool with at least the required amount of free space,
     *         or null if no such storage domain exists in the pool
     */
    public static StorageDomain findStorageDomainForMemory(Guid storagePoolId, long sizeRequested,
            Map<StorageDomain, Integer> domain2reservedSpaceInDomain) {
        List<StorageDomain> domainsInPool = DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(storagePoolId);
        for (StorageDomain currDomain : domainsInPool) {
            long reservedSizeForDisks = domain2reservedSpaceInDomain.containsKey(currDomain) ?
                    domain2reservedSpaceInDomain.get(currDomain) : 0;
            long sizeNeeded = sizeRequested + reservedSizeForDisks;
            if ((currDomain.getStorageDomainType().equals(StorageDomainType.Master)
                    || currDomain.getStorageDomainType().equals(StorageDomainType.Data))
                    && currDomain.getStatus() == StorageDomainStatus.Active
                    && doesStorageDomainHaveSpaceForRequest(currDomain, sizeNeeded)) {
                return currDomain;
            }
        }
        return null;
    }

    protected static boolean doesStorageDomainHaveSpaceForRequest(StorageDomain storageDomain, long sizeRequested) {
        // not calling validate in order not to add the messages per domain
        return (new StorageDomainValidator(storageDomain).isDomainHasSpaceForRequest(sizeRequested)).isValid();
    }

    public static ValidationResult canRunActionOnNonManagedVm(VM vm, VdcActionType actionType) {
        ValidationResult validationResult = ValidationResult.VALID;

        if (!isManagedVm(vm)) {
            if (!COMMANDS_ALLOWED_ON_NON_MANAGED_VMS.contains(actionType)) {
                validationResult = new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_RUN_ACTION_ON_NON_MANAGED_VM);
            }
        }
        return validationResult;
    }

    public static boolean isHostedEngine(VM vm) {
        return OriginType.HOSTED_ENGINE.equals(vm.getOrigin());
    }

    public static boolean isExternalVm(VM vm) {
        return OriginType.EXTERNAL.equals(vm.getOrigin());
    }

    public static boolean isManagedVm(VM vm) {
        return !isHostedEngine(vm) && !isExternalVm(vm);
    }

}
