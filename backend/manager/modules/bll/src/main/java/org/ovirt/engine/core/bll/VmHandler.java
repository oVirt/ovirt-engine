package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.scheduling.utils.NumaPinningHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CopyOnNewVersion;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.EditableVmField;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.TransientField;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.VmDeviceUpdate;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.VmActionByVmOriginTypeValidator;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.VirtioWinLoader;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VmHandler implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(VmHandler.class);

    @Inject
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private LockManager lockManager;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VmInitDao vmInitDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    protected SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Inject
    private SnapshotsManager snapshotsManager;

    @Inject
    private BackendInternal backend;

    @Inject
    private VmValidationUtils vmValidationUtils;

    @Inject
    private OsRepository osRepository;

    @Inject
    private VirtioWinLoader virtioWinLoader;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    @Inject
    private DiskHandler diskHandler;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private ObjectIdentityChecker updateVmsStatic;

    /**
     * Initialize list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#initHandlers
     */
    @PostConstruct
    public void init() {
        Class<?>[] inspectedClassNames = new Class<?>[] {
                VmBase.class,
                VM.class,
                VmStatic.class,
                VmDynamic.class,
                VmManagementParametersBase.class };

        updateVmsStatic =
                new ObjectIdentityChecker(VmHandler.class, Arrays.asList(inspectedClassNames));

        for (Pair<EditableDeviceOnVmStatusField, Field> pair : BaseHandler.extractAnnotatedFields(EditableDeviceOnVmStatusField.class,
                inspectedClassNames)) {
            updateVmsStatic.addField(Arrays.asList(pair.getFirst().statuses()), pair.getSecond().getName());
        }

        for (Pair<TransientField, Field> pair : BaseHandler.extractAnnotatedFields(TransientField.class,
                inspectedClassNames)) {
            updateVmsStatic.addTransientFields(pair.getSecond().getName());
        }

        for (Pair<EditableVmField, Field> pair : BaseHandler.extractAnnotatedFields(EditableVmField.class, inspectedClassNames)) {
            EditableVmField annotation = pair.getFirst();
            List<VMStatus> statusList = Arrays.asList(annotation.onStatuses());
            String fieldName = pair.getSecond().getName();

            if (statusList.isEmpty()) {
                updateVmsStatic.addPermittedFields(fieldName);
            } else {
                updateVmsStatic.addField(statusList, fieldName);

                if (!annotation.hotSettableOnStatus().getStates().isEmpty()) {
                    updateVmsStatic.addHotsetField(fieldName, annotation.hotSettableOnStatus().getStates());
                }
            }

            if (annotation.onHostedEngine()) {
                updateVmsStatic.addHostedEngineFields(fieldName);
            }
        }
        enableVmsToolVersionCheck();
    }

    void enableVmsToolVersionCheck() {
        executor.scheduleWithFixedDelay(this::performToolsVersionCheck,
                Config.<Integer>getValue(ConfigValues.WindowsGuestAgentUpdateCheckInternal),
                Config.<Integer>getValue(ConfigValues.WindowsGuestAgentUpdateCheckInternal),
                TimeUnit.SECONDS);
    }

    private void performToolsVersionCheck() {
        try {
            List<StoragePool> storagePools = storagePoolDao.getAllByStatus(StoragePoolStatus.Up);
            for (StoragePool sp : storagePools) {
                Guid storagePoolId = sp.getId();
                // If ISO Domains is active on the SP, the IsoDomainListSynchronizer will have interval check
                // therefore we skip the check here.
                if (isoDomainListSynchronizer.findActiveISODomain(storagePoolId) == null) {
                    refreshVmsToolsVersion(storagePoolId, Set.of());
                }
            }
        } catch (Throwable t){
            log.error("Exception while checking guest tools version: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }
    public boolean isUpdateValid(VmStatic source, VmStatic destination, VMStatus status) {
        return source.isManagedHostedEngine() ?
                updateVmsStatic.isHostedEngineUpdateValid(source, destination)
                : updateVmsStatic.isUpdateValid(source, destination, status);
    }

    public Set<String> getChangedFieldsForStatus(VmStatic source, VmStatic destination, VmManagementParametersBase params, VMStatus status) {
        List<String> fields = updateVmsStatic.getChangedFieldsForStatus(source, destination, status);
        List<VmDeviceUpdate> devices = getVmDevicesFieldsToUpdateOnNextRun(source.getId(), status, params);

        fields.addAll(devices.stream().map(VmDeviceUpdate::getName).collect(Collectors.toList()));
        return new HashSet<>(fields);
    }

    public boolean isUpdateValid(VmStatic source, VmStatic destination, VMStatus status, boolean hotsetEnabled) {
        return source.isManagedHostedEngine() ?
                updateVmsStatic.isHostedEngineUpdateValid(source, destination)
                : updateVmsStatic.isUpdateValid(source, destination, status, hotsetEnabled);
    }

    public boolean isUpdateValid(VmStatic source, VmStatic destination) {
        return source.isManagedHostedEngine() ?
                updateVmsStatic.isHostedEngineUpdateValid(source, destination)
                : updateVmsStatic.isUpdateValid(source, destination);
    }

    public boolean isUpdateValidForVmDevice(String fieldName, VMStatus status) {
        return updateVmsStatic.isFieldUpdatable(status, fieldName, null);
    }

    public boolean copyNonEditableFieldsToDestination(
            VmStatic source,
            VmStatic destination,
            boolean hotSetEnabled,
            VMStatus vmStatus,
            boolean hotMemoryUnplug) {
        final boolean succeeded =
                updateVmsStatic.copyNonEditableFieldsToDestination(source, destination, hotSetEnabled, vmStatus);
        if (!succeeded) {
            return false;
        }
        /*
        org.ovirt.engine.core.common.businessentities.EditableVmField annotation can't express dependency on
        `hotMemoryUnplug` parameter. Done manually.
         */
        if (source.getMemSizeMb() > destination.getMemSizeMb()
                && hotSetEnabled
                && !hotMemoryUnplug) {
            destination.setMemSizeMb(source.getMemSizeMb());
            destination.setMinAllocatedMem(source.getMinAllocatedMem());
        }
        return true;
    }

    public ValidationResult verifyMacPool(int nicsCount, MacPool macPool) {
        return ValidationResult.failWith(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES)
                .when(macPool.getAvailableMacsCount() < nicsCount);
    }

    /**
     * Determines whether VM priority value is in the correct range.
     *
     * @param value
     *            The value.
     * @return {@link ValidationResult#VALID} if VM priority value is in the correct range; otherwise a failing
     * {@code {@link ValidationResult}}
     */
    public ValidationResult isVmPriorityValueLegal(int value) {
        return ValidationResult.failWith(
                EngineMessage.VM_OR_TEMPLATE_ILLEGAL_PRIORITY_VALUE,
                String.format("$MaxValue %1$s", Config.<Integer> getValue(ConfigValues.VmPriorityMaxValue))
        ).unless(value >= 0 && value <= Config.<Integer> getValue(ConfigValues.VmPriorityMaxValue));
    }

    /**
     * Checks if VM with same name exists in the given DC. If no DC provided, check all VMs in the database.
     */
    public boolean isVmWithSameNameExistStatic(String vmName, Guid storagePoolId) {
        NameQueryParameters params = new NameQueryParameters(vmName);
        params.setDatacenterId(storagePoolId);
        QueryReturnValue result = backend.runInternalQuery(QueryType.IsVmWithSameNameExist, params);
        return (Boolean) result.getReturnValue();
    }

    public boolean isVmWithSameNameExistStatic(VmStatic vm, Guid storagePoolId) {
        return getVmNameValidator(vm.getOrigin()).isVmWithSameNameExistStatic(vm, storagePoolId);
    }

    /**
     * Lock the VM in a new transaction, saving compensation data of the old status.
     *
     * @param vm
     *            The VM to lock.
     * @param compensationContext
     *            Used to save the old VM status, for compensation purposes.
     */
    public void lockVm(final VmDynamic vm, final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(() -> {
            compensationContext.snapshotEntityStatus(vm);
            lockVm(vm.getId());
            compensationContext.stateChanged();
            return null;
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
            throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
        }
    }

    /**
     * Lock VM after check its status, If VM status is locked, we throw an exception.
     *
     * @param vmId
     *            - The ID of the VM.
     */
    public void checkStatusAndLockVm(Guid vmId) {
        VmDynamic vmDynamic = vmDynamicDao.get(vmId);
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
    public void checkStatusAndLockVm(Guid vmId, CompensationContext compensationContext) {
        VmDynamic vmDynamic = vmDynamicDao.get(vmId);
        checkStatusBeforeLock(vmDynamic.getStatus());
        lockVm(vmDynamic, compensationContext);
    }

    public void lockVm(Guid vmId) {
        vdsBrokerFrontend.runVdsCommand(VDSCommandType.SetVmStatus,
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
    public void unlockVm(final VM vm, final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(() -> {
            compensationContext.snapshotEntityStatus(vm.getDynamicData());
            unLockVm(vm);
            compensationContext.stateChanged();
            return null;
        });
    }

    public void unLockVm(VM vm) {
        vdsBrokerFrontend.runVdsCommand(VDSCommandType.SetVmStatus,
                new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Down));
        vm.setStatus(VMStatus.Down);
    }

    public void updateDisksFromDb(VM vm) {
        List<Disk> imageList = diskDao.getAllForVm(vm.getId());
        vm.clearDisks();
        updateDisksForVm(vm, imageList);
        updateDisksVmDataForVm(vm);
    }

    public void updateDisksForVm(VM vm, Collection<? extends Disk> disks) {
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

    public void updateDisksVmDataForVm(VM vm) {
        for (Disk disk : vm.getDiskMap().values()) {
            diskHandler.updateDiskVmElementFromDb(disk, vm.getId());
        }
    }


    /**
     * Fetch VmInit from Database
     * @param vm VmBase to set the VmInit into
     * @param secure if true don't return any password field
     * We want to set false only when running VM becase the VmInitDao
     * decrypt the password.
     */
    public void updateVmInitFromDB(VmBase vm, boolean secure) {
        vm.setVmInit(vmInitDao.get(vm.getId()));
        if (vm.getVmInit() != null) {
            if (secure) {
                vm.getVmInit().setPasswordAlreadyStored(!StringUtils.isEmpty(vm.getVmInit().getRootPassword()));
                vm.getVmInit().setRootPassword(null);
            } else {
                vm.getVmInit().setPasswordAlreadyStored(false);
            }
        }
    }

    public void addVmInitToDB(VmInit vmInit) {
        addVmInitToDB(vmInit, null);
    }

    public void addVmInitToDB(VmInit vmInit, CompensationContext compensationContext) {
        if (vmInit != null) {
            VmInit oldVmInit = vmInitDao.get(vmInit.getId());
            if (oldVmInit == null) {
                CompensationUtils.saveEntity(vmInit, vmInitDao, compensationContext);
            } else {
                if (vmInit.isPasswordAlreadyStored()) {
                    // since we are not always returning the password in
                    // updateVmInitFromDB()
                    // method (we don't want to display it in the UI/API) we
                    // don't want to override
                    // the password if the flag is on
                    vmInit.setRootPassword(oldVmInit.getRootPassword());
                }
                CompensationUtils.updateEntity(vmInit, oldVmInit, vmInitDao, compensationContext);
            }
        }
    }

    public void updateVmInitToDB(VmBase vm, CompensationContext compensationContext) {
        if (vm.getVmInit() != null) {
            addVmInitToDB(vm.getVmInit(), compensationContext);
        } else {
            removeVmInitFromDB(vm, compensationContext);
        }
    }

    public void removeVmInitFromDB(VmBase vm) {
        removeVmInitFromDB(vm, null);
    }

    public void removeVmInitFromDB(VmBase vm, CompensationContext compensationContext) {
        CompensationUtils.removeEntity(vm.getId(), vmInitDao, compensationContext);
    }

    public List<VmInit> getVmInitWithoutPasswordByIds(List<Guid> ids) {
        List<VmInit> all = vmInitDao.getVmInitByIds(ids);

        for (VmInit vmInit: all) {
                vmInit.setPasswordAlreadyStored(!StringUtils.isEmpty(vmInit.getRootPassword()));
                vmInit.setRootPassword(null);
        }
        return all;
    }

    /**
     * Filters the vm image disks/disk devices.<BR/>
     * note: luns will be filtered, only active image disks will be return.
     */
    public void filterImageDisksForVM(VM vm) {
        List<DiskImage> filteredDisks = DisksFilter.filterImageDisks(vm.getDiskMap().values(), ONLY_ACTIVE);
        List<CinderDisk> filteredCinderDisks = DisksFilter.filterCinderDisks(vm.getDiskMap().values());
        filteredDisks.addAll(filteredCinderDisks);
        filteredDisks.addAll(DisksFilter.filterManagedBlockStorageDisks(vm.getDiskMap().values()));
        @SuppressWarnings("unchecked")
        Collection<? extends Disk> vmDisksToRemove = CollectionUtils.subtract(vm.getDiskMap().values(), filteredDisks);
        vm.clearDisks();
        updateDisksForVm(vm, filteredDisks);
        for (Disk diskToRemove : vmDisksToRemove) {
            vm.getManagedVmDeviceMap().remove(diskToRemove.getId());
        }
    }

    public void updateNetworkInterfacesFromDb(VM vm) {
        List<VmNetworkInterface> interfaces = vmNetworkInterfaceDao.getAllForVm(vm.getId());
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
    public void updateVmGuestAgentVersion(final VM vm) {
        if (vm.getAppList() != null) {
            final String[] parts = vm.getAppList().split("[,]", -1);
            if (parts.length != 0) {
                final List<String> possibleAgentAppNames = Config.getValue(ConfigValues.AgentAppName);
                final Map<String, String> spiceDriversInGuest = Config.getValue(ConfigValues.SpiceDriverNameInGuest);
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

    public void updateVmLock(final VM vm) {
        vm.setLockInfo(lockManager.getLockInfo(String.format("%s%s", vm.getId(), LockingGroup.VM.name())));
    }

    public void updateOperationProgress(final VM vm) {
        VmManager vmManager = resourceManager.getVmManager(vm.getId(), false);
        if (vmManager != null) {
            vm.setBackgroundOperationDescription(vmManager.getConvertOperationDescription());
            vm.setBackgroundOperationProgress(vmManager.getConvertOperationProgress());
        } else {
            vm.setBackgroundOperationDescription(null);
            vm.setBackgroundOperationProgress(-1);
        }
    }

    public void updateVmStatistics(final VM vm) {
        VmManager vmManager = resourceManager.getVmManager(vm.getId(), false);
        if (vmManager != null) {
            vm.setStatisticsData(vmManager.getStatistics());
        }
    }

    public void updateConfiguredCpuVerb(final VM vm) {
        String configuredCpuVerb = cpuFlagsManagerHandler.getCpuId(
                        vm.getClusterCpuName(),
                        vm.getCompatibilityVersion());
        vm.setConfiguredCpuVerb(configuredCpuVerb);
    }

    public void updateIsDifferentTimeZone(VM vm, MemoizingSupplier<Function<String, Integer>> javaZoneIdToOffset) {
        String timeZone = vm.getTimeZone();
        if (timeZone != null && !timeZone.isEmpty() && osRepository.isWindows(vm.getOs())) {
            String javaZoneId = WindowsJavaTimezoneMapping.get(timeZone);
            int offset = javaZoneIdToOffset.get().apply(javaZoneId);
            int guestOffset = vm.getGuestOsTimezoneOffset() * 60; // convert to seconds
            vm.setDifferentTimeZone(guestOffset != offset);
        }
    }

    public MemoizingSupplier<Function<String, Integer>> getJavaZoneIdToOffsetFuncSupplier() {
        return new MemoizingSupplier<>(() -> {
            long now = System.currentTimeMillis();
            Map<String, Integer> cache = new HashMap<>();
            return javaZoneId -> cache.computeIfAbsent(javaZoneId, tz -> VmInfoBuildUtils.javaZoneToOffset(tz, now));
        });
    }

    public VmManagementParametersBase createVmManagementParametersBase(VM vm) {
        VmManagementParametersBase params = new VmManagementParametersBase(vm);
        List<VmDevice> devices = new ArrayList<>(vm.getManagedVmDeviceMap().values());

        vmDeviceUtils.updateVmDevicesInParameters(params, devices);

        return params;
    }

    public VM getNextRunVmConfiguration(Guid vmID, Guid userID, boolean isFiltered, boolean loadAdditionalInformation) {
        Snapshot snapshot = snapshotDao.get(
                vmID, Snapshot.SnapshotType.NEXT_RUN, userID, isFiltered);
        if (snapshot == null) {
            return null;
        }
        VM nextVM = snapshotVmConfigurationHelper.getVmFromConfiguration(snapshot);
        if (nextVM == null) {
            return null;
        }

        VmPropertiesUtils.getInstance().separateCustomPropertiesToUserAndPredefined(
            nextVM.getCompatibilityVersion(), nextVM.getStaticData());

        if (loadAdditionalInformation) {
            // update information that is not saved in the config
            updateDisksFromDb(nextVM);
            updateVmGuestAgentVersion(nextVM);
            updateNetworkInterfacesFromDb(nextVM);
            updateVmStatistics(nextVM);
        }
        return nextVM;
    }

    /**
     * Checks the validity of the given memory size according to OS type.
     *
     * @param vm
     *            a vm|template.
     * @param clusterVersion
     *            the vm's cluster version.
     */
    public void warnMemorySizeLegal(VmBase vm, Version clusterVersion) {
        if (!vmValidationUtils.isMemorySizeLegal(vm.getOsId(), vm.getMemSizeMb(), clusterVersion)) {
            AuditLogable logable = new AuditLogableImpl();
            logable.setVmId(vm.getId());
            logable.setVmName(vm.getName());
            logable.addCustomValue("VmMemInMb", String.valueOf(vm.getMemSizeMb()));
            logable.addCustomValue("VmMinMemInMb",
                    String.valueOf(vmValidationUtils.getMinMemorySizeInMb(vm.getOsId(), clusterVersion)));
            logable.addCustomValue("VmMaxMemInMb",
                    String.valueOf(vmValidationUtils.getMaxMemorySizeInMb(vm.getOsId(), clusterVersion)));

            auditLogDirector.log(logable, AuditLogType.VM_MEMORY_NOT_IN_RECOMMENDED_RANGE);
        }
    }

    public boolean isWindowsVm(VM vm) {
        return osRepository.isWindows(vm.getOs());
    }

    /**
     * Check if the OS type is supported.
     *
     * @param osId
     *            Type of the OS.
     * @param architectureType
     *            The architecture type.
     */
    public ValidationResult isOsTypeSupported(int osId, ArchitectureType architectureType) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_IS_NOT_SUPPORTED_BY_ARCHITECTURE_TYPE)
                .unless(vmValidationUtils.isOsTypeSupported(osId, architectureType));
    }

    /**
     * Check if the graphics and display types are supported.
     *
     * @param osId
     *            Type of the OS.
     * @param graphics
     *            Collection of graphics types (SPICE, VNC).
     * @param displayType
     *            Display type.
     * @param biosType
     *            Chipset/Firmware type.
     * @param clusterVersion
     *            The cluster version.
     */
    public ValidationResult isGraphicsAndDisplaySupported
        (int osId, Collection<GraphicsType> graphics, DisplayType displayType, BiosType biosType, Version clusterVersion) {
        if (!vmValidationUtils.isGraphicsAndDisplaySupported(osId, clusterVersion, graphics, displayType)) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_VM_DISPLAY_TYPE_IS_NOT_SUPPORTED_BY_OS);
        }
        if (displayType == DisplayType.bochs && (biosType == null || !biosType.isOvmf())) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_VM_DISPLAY_TYPE_IS_NOT_SUPPORTED_BY_FIRMWARE);
        }
        return ValidationResult.VALID;
    }

    /**
     * Check if the OS type is supported for VirtIO-SCSI.
     *
     * @param osId
     *            Type of the OS
     * @param clusterVersion
     *            Cluster's version
     */
    public ValidationResult isOsTypeSupportedForVirtioScsi(int osId, Version clusterVersion) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI)
                // VirtIO_SCSI is not chipset-dependent at the moment, so we can pass chipset == null to the call
                .unless(vmValidationUtils.isDiskInterfaceSupportedByOs(osId, clusterVersion, null,
                        DiskInterface.VirtIO_SCSI));
    }

    /**
     * Check if the interface name is not duplicate in the list of interfaces.
     *
     * @param interfaces
     *            - List of interfaces the VM/Template got.
     * @param candidateInterfaceName
     *            - Candidate for interface name.
     * @return - True , if name is valid, false, if name already exist.
     */
    public ValidationResult isNotDuplicateInterfaceName(List<VmNic> interfaces, final String candidateInterfaceName) {
        return ValidationResult.failWith(EngineMessage.NETWORK_INTERFACE_NAME_ALREADY_IN_USE)
                .when(interfaces.stream().anyMatch(i -> i.getName().equals(candidateInterfaceName)));
    }

    /**
     * Checks number of monitors validation according to VM and Graphics types.
     *
     * @param graphicsTypes
     *            Collection of graphics types of a VM.
     * @param numOfMonitors
     *            Number of monitors
     */
    public ValidationResult isNumOfMonitorsLegal(Collection<GraphicsType> graphicsTypes, int numOfMonitors) {
        boolean legal = false;

        if (graphicsTypes.contains(GraphicsType.VNC)) {
            legal = numOfMonitors <= 1;
        } else if (graphicsTypes.contains(GraphicsType.SPICE)) { // contains spice and doesn't contain vnc
            legal = numOfMonitors <= getMaxNumberOfMonitors();
        }

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS).unless(legal);
    }

    /**
     * get max of allowed monitors from config config value is a comma separated list of integers
     */
    private static int getMaxNumberOfMonitors() {
        List<String> values = Config.getValue(ConfigValues.ValidNumOfMonitors);
        return values.stream().map(String::trim).mapToInt(Integer::parseInt).max().orElse(0);
    }

    public static ValidationResult canRunActionOnNonManagedVm(VM vm, ActionType actionType) {
        ValidationResult validationResult = ValidationResult.VALID;

        if (!VmActionByVmOriginTypeValidator.isCommandAllowed(vm, actionType)) {
            validationResult = new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RUN_ACTION_ON_NON_MANAGED_VM);
        }

        return validationResult;
    }

    public void updateCurrentCd(VM vm, String currentCd) {
        VmDynamic vmDynamic = vm.getDynamicData();
        vmDynamic.setCurrentCd(currentCd);
        vdsBrokerFrontend.runVdsCommand(VDSCommandType.UpdateVmDynamicData,
                new UpdateVmDynamicDataVDSCommandParameters(vmDynamic));
    }

    public void updateDefaultTimeZone(VmBase vmBase) {
        if (vmBase.getTimeZone() == null) {
            if (osRepository.isWindows(vmBase.getOsId())) {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultWindowsTimeZone));
            } else {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultGeneralTimeZone));
            }
        }
    }

    public boolean isUpdateValidForVmDevices(Guid vmId, VMStatus vmStatus, Object objectWithEditableDeviceFields) {
        if (objectWithEditableDeviceFields == null) {
            return true;
        }
        return getVmDevicesFieldsToUpdateOnNextRun(vmId, vmStatus, objectWithEditableDeviceFields).isEmpty();
    }

    public List<VmDeviceUpdate> getVmDevicesFieldsToUpdateOnNextRun(
            Guid vmId, VMStatus vmStatus, Object objectWithEditableDeviceFields) {
        List<VmDeviceUpdate> fieldList = new ArrayList<>();

        if (objectWithEditableDeviceFields == null) {
            return fieldList;
        }

        List<Pair<EditableDeviceOnVmStatusField , Field>> pairList = BaseHandler.extractAnnotatedFields(
                EditableDeviceOnVmStatusField.class, objectWithEditableDeviceFields.getClass());

        for (Pair<EditableDeviceOnVmStatusField, Field> pair : pairList) {
            EditableDeviceOnVmStatusField annotation = pair.getFirst();
            Field field = pair.getSecond();
            field.setAccessible(true);

            if (isUpdateValidForVmDevice(field.getName(), vmStatus)) {
                // field may be updated on the current run, so not including for the next run
                continue;
            }

            try {
                Object value = field.get(objectWithEditableDeviceFields);
                if (value == null) {
                    // preserve current configuration
                } else if (value instanceof Boolean) {
                    addDeviceUpdateOnNextRun(vmId, annotation, null, value, fieldList);
                } else if (value instanceof VmManagementParametersBase.Optional) {
                    VmManagementParametersBase.Optional<?> optional = (VmManagementParametersBase.Optional<?>) value;
                    if (optional.isUpdate()) {
                        addDeviceUpdateOnNextRun(vmId, annotation, null, optional.getValue(), fieldList);
                    }
                } else if (value instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        boolean success = addDeviceUpdateOnNextRun(vmId, annotation,
                                entry.getKey(), entry.getValue(), fieldList);
                        if (!success) {
                            break;
                        }
                    }
                } else {
                    log.warn("getVmDevicesFieldsToUpdateOnNextRun: Unsupported field type: " +
                            value.getClass().getName());
                }
            } catch (IllegalAccessException | ClassCastException e) {
                log.warn("getVmDevicesFieldsToUpdateOnNextRun: Reflection error");
                log.debug("Original exception was:", e);
            }
        }

        return fieldList;
    }

    private boolean addDeviceUpdateOnNextRun(Guid vmId, EditableDeviceOnVmStatusField annotation,
                                                Object key, Object value, List<VmDeviceUpdate> updates) {
        return addDeviceUpdateOnNextRun(vmId, annotation.generalType(), annotation.type(), annotation.isReadOnly(),
                annotation.name(), key, value, updates);
    }

    private boolean addDeviceUpdateOnNextRun(Guid vmId, VmDeviceGeneralType generalType, VmDeviceType type,
                boolean readOnly, String name, Object key, Object value, List<VmDeviceUpdate> updates) {

        if (key != null) {
            VmDeviceGeneralType keyGeneralType = VmDeviceGeneralType.UNKNOWN;
            VmDeviceType keyType = VmDeviceType.UNKNOWN;

            if (key instanceof VmDeviceGeneralType) {
                keyGeneralType = (VmDeviceGeneralType) key;
            } else if (key instanceof VmDeviceType) {
                keyType = (VmDeviceType) key;
            } else if (key instanceof GraphicsType) {
                keyType = ((GraphicsType) key).getCorrespondingDeviceType();
            } else {
                log.warn("addDeviceUpdateOnNextRun: Unsupported map key type: " +
                        key.getClass().getName());
                return false;
            }

            if (keyGeneralType != VmDeviceGeneralType.UNKNOWN) {
                generalType = keyGeneralType;
            }
            if (keyType != VmDeviceType.UNKNOWN) {
                type = keyType;
            }
        }

        // if device type is set to unknown, search by general type only
        // because some devices have more than one type, like sound can be ac97/ich6
        String typeName = type != VmDeviceType.UNKNOWN ? type.getName() : null;

        if (value == null) {
            if (vmDeviceUtils.vmDeviceChanged(vmId, generalType, typeName, false)) {
                updates.add(new VmDeviceUpdate(generalType, type, readOnly, name, false));
            }
        } else if (value instanceof Boolean) {
            if (vmDeviceUtils.vmDeviceChanged(vmId, generalType, typeName, (Boolean) value)) {
                updates.add(new VmDeviceUpdate(generalType, type, readOnly, name, (Boolean) value));
            }
        } else if (value instanceof VmDevice) {
            if (vmDeviceUtils.vmDeviceChanged(vmId, generalType, typeName, (VmDevice) value)) {
                updates.add(new VmDeviceUpdate(generalType, type, readOnly, name, (VmDevice) value));
            }
        } else if (value instanceof VmWatchdog) {
            VmDevice watchdogDevice = ((VmWatchdog) value).createVmDevice();
            if (vmDeviceUtils.vmDeviceChanged(vmId, generalType, typeName, watchdogDevice)) {
                updates.add(new VmDeviceUpdate(generalType, type, readOnly, name,  watchdogDevice));
            }
        } else {
            log.warn("addDeviceUpdateOnNextRun: Unsupported value type: " +
                    value.getClass().getName());
            return false;
        }

        return true;
    }

    public ValidationResult isCpuSupported(int osId, Version version, String cpuName) {
        String cpuId = cpuFlagsManagerHandler.getCpuId(cpuName, version);
        if (cpuId == null) {
            return new ValidationResult(EngineMessage.CPU_TYPE_UNKNOWN);
        }
        if (!osRepository.isCpuSupported(
                osId,
                version,
                cpuId)) {
            String unsupportedCpus = osRepository.getUnsupportedCpus(osId, version).toString();
            return new ValidationResult(EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS,
                    "$unsupportedCpus " + StringUtils.strip(unsupportedCpus, "[]"));
        }
        return ValidationResult.VALID;
    }

    public void updateNumaNodesFromDb(VM vm){
        List<VmNumaNode> nodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());

        vm.setvNumaNodeList(nodes);
    }

    public static List<PermissionSubject> getPermissionsNeededToChangeCluster(Guid vmId, Guid clusterId) {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(vmId, VdcObjectType.VM, ActionGroup.EDIT_VM_PROPERTIES));
        permissionList.add(new PermissionSubject(clusterId, VdcObjectType.Cluster, ActionGroup.CREATE_VM));
        return permissionList;
    }


    /**
     * Returns graphics types of devices VM/Template is supposed to have after adding/updating.
     * <p/>
     * When adding, VM/Template inherits graphics devices from the template by default.
     * When updating, VM/Template has already some graphics devices set.
     * However - these devices can be customized (overriden) in params
     * (i.e. params can prevent a device to be inherited from a template).
     * <p/>
     *
     * @return graphics types of devices VM/Template is supposed to have after adding/updating.
     */
    public Set<GraphicsType> getResultingVmGraphics(List<GraphicsType> srcEntityGraphics, Map<GraphicsType, GraphicsDevice> graphicsDevices) {
        Set<GraphicsType> result = new HashSet<>();

        for (GraphicsType type : GraphicsType.values()) {
            if (graphicsDevices.get(type) != null) {
                result.add(type);
            }
        }

        if (result.isEmpty()) {// if graphics are set in params, do not use template graphics
            for (GraphicsType type : GraphicsType.values()) {
                if (srcEntityGraphics.contains(type) && !graphicsResetInParams(type, graphicsDevices)) { // graphics is in template and is not nulled in params
                    result.add(type);
                }
            }
        }

        return result;
    }

    /**
     * Returns true if given graphics type was reset in params (that means params contain given graphics device which
     * is set to null).
     */
    private static boolean graphicsResetInParams(GraphicsType type, Map<GraphicsType, GraphicsDevice> graphicsDevices) {
        return graphicsDevices.containsKey(type) && graphicsDevices.get(type) == null;
    }

    /**
     * Checks that dedicated host exists on the same cluster as the VM
     *
     * @param vm                  - the VM to check
     */
    public ValidationResult validateDedicatedVdsExistOnSameCluster(VmBase vm) {
        for (Guid vdsId : vm.getDedicatedVmForVdsList()) {
            // get dedicated host, checks if exists and compare its cluster to the VM cluster
            var vds = vdsStaticDao.get(vdsId);
            if (vds == null) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DEDICATED_VDS_DOES_NOT_EXIST);
            }
            if (!Objects.equals(vm.getClusterId(), vds.getClusterId())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER);
            }
        }
        return ValidationResult.VALID;
    }

    private static final Pattern TOOLS_PATTERN_1 = Pattern.compile("rhev-tools\\s+([\\d\\.]+)");
    private static final Pattern TOOLS_PATTERN_2 = Pattern.compile("ovirt guest tools\\s+([\\d\\.-]+)");
    private static final Pattern QEMU_GA_PATTERN_VER = Pattern.compile("qemu-guest-agent-([\\d\\.-]+)");
    private static final Pattern QEMU_GA_PATTERN = Pattern.compile("(?i:qemu-guest-agent-|QEMU guest agent)");

    private void updateOvirtGuestAgentStatus(VM vm, GuestAgentStatus ovirtGuestAgentStatus) {
        if (vm.getOvirtGuestAgentStatus() != ovirtGuestAgentStatus) {
            vm.setOvirtGuestAgentStatus(ovirtGuestAgentStatus);
            vmDynamicDao.updateOvirtGuestAgentStatus(vm.getId(), vm.getOvirtGuestAgentStatus());
        }
    }

    private void updateQemuGuestAgentStatus(VM vm, GuestAgentStatus qemuGuestAgentStatus) {
        if (vm.getQemuGuestAgentStatus() != qemuGuestAgentStatus) {
            vm.setQemuGuestAgentStatus(qemuGuestAgentStatus);
            vmDynamicDao.updateQemuGuestAgentStatus(vm.getId(), vm.getQemuGuestAgentStatus());
        }
    }

    /**
     * Looking for "RHEV_Tools x.x.x" in VMs app_list if found we look if there is a newer version in the isoList - if
     * so we update the GuestAgentStatus of VmDynamic to UpdateNeeded
     *
     * @param poolId
     *            storage pool id
     * @param isoList
     *            list of iso file names
     */
    public void refreshVmsToolsVersion(Guid poolId, Set<String> isoList) {
        Set<String> isoNamesAsSet = diskImageDao.getIsoDisksForStoragePoolAsRepoImages(poolId).stream()
                .map(RepoImage::getRepoImageName).collect(Collectors.toSet());
        isoNamesAsSet.addAll(isoList);
        String latestVersionWGT = getLatestGuestToolsVersion(isoNamesAsSet, isoDomainListSynchronizer.getRegexToolPattern());
        String latestVersionVIO = getLatestGuestToolsVersion(isoNamesAsSet, getRegexVirtIoIsoPattern());
        if (latestVersionWGT == null && latestVersionVIO == null) {
            return;
        }

        List<VM> vms = vmDao.getAllForStoragePool(poolId);
        for (VM vm : vms) {
            if (osRepository.isWindows(vm.getVmOsId())) {
                if (!FeatureSupported.isWindowsGuestToolsSupported(vm.getClusterCompatibilityVersion())) {
                    virtioWinLoader.load();
                    // in case we are suppose to use virtio-win RPM but it's not installed we can't check the status
                    if (virtioWinLoader.getVirtioIsoName() == null
                            || virtioWinLoader.getAgentVersionByOsName(vm.getVmOsId()) == null) {
                        return;
                    }
                    if (getLatestGuestToolsVersion(Set.of(virtioWinLoader.getVirtioIsoName()), getRegexVirtIoIsoPattern())
                            .equals(latestVersionVIO)) {
                        String availableAgent = virtioWinLoader.getAgentVersionByOsName(vm.getVmOsId());
                        if (availableAgent != null) {
                            maybeUpdateOvirtGuestAgentStatus(availableAgent, vm);
                        }
                    }
                } else {
                    maybeUpdateOvirtGuestAgentStatus(latestVersionWGT, vm);
                }
            }
            updateQemuGuestAgentStatus(vm, isQemuAgentInAppsList(vm.getAppList()));
        }
    }

    private void maybeUpdateOvirtGuestAgentStatus(String latestVersion, VM vm) {
        String toolsVersion = currentOvirtGuestAgentVersion(vm);
        if (toolsVersion != null) {
            if (new Version(toolsVersion).less(new Version(latestVersion))) {
                toolsVersion = toolsVersion.equals("0.0.0") ? "an old version of Windows Guest Tools" : toolsVersion;
                log.info("Newer version of guest agent is available {} for VM '{}' ({}) having {}",
                        latestVersion, vm.getName(), vm.getId(), toolsVersion);
                updateOvirtGuestAgentStatus(vm, GuestAgentStatus.UpdateNeeded);
            } else {
                updateOvirtGuestAgentStatus(vm, GuestAgentStatus.Exists);
            }
        } else {
            updateOvirtGuestAgentStatus(vm, GuestAgentStatus.DoesntExist);
        }
    }

    public String currentOvirtGuestAgentVersion(VM vm) {
        if (vm.getAppList() != null){
            if (!FeatureSupported.isWindowsGuestToolsSupported(vm.getCompatibilityVersion())) {
                if (vm.getAppList().toLowerCase().contains("qemu-guest-agent")) {
                    Matcher m = QEMU_GA_PATTERN_VER.matcher(vm.getAppList().toLowerCase());
                    if (m.find() && m.groupCount() > 0) {
                        return m.group(1);
                    }
                } else {
                    // Check if old WGT are installed and we need to sign it as need update.
                    if (isQemuAgentInAppsList(vm.getAppList()) == GuestAgentStatus.Exists) {
                        return "0.0.0";
                    }
                }
            } else if (vm.getAppList().toLowerCase().contains("rhev-tools")) {
                Matcher m = TOOLS_PATTERN_1.matcher(vm.getAppList().toLowerCase());
                if (m.find() && m.groupCount() > 0) {
                    return m.group(1);
                }
            } else if (vm.getAppList().toLowerCase().contains("ovirt guest tools")) {
                Matcher m = TOOLS_PATTERN_2.matcher(vm.getAppList().toLowerCase());
                if (m.find() && m.groupCount() > 0) {
                    return String.join(".",
                            Arrays.asList(m.group(1)
                                    .replace("-", ".")
                                    .split("\\."))
                                    .subList(0, 3));
                }
            }
        }
        return null;
    }


    GuestAgentStatus isQemuAgentInAppsList(String appList) {
        if (appList != null && QEMU_GA_PATTERN.matcher(appList).find()) {
            return GuestAgentStatus.Exists;
        } else {
            return GuestAgentStatus.DoesntExist;
        }
    }

    /**
     * iso file name that we are looking for: RHEV_toolsSetup_x.x_x.iso or RHV_toolsSetup_x.x_x.iso
     * in old clusters. Since 4.4 we are looking for virtio-win-x.x.x.iso.
     * Returning latest version only: x.x.x (ie 3.1.2)
     *
     * @param isoList list of iso file names.
     * @param pattern the pattern we search with.
     * @return latest iso version or null if no iso tools was found
     */
    protected String getLatestGuestToolsVersion(Set<String> isoList, String pattern) {
        Version latestVersion = null;
        Pattern toolsPattern = Pattern.compile(pattern);
        for (String iso: isoList) {
            Matcher m = toolsPattern.matcher(iso.toLowerCase());
            if (m.find()) {
                Version isoVersion = new Version(String.join(".",
                        m.group(IsoDomainListSynchronizer.TOOL_CLUSTER_LEVEL),
                        m.group(IsoDomainListSynchronizer.TOOL_VERSION)));
                if (latestVersion == null) {
                    latestVersion = isoVersion;
                } else if (latestVersion.compareTo(isoVersion) < 0) {
                    latestVersion = isoVersion;
                }
            }
        }
        return latestVersion != null ? latestVersion.toString() : null;
    }

    public String getRegexVirtIoIsoPattern() {
        return String.format("%1$s(?<%2$s>[0-9]{1,}.[0-9])\\.(?<%3$s>[0-9]{1,})[.\\w]*.[i|I][s|S][o|O]$",
                "virtio-win-",
                IsoDomainListSynchronizer.TOOL_CLUSTER_LEVEL,
                IsoDomainListSynchronizer.TOOL_VERSION);
    }

    /**
     * Copy fields that annotated with {@link org.ovirt.engine.core.common.businessentities.CopyOnNewVersion} from the new template version to the vm
     *
     * @param source
     *            - template to copy data from
     * @param dest
     *            - vm to copy data to
     */
    public static boolean copyData(VmBase source, VmBase dest) {
        for (Field srcFld : VmBase.class.getDeclaredFields()) {
            try {
                if (srcFld.getAnnotation(CopyOnNewVersion.class) != null) {
                    srcFld.setAccessible(true);

                    Field dstFld = VmBase.class.getDeclaredField(srcFld.getName());
                    dstFld.setAccessible(true);
                    dstFld.set(dest, srcFld.get(source));
                }
            } catch (Exception exp) {
                log.error("Failed to copy field '{}' of new version to VM '{}' ({}): {}",
                        srcFld.getName(),
                        source.getName(),
                        source.getId(),
                        exp.getMessage());
                log.debug("Exception", exp);
                return false;
            }
        }
        return true;
    }

    public void autoSelectUsbPolicy(VmBase fromParams) {
        if (fromParams.getUsbPolicy() == null) {
            fromParams.setUsbPolicy(fromParams.getVmType() == VmType.HighPerformance ? UsbPolicy.DISABLED : UsbPolicy.ENABLED_NATIVE);
        }
    }

    public ValidationResult validateSmartCardDevice(VmBase parametersStaticData) {
        if (parametersStaticData.isSmartcardEnabled() && parametersStaticData.getUsbPolicy() == UsbPolicy.DISABLED
                && parametersStaticData.getVmType() == VmType.HighPerformance) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SMARTCARD_NOT_SUPPORTED_WITHOUT_USB);
        }
        return ValidationResult.VALID;
    }

    public void updateCpuAndNumaPinning(VM vm, Guid vdsId) {
        VdsDynamic host = vdsDynamicDao.get(vdsId);
        NumaPinningHelper.applyAutoPinningPolicy(vm, host, vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(host.getId()));
    }

    public void autoSelectResumeBehavior(VmBase vmBase) {
        if (vmBase.isAutoStartup() && vmBase.getLeaseStorageDomainId() != null) {
            // since 4.2 the only supported resume behavior for HA vms with lease is kill
            vmBase.setResumeBehavior(VmResumeBehavior.KILL);
        }
    }

    /**
     * Automatic selection of display type based on its graphics types in parameters.
     * This method preserves backward compatibility for REST API - legacy REST API doesn't allow to set display and
     * graphics separately.
     */
    public void autoSelectDefaultDisplayType(Guid srcEntityId,
                                                VmBase parametersStaticData,
                                                Cluster cluster,
                                                Map<GraphicsType, GraphicsDevice> graphicsDevices) {
        if (parametersStaticData.getOsId() == OsRepository.AUTO_SELECT_OS) {
            return;
        }

        List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays = osRepository.getGraphicsAndDisplays(
                parametersStaticData.getOsId(),
                CompatibilityVersionUtils.getEffective(parametersStaticData, cluster));

        if (parametersStaticData.getDefaultDisplayType() != null
                && (parametersStaticData.getDefaultDisplayType() == DisplayType.none
                || isDisplayTypeSupported(parametersStaticData.getDefaultDisplayType(), graphicsAndDisplays))) {
            return;
        }

        DisplayType defaultDisplayType = null;
        // map holding display type -> set of supported graphics types for this display type
        Map<DisplayType, Set<GraphicsType>> displayGraphicsSupport = new LinkedHashMap<>();

        for (Pair<GraphicsType, DisplayType> graphicsAndDisplay : graphicsAndDisplays) {
            DisplayType display = graphicsAndDisplay.getSecond();
            if (!displayGraphicsSupport.containsKey(display)) {
                displayGraphicsSupport.put(display, new HashSet<>());
            }

            displayGraphicsSupport.get(display).add(graphicsAndDisplay.getFirst());
        }

        final List<GraphicsType> graphicsTypes = vmDeviceUtils.getGraphicsTypesOfEntity(srcEntityId);
        final Set<GraphicsType> resultingVmGraphics = getResultingVmGraphics(graphicsTypes, graphicsDevices);

        if (parametersStaticData.getVmType() == VmType.Server &&
                parametersStaticData.getBiosType() != null &&
                parametersStaticData.getBiosType().isOvmf()) {
            Set<GraphicsType> bochsGraphics = displayGraphicsSupport.get(DisplayType.bochs);
            if (bochsGraphics != null && bochsGraphics.containsAll(resultingVmGraphics)) {
                defaultDisplayType = DisplayType.bochs;
            }
        }

        if (defaultDisplayType == null) {
            for (Map.Entry<DisplayType, Set<GraphicsType>> entry : displayGraphicsSupport.entrySet()) {
                if (entry.getValue().containsAll(resultingVmGraphics)) {
                    defaultDisplayType = entry.getKey();
                    break;
                }
            }
        }

        if (defaultDisplayType == null) {
            if (!displayGraphicsSupport.isEmpty()) {// when not found otherwise, let's take osinfo's record as the default
                Map.Entry<DisplayType, Set<GraphicsType>> entry = displayGraphicsSupport.entrySet().iterator().next();
                defaultDisplayType = entry.getKey();
            } else {// no osinfo record
                defaultDisplayType = DisplayType.qxl;
            }
        }

        parametersStaticData.setDefaultDisplayType(defaultDisplayType);
    }

    public void autoSelectGraphicsDevice(Guid srcEntityId,
                                                VmStatic parametersStaticData,
                                                Cluster cluster,
                                                Map<GraphicsType, GraphicsDevice> graphicsDevices,
                                                Version compatibilityVersion) {
        if (graphicsDevices.isEmpty() // if not set by user in params
                && cluster != null) { // and Cluster is known
            DisplayType defaultDisplayType = parametersStaticData.getDefaultDisplayType();

            int osId = parametersStaticData.getOsId();

            List<GraphicsType> sourceGraphics = vmDeviceUtils.getGraphicsTypesOfEntity(srcEntityId);
            // if the source graphics device is supported then use it
            // otherwise choose the first supported graphics device
            if (!vmValidationUtils.isGraphicsAndDisplaySupported(osId, compatibilityVersion, sourceGraphics, defaultDisplayType)) {
                GraphicsType defaultGraphicsType = null;
                List<Pair<GraphicsType, DisplayType>> pairs = osRepository.getGraphicsAndDisplays(osId, compatibilityVersion);
                for (Pair<GraphicsType, DisplayType> pair : pairs) {
                    if (pair.getSecond().equals(defaultDisplayType)) {
                        defaultGraphicsType = pair.getFirst();
                        break;
                    }
                }

                if (defaultGraphicsType != null) {
                    for (GraphicsType graphicsType : GraphicsType.values()) {// reset graphics devices
                        graphicsDevices.put(graphicsType, null);
                    }

                    VmDeviceType vmDisplayType = defaultGraphicsType.getCorrespondingDeviceType();

                    GraphicsDevice defaultGraphicsDevice = new GraphicsDevice(vmDisplayType);
                    graphicsDevices.put(defaultGraphicsType, defaultGraphicsDevice);
                }
            }
        }
    }

    private static boolean isDisplayTypeSupported(DisplayType displayType, List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays) {
        for (Pair<GraphicsType, DisplayType> pair : graphicsAndDisplays) {
            if (displayType.equals(pair.getSecond())) {
                return true;
            }
        }
        return false;
    }

    private ValidationResult validateHostedEnginePhysicalMemory(VmBase vmBase) {
        if (vmBase.isHostedEngine()) {
            Optional<Integer> minPhysicalMem = vdsDao.getAllForCluster(vmBase.getClusterId()).stream()
                    .filter(v -> v.getStatus() == VDSStatus.Up && v.isHostedEngineHost())
                    .map(VDS::getPhysicalMemMb)
                    .min(Integer::compare);
            if (minPhysicalMem.isPresent() && vmBase.getMemSizeMb() >= minPhysicalMem.get()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PHYSICAL_MEMORY_CANNOT_BE_SMALLER_THAN_MEMORY_SIZE,
                        ReplacementUtils.createSetVariableString("physMemory", minPhysicalMem.get()),
                        ReplacementUtils.createSetVariableString("memory", vmBase.getMemSizeMb()));
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateMaxMemorySize(VmBase vmBase, Version effectiveCompatibilityVersion) {
        ValidationResult validateResult = validateHostedEnginePhysicalMemory(vmBase);
        if (!validateResult.isValid()) {
            return validateResult;
        }

        if (vmBase.getMaxMemorySizeMb() < vmBase.getMemSizeMb()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_MEMORY_CANNOT_BE_SMALLER_THAN_MEMORY_SIZE,
                    ReplacementUtils.createSetVariableString("maxMemory", vmBase.getMaxMemorySizeMb()),
                    ReplacementUtils.createSetVariableString("memory", vmBase.getMemSizeMb()));
        }
        final int maxMemoryUpperBound = VmCommonUtils.maxMemorySizeWithHotplugInMb(
                vmBase.getOsId(), effectiveCompatibilityVersion);
        if (vmBase.getMaxMemorySizeMb() > maxMemoryUpperBound) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_MEMORY_CANNOT_EXCEED_PLATFORM_LIMIT,
                    ReplacementUtils.createSetVariableString("maxMemory", vmBase.getMaxMemorySizeMb()),
                    ReplacementUtils.createSetVariableString("platformLimit", maxMemoryUpperBound));
        }
        return ValidationResult.VALID;
    }

    /**
     * OvfReader can't provide proper value of {@link VmBase#maxMemorySizeMb} since it depends on effective
     * compatibility version of target cluster.
     */
    public void updateMaxMemorySize(VmBase vmBase, Version effectiveCompatibilityVersion) {
        if (vmBase == null) {
            return;
        }
        final int maxOfMaxMemorySize =
                VmCommonUtils.maxMemorySizeWithHotplugInMb(vmBase.getOsId(), effectiveCompatibilityVersion);
        if (vmBase.getMaxMemorySizeMb() > maxOfMaxMemorySize) {
            vmBase.setMaxMemorySizeMb(maxOfMaxMemorySize);
            return;
        }
        if (vmBase.getMaxMemorySizeMb() == 0) {
            final int maxMemorySize = Math.min(
                    VmCommonUtils.getMaxMemorySizeDefault(vmBase.getMemSizeMb()),
                    maxOfMaxMemorySize);
            vmBase.setMaxMemorySizeMb(maxMemorySize);
        }
    }

    /**
     * @param objectWithEditableDeviceFields object with fields annotated with {@link EditableDeviceOnVmStatusField},
     *                                       usually a command parameters object
     */
    public void createNextRunSnapshot(
            VM existingVm,
            VmStatic newVmStatic,
            VmManagementParametersBase objectWithEditableDeviceFields,
            CompensationContext compensationContext) {
        // first remove existing snapshot
        Snapshot runSnap = snapshotDao.get(existingVm.getId(), Snapshot.SnapshotType.NEXT_RUN);
        if (runSnap != null) {
            snapshotDao.remove(runSnap.getId());
        }

        final VM newVm = new VM();
        newVm.setStaticData(newVmStatic);
        newVm.setClusterBiosType(existingVm.getClusterBiosType());
        newVm.setClusterArch(existingVm.getClusterArch());

        Set<String> changedFields = getChangedFieldsForStatus(
                existingVm.getStaticData(),
                newVm.getStaticData(),
                objectWithEditableDeviceFields,
                VMStatus.Up);

        // create new snapshot with new configuration
        snapshotsManager.addSnapshot(Guid.newGuid(),
                "Next Run configuration snapshot",
                Snapshot.SnapshotStatus.OK,
                Snapshot.SnapshotType.NEXT_RUN,
                newVm,
                true,
                changedFields,
                null,
                null,
                null,
                Collections.emptyList(),
                vmDeviceUtils.getVmDevicesForNextRun(existingVm,
                        objectWithEditableDeviceFields,
                        existingVm.getDefaultDisplayType()),
                compensationContext);
    }

    /**
     * Marks given VM to be in case of guest-initiated reboot destroyed (instead of normally rebooting), thus triggering
     * the engine-driven Cold Reboot logic used for engine-initiated VM reboots (in case of RunOnce or configuration change.)
     *
     * @see ColdRebootAutoStartVmsRunner
     */
    public void setVmDestroyOnReboot(VM vm) {
        if (vm.isRunning()) {
            vdsBrokerFrontend.runVdsCommand(VDSCommandType.SetDestroyOnReboot,
                    new VdsAndVmIDVDSParametersBase(vm.getRunOnVds(), vm.getId()));
        }
    }

    private VmNameValidator getVmNameValidator(OriginType originType) {
        if (originType == OriginType.KUBEVIRT) {
            return new KubevirtVmValidator();
        } else {
            return new ManagedVmValidator();
        }
    }

    public ValidationResult validateCpuPinningPolicy(VmBase vmBase, boolean numaSet) {
        ValidationResult result = ValidationResult.VALID;

        switch (vmBase.getCpuPinningPolicy()) {
        case MANUAL:
            if (StringUtils.isBlank(vmBase.getCpuPinning())) {
                result = new ValidationResult(EngineMessage.ACTION_TYPE_CANNOT_SET_MANUAL_PINNING);
            }
            break;
        case RESIZE_AND_PIN_NUMA:
            if (numaSet) {
                result = new ValidationResult(EngineMessage.ACTION_TYPE_CANNOT_RESIZE_AND_PIN_AND_NUMA_SET);
                break;
            }
            boolean singleCoreHostFound = vmBase.getDedicatedVmForVdsList()
                    .stream()
                    .map(vdsId -> vdsDynamicDao.get(vdsId))
                    .anyMatch(vdsDynamic -> vdsDynamic.getCpuCores() / vdsDynamic.getCpuSockets() == 1);
            if (singleCoreHostFound) {
                result = new ValidationResult(EngineMessage.ACTION_TYPE_CANNOT_RESIZE_AND_PIN_SINGLE_CORE);
            }
            break;
        default:
            break;
        }

        return result;
    }

    public void updateToQ35(VmStatic oldVm,
                            VmStatic newVm,
                            Cluster cluster,
                            CompensationContext compensationContext) {
        updateToQ35(oldVm, newVm, cluster, compensationContext, false);
    }

    public void updateToQ35(VmStatic oldVm,
            VmStatic newVm,
            Cluster cluster,
            CompensationContext compensationContext,
            boolean forceVmStaticUpdate) {

        if (newVm.getBiosType() == BiosType.I440FX_SEA_BIOS
                && cluster.getBiosType().getChipsetType() == ChipsetType.Q35
                && osRepository.isQ35Supported(newVm.getOsId())) {
            newVm.setBiosType(BiosType.Q35_SEA_BIOS);
        }

        if (forceVmStaticUpdate || oldVm.getBiosType() != newVm.getBiosType()) {
            VmManager vmManager = resourceManager.getVmManager(newVm.getId());
            vmManager.update(newVm);

            if (oldVm.getBiosType().getChipsetType() != newVm.getBiosType().getChipsetType()) {
                convertVmToNewChipset(oldVm,
                        newVm,
                        cluster,
                        compensationContext);
            }
        }
    }

    public void convertVmToNewChipset(VmBase oldVmBase,
            VmBase newVmBase,
            Cluster cluster,
            CompensationContext compensationContext) {
        vmDeviceUtils.updateSoundDevice(oldVmBase,
                newVmBase,
                CompatibilityVersionUtils.getEffective(newVmBase, cluster),
                vmDeviceUtils.hasSoundDevice(oldVmBase.getId()));
        vmDeviceUtils.updateUsbSlots(oldVmBase, newVmBase, cluster);
        convertVmToNewChipset(newVmBase.getId(), newVmBase.getBiosType().getChipsetType(), compensationContext);
    }

    public void convertVmToNewChipset(Guid vmId, ChipsetType newChipsetType, CompensationContext compensationContext) {
        convertVmDisksToNewChipset(vmId, newChipsetType, compensationContext);
        vmDeviceUtils.convertVmDevicesToNewChipset(vmId, newChipsetType, compensationContext != null);
    }

    private void convertVmDisksToNewChipset(Guid vmId, ChipsetType newChipsetType, CompensationContext compensationContext) {
        log.info("Converting all disks for VM with id {} to new chipset {}", vmId, newChipsetType);
        List<DiskVmElement> diskVmElements = diskVmElementDao.getAllForVm(vmId);
        for (DiskVmElement diskVmElement : diskVmElements) {
            if (compensationContext != null) {
                CompensationUtils.<VmDeviceId, DiskVmElement> updateEntity(diskVmElement, el -> {
                    convertVmDiskToNewChipset(el, newChipsetType);
                }, diskVmElementDao, compensationContext);
            } else {
                convertVmDiskToNewChipset(diskVmElement, newChipsetType);
            }
        }

        if (compensationContext == null) {
            for (DiskVmElement diskVmElement : diskVmElements) {
                diskVmElementDao.update(diskVmElement);
            }
        }
    }

    private void convertVmDiskToNewChipset(DiskVmElement diskVmElement, ChipsetType newChipsetType) {
        if (DiskInterface.IDE == diskVmElement.getDiskInterface() && ChipsetType.Q35 == newChipsetType) {
            diskVmElement.setDiskInterface(DiskInterface.SATA);
        } else if (DiskInterface.SATA == diskVmElement.getDiskInterface() && ChipsetType.I440FX == newChipsetType) {
            diskVmElement.setDiskInterface(DiskInterface.IDE);
        }
    }

    public Boolean getMigrateEncrypted(VM vm, Cluster cluster) {
        Version version = vm.getCompatibilityVersion();
        if (version == null) {
            return null;
        }

        if (!FeatureSupported.isMigrateEncryptedSupported(version)) {
            return null;
        }

        if (vm.getMigrateEncrypted() != null) {
            return vm.getMigrateEncrypted();
        }

        if (cluster.getMigrateEncrypted() != null) {
            return cluster.getMigrateEncrypted();
        }

        return Config.getValue(ConfigValues.DefaultMigrationEncryption);
    }

    private interface VmNameValidator {
        boolean isVmWithSameNameExistStatic(VmStatic vm, Guid storagePoolId);
    }

    private class ManagedVmValidator implements VmNameValidator {

        @Override
        public boolean isVmWithSameNameExistStatic(VmStatic vm, Guid storagePoolId) {
            return VmHandler.this.isVmWithSameNameExistStatic(vm.getName(), storagePoolId);
        }
    }

    private class KubevirtVmValidator implements VmNameValidator {

        @Override public boolean isVmWithSameNameExistStatic(VmStatic vm, Guid storagePoolId) {
            return vmDao.getByNameAndNamespaceForCluster(vm.getClusterId(), vm.getName(), vm.getNamespace()) != null;
        }
    }
}
