package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UnchangeableByVdsm;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NumaUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DestroyVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FullListVdsCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmsMonitoring {

    private VDS vds;
    private VdsManager vdsManager;
    private final Map<Guid, VM> vmDict;
    private Map<Guid, VmInternalData> runningVms;
    private Map<Guid, VmManager> vmManagers;

    private int memCommited;
    private int vmsCoresCount;
    private final Map<Guid, VmDynamic> vmDynamicToSave = new HashMap<>();
    private final Map<Guid, VmStatistics> vmStatisticsToSave = new HashMap<>();
    private final Map<Guid, List<VmNetworkInterface>> vmInterfaceStatisticsToSave = new HashMap<>();
    private final Map<Guid, DiskImageDynamic> vmDiskImageDynamicToSave = new HashMap<>();
    private final Map<VmDeviceId, VmDevice> vmDeviceToSave = new HashMap<>();
    private final Map<VM, VmDynamic> vmsClientIpChanged = new HashMap<>();
    private final Map<Guid, List<VmGuestAgentInterface>> vmGuestAgentNics = new HashMap<>();
    private final List<VmDynamic> poweringUpVms = new ArrayList<>();
    private final List<Guid> vmsMovedToDown = new ArrayList<>();
    private final List<Guid> vmsToRemoveFromAsync = new ArrayList<>();
    private final List<Guid> succededToRunVms = new ArrayList<>();
    private final List<VmDevice> newVmDevices = new ArrayList<>();
    private final List<VmDeviceId> removedDeviceIds = new ArrayList<>();
    private final List<LUNs> vmLunDisksToSave = new ArrayList<>();
    private final List<Guid> vmsToRerun = new ArrayList<>();
    private final List<Guid> autoVmsToRun = new ArrayList<>();
    private final List<VmStatic> externalVmsToAdd = new ArrayList<>();
    private final Map<Guid, VmJob> vmJobsToUpdate = new HashMap<>();
    private final List<Guid> vmJobIdsToRemove = new ArrayList<>();
    private final List<Guid> existingVmJobIds = new ArrayList<>();
    /** Pairs of VM and the ID of the VDS it should be destroyed from */
    private final List<Pair<VM, Guid>> vmsToDestroy = new LinkedList<>();

    private static final Map<Guid, Integer> vmsWithBalloonDriverProblem = new HashMap<>();
    private static final Map<Guid, Integer> vmsWithUncontrolledBalloon = new HashMap<>();

    private static final int TO_MEGA_BYTES = 1024;
    private static final String HOSTED_ENGINE_VM_NAME = "HostedEngine";
    private static final String EXTERNAL_VM_NAME_FORMAT = "external-%1$s";
    private static final Logger log = LoggerFactory.getLogger(VmsMonitoring.class);

    /** names of fields in {@link VmDynamic} that are not changed by VDSM */
    private static final List<String> UNCHANGEABLE_FIELDS_BY_VDSM;

    static {
        List<String> tmpList = new ArrayList<String>();
        for (Field field : VmDynamic.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(UnchangeableByVdsm.class)) {
                tmpList.add(field.getName());
            }
        }
        UNCHANGEABLE_FIELDS_BY_VDSM = Collections.unmodifiableList(tmpList);
    }

    public VmsMonitoring(VdsManager vdsManager) {
        //TODO this query for vds is heavy and redundant -
        //TODO vds will be changed to VdsStatic or even a mini view entity -
        //TODO there is no need to fetch the whole entity,
        //TODO this means removing VDS from parameters classes of VdsBrokerCommands
        vds = getDbFacade().getVdsDao().get(vdsManager.getVdsId());
        this.vdsManager = vdsManager;
        this.vmDict = getDbFacade().getVmDao().getAllRunningByVds(vds.getId());
        vmManagers = new HashMap<>(vmDict.size()); // max size is the one's that in the db
    }

    /**
     * analyze and react upon changes on the monitoredVms. relevant changes would
     * be persisted and state transitions and internal commands would
     * take place accordingly.
     */
    public void perform() {
        try {
            lockVmsManager();
            refreshVmStats();
            saveVmsToDb();
            afterVMsRefreshTreatment();
            vdsManager.updateMetrics(memCommited, vmsCoresCount);
        } finally {
            unlockVmsManager();
        }

    }

    private void lockVmsManager() {
        for (Guid vmId : vmDict.keySet()) {
            VmManager vmManager = ResourceManager.getInstance().getVmManager(vmId);
            if (vmManager.trylock()) {
                // store the locked managers to finally release them at the end of the cycle
                vmManagers.put(vmId, vmManager);
            }
        }
    }

    private void unlockVmsManager() {
        for (VmManager vmManager : vmManagers.values()) {
            vmManager.unlock();
        }
    }

    private void refreshVmStats() {
        log.debug("refresh VMs list entered");

        // Retrieve the list of existing jobs and/or job placeholders.  Only these jobs
        // are allowed to be updated by updateVmJobs()
        refreshExistingVmJobList();

        fetchRunningVms();
        // refreshCommitedMemory must be called before we modify runningVms, because
        // we iterate over it there, assuming it is the same as it was received from VDSM
        refreshCommitedMemory();

        filterVmsFromMonitoringCycle();

        List<Guid> staleRunningVms = checkVmsStatusChanged();

        proceedWatchdogEvents();

        proceedBalloonCheck();

        proceedDownVms();

        proceedGuaranteedMemoryCheck();

        processExternallyManagedVms();
        // update repository and check if there are any vm in cache that not
        // in vdsm
        updateRepository(staleRunningVms);
        // Going over all returned VMs and updting the data structures
        // accordingly

        // checking the db for incoherent vm status;
        // setVmStatusDownForVmNotFound();

        // Handle VM devices were changed (for 3.1 cluster and above)
        if (!VmDeviceCommonUtils.isOldClusterVersion(vds.getVdsGroupCompatibilityVersion())) {
            handleVmDeviceChange();
        }

        prepareGuestAgentNetworkDevicesForUpdate();

        updateLunDisks();

        updateVmJobs();
    }

    /**
     * if we can't hold this VM lock we filter it out and
     * so we don't try to detect state-transition and so on.
     * - VMs which are anyway not exist in db should never be filtered out
     * - metrics calculation like memCommited and vmsCoresCount should be calculated *before*
     *   this filtering.
     */
    private void filterVmsFromMonitoringCycle() {
        for (Guid vmId : runningVms.keySet()) {
            if (!vmManagers.containsKey(vmId)) {
                runningVms.remove(vmId);
                vmDict.remove(vmId);
                log.debug("skipping VM '{}' from this monitoring cycle -" +
                        " the VM is locked by its VmManager ", vmId );
            }
        }
    }

    /**
     * fetch running VMs and populate the internal structure.
     */
    protected void fetchRunningVms() {
        VDSCommandType commandType =
                vdsManager.getRefreshStatistics()
                        ? VDSCommandType.GetAllVmStats
                        : VDSCommandType.List;
        VDSReturnValue vdsReturnValue =
                getResourceManager().runVdsCommand(commandType, new VdsIdAndVdsVDSCommandParametersBase(vds));
        runningVms = (Map<Guid, VmInternalData>) vdsReturnValue.getReturnValue();
    }

    private void refreshCommitedMemory() {
        memCommited = vds.getGuestOverhead();
        vmsCoresCount = 0;

        for (VmInternalData runningVm : runningVms.values()) {
            VmDynamic vmDynamic = runningVm.getVmDynamic();
            // VMs' pending resources are cleared in powering up, so in launch state
            // we shouldn't include them as committed.
            if (vmDynamic.getStatus() != VMStatus.WaitForLaunch &&
                    vmDynamic.getStatus() != VMStatus.Down) {
                VM vm = vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    memCommited += vm.getVmMemSizeMb();
                    memCommited += vds.getGuestOverhead();
                    vmsCoresCount += vm.getNumOfCpus();
                }
            }
        }
    }

    // if not statistics check if status changed return a list of those
    protected List<Guid> checkVmsStatusChanged() {
        List<Guid> staleRunningVms = new ArrayList<>();
        if (!vdsManager.getRefreshStatistics()) {
            List<VmDynamic> tempRunningList = new ArrayList<VmDynamic>();
            for (VmInternalData runningVm : runningVms.values()) {
                tempRunningList.add(runningVm.getVmDynamic());
            }
            for (VmDynamic runningVm : tempRunningList) {
                VM vmToUpdate = vmDict.get(runningVm.getId());

                boolean statusChanged = false;
                if (vmToUpdate == null
                        || (vmToUpdate.getStatus() != runningVm.getStatus() &&
                        !(vmToUpdate.getStatus() == VMStatus.PreparingForHibernate && runningVm.getStatus() == VMStatus.Up)) ) {
                    VDSReturnValue vmStats =
                            getResourceManager().runVdsCommand(
                                    VDSCommandType.GetVmStats,
                                    new GetVmStatsVDSCommandParameters(vds, runningVm.getId()));
                    if (vmStats.getSucceeded()) {
                        runningVms.put(runningVm.getId(), (VmInternalData) vmStats.getReturnValue());
                        statusChanged = true;
                    } else {
                        if (vmToUpdate != null) {
                            log.error(
                                    "failed to fetch '{}' stats. status remain unchanged '{}'",
                                    vmToUpdate.getName(),
                                    vmToUpdate.getStatus());
                        }
                    }
                }

                if (!statusChanged) {
                    // status not changed move to next vm
                    staleRunningVms.add(runningVm.getId());
                    runningVms.remove(runningVm.getId());
                }
            }
        }
        return staleRunningVms;
    }

    private void proceedWatchdogEvents() {
        for (VmInternalData vmInternalData : runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            VM vmTo = vmDict.get(vmDynamic.getId());
            if (isNewWatchdogEvent(vmDynamic, vmTo)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmDynamic.getId());
                auditLogable.addCustomValue("wdaction", vmDynamic.getLastWatchdogAction());
                // for the interpretation of vdsm's response see http://docs.python.org/2/library/time.html
                auditLogable.addCustomValue("wdevent",
                        ObjectUtils.toString(new Date(vmDynamic.getLastWatchdogEvent().longValue() * 1000)));
                AuditLogDirector.log(auditLogable, AuditLogType.WATCHDOG_EVENT);
            }
        }
    }

    private void proceedBalloonCheck() {
        if (vds.isBalloonEnabled()) {
            for (VmInternalData vmInternalData : runningVms.values()) {
                VmBalloonInfo balloonInfo = vmInternalData.getVmStatistics().getVmBalloonInfo();
                Guid vmId = vmInternalData.getVmDynamic().getId();
                if (vmDict.get(vmId) == null) {
                    continue; // if vm is unknown - continue
                }

                if (isBalloonDeviceActiveOnVm(vmInternalData)
                        && (Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())
                        || !isBalloonWorking(balloonInfo))) {
                    vmBalloonDriverIsRequestedAndUnavailable(vmId);
                } else {
                    vmBalloonDriverIsNotRequestedOrAvailable(vmId);
                }

                // save the current value for the next time we check it
                balloonInfo.setBalloonLastMemory(balloonInfo.getCurrentMemory());

                if (vmInternalData.getVmStatistics().getusage_mem_percent() != null
                        && vmInternalData.getVmStatistics().getusage_mem_percent() == 0  // guest agent is down
                        && balloonInfo.isBalloonDeviceEnabled() // check if the device is present
                        && !Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())) {
                    guestAgentIsDownAndBalloonInfalted(vmId);
                } else {
                    guestAgentIsUpOrBalloonDeflated(vmId);
                }

            }
        }
    }

    /**
     * Delete all vms with status Down
     */
    private void proceedDownVms() {
        for (VmInternalData vmInternalData : runningVms.values()) {
            VmDynamic vm = vmInternalData.getVmDynamic();
            if (vm.getStatus() != VMStatus.Down) {
                continue;
            }

            VM vmTo = vmDict.get(vm.getId());
            VMStatus status = VMStatus.Unassigned;
            if (vmTo != null) {
                status = vmTo.getStatus();
                proceedVmBeforeDeletion(vmTo, vm);

                // when going to suspend, delete vm from cache later
                if (status == VMStatus.SavingState) {
                    ResourceManager.getInstance().InternalSetVmStatus(vmTo, VMStatus.Suspended);
                }

                clearVm(vmTo, vmInternalData.getVmDynamic().getExitStatus(), vmInternalData.getVmDynamic()
                        .getExitMessage(), vmInternalData.getVmDynamic().getExitReason());
            }

            VmStatistics vmStatistics = getDbFacade().getVmStatisticsDao().get(vm.getId());
            if (vmStatistics != null) {
                DestroyVDSCommand<DestroyVmVDSCommandParameters> vdsBrokerCommand =
                        new DestroyVDSCommand<DestroyVmVDSCommandParameters>(new DestroyVmVDSCommandParameters(
                                vds.getId(), vm.getId(), false, false, 0));
                vdsBrokerCommand.execute();

                if (vmTo != null && status == VMStatus.SavingState) {
                    afterSuspendTreatment(vm);
                } else if (status != VMStatus.MigratingFrom) {
                    handleVmOnDown(vmTo, vm, vmStatistics);
                }
            }
        }
    }

    private void proceedGuaranteedMemoryCheck() {
        for (VmInternalData vmInternalData : runningVms.values()) {
            VM savedVm = vmDict.get(vmInternalData.getVmDynamic().getId());
            if (savedVm == null) {
                continue;
            }
            VmStatistics vmStatistics = vmInternalData.getVmStatistics();
            if (vmStatistics != null && vmStatistics.getVmBalloonInfo().getCurrentMemory() != null &&
                    vmStatistics.getVmBalloonInfo().getCurrentMemory() > 0 &&
                    savedVm.getMinAllocatedMem() > vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.addCustomValue("VmName", savedVm.getName());
                auditLogable.addCustomValue("VdsName", this.vds.getName());
                auditLogable.addCustomValue("MemGuaranteed", String.valueOf(savedVm.getMinAllocatedMem()));
                auditLogable.addCustomValue("MemActual",
                        Long.toString((vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES)));
                auditLog(auditLogable, AuditLogType.VM_MEMORY_UNDER_GUARANTEED_VALUE);
            }

        }
    }

    protected boolean isBalloonWorking(VmBalloonInfo balloonInfo) {
        return (Math.abs(balloonInfo.getBalloonLastMemory() - balloonInfo.getBalloonTargetMemory())
                > Math.abs(balloonInfo.getCurrentMemory() - balloonInfo.getBalloonTargetMemory()));
    }

    protected void processExternallyManagedVms() {
        List<String> vmsToQuery = new ArrayList<String>();
        // Searching for External VMs that run on the host
        for (VmInternalData vmInternalData : runningVms.values()) {
            VM currentVmData = vmDict.get(vmInternalData.getVmDynamic().getId());
            if (currentVmData == null) {
                if (getDbFacade().getVmStaticDao().get(vmInternalData.getVmDynamic().getId()) == null) {
                    Guid vmId = vmInternalData.getVmDynamic().getId();
                    vmsToQuery.add(vmId.toString());
                }
            }
        }
        // Fetching for details from the host
        // and marking the VMs for addition
        if (!vmsToQuery.isEmpty()) {
            // Query VDSM for VMs info, and creating a proper VMStatic to be used when importing them
            Map[] vmsInfo = getVmInfo(vmsToQuery);
            for (Map vmInfo : vmsInfo) {
                Guid vmId = Guid.createGuidFromString((String) vmInfo.get(VdsProperties.vm_guid));
                VmStatic vmStatic = new VmStatic();
                vmStatic.setId(vmId);
                vmStatic.setCreationDate(new Date());
                vmStatic.setVdsGroupId(vds.getVdsGroupId());
                String vmNameOnHost = (String) vmInfo.get(VdsProperties.vm_name);

                if (StringUtils.equals(HOSTED_ENGINE_VM_NAME, vmNameOnHost)) {
                    vmStatic.setName(vmNameOnHost);
                    vmStatic.setOrigin(OriginType.HOSTED_ENGINE);
                    vmStatic.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
                } else {
                    vmStatic.setName(String.format(EXTERNAL_VM_NAME_FORMAT, vmNameOnHost));
                    vmStatic.setOrigin(OriginType.EXTERNAL);
                }

                vmStatic.setNumOfSockets(parseIntVdsProperty(vmInfo.get(VdsProperties.num_of_cpus)));
                vmStatic.setMemSizeMb(parseIntVdsProperty(vmInfo.get(VdsProperties.mem_size_mb)));
                vmStatic.setSingleQxlPci(false);

                externalVmsToAdd.add(vmStatic);
                log.info("Importing VM '{}' as '{}', as it is running on the on Host, but does not exist in the engine.", vmNameOnHost, vmStatic.getName());
            }
        }
    }

    private void updateRepository(List<Guid> staleRunningVms) {
        for (VmInternalData vmInternalData : runningVms.values()) {
            VmDynamic runningVm = vmInternalData.getVmDynamic();
            VM vmToUpdate = vmDict.get(runningVm.getId());

            // if not migrating here and not down
            if (!inMigrationTo(runningVm, vmToUpdate) && runningVm.getStatus() != VMStatus.Down) {
                if (vmToUpdate != null) {
                    if (vmDict.containsKey(vmToUpdate.getId())
                            && !StringUtils.equals(runningVm.getClientIp(), vmToUpdate.getClientIp())) {
                        vmsClientIpChanged.put(vmToUpdate, runningVm);
                    }
                }
                if (vmToUpdate != null) {
                    logVmStatusTransition(vmToUpdate, runningVm);

                    if ((vmToUpdate.getStatus() != VMStatus.Up && vmToUpdate.getStatus() != VMStatus.PoweringUp && runningVm.getStatus() == VMStatus.Up)
                            || (vmToUpdate.getStatus() != VMStatus.PoweringUp && runningVm.getStatus() == VMStatus.PoweringUp)) {
                        poweringUpVms.add(runningVm);
                    }

                    // Generate an event for those machines that transition from "PoweringDown" to
                    // "Up" as this means that the power down operation failed:
                    if (vmToUpdate.getStatus() == VMStatus.PoweringDown && runningVm.getStatus() == VMStatus.Up) {
                        AuditLogableBase logable = new AuditLogableBase(vds.getId(), vmToUpdate.getId());
                        auditLog(logable, AuditLogType.VM_POWER_DOWN_FAILED);
                    }

                    if (vmToUpdate.getStatus() != VMStatus.Up && vmToUpdate.getStatus() != VMStatus.MigratingFrom
                            && runningVm.getStatus() == VMStatus.Up) {
                        // Vm moved to Up status - remove its record from Async
                        // reportedAndUnchangedVms handling
                        if (log.isDebugEnabled()) {
                            log.debug("removing VM '{}' from successful run VMs list", vmToUpdate.getId());
                        }
                        if (!succededToRunVms.contains(vmToUpdate.getId())) {
                            succededToRunVms.add(vmToUpdate.getId());
                        }
                    }
                    afterMigrationFrom(runningVm, vmToUpdate);

                    if (vmToUpdate.getStatus() != VMStatus.NotResponding
                            && runningVm.getStatus() == VMStatus.NotResponding) {
                        AuditLogableBase logable = new AuditLogableBase(vds.getId(), vmToUpdate.getId());
                        auditLog(logable, AuditLogType.VM_NOT_RESPONDING);
                    }
                    // check if vm is suspended and remove it from async list
                    else if (runningVm.getStatus() == VMStatus.Paused) {
                        vmsToRemoveFromAsync.add(vmToUpdate.getId());
                        if (vmToUpdate.getStatus() != VMStatus.Paused) {
                            // check exit message to determine why the VM is paused
                            AuditLogType logType = vmPauseStatusToAuditLogType(runningVm.getPauseStatus());
                            if (logType != AuditLogType.UNASSIGNED) {
                                AuditLogableBase logable = new AuditLogableBase(vds.getId(), vmToUpdate.getId());
                                auditLog(logable, logType);
                            }
                        }

                    }
                }
                if (vmToUpdate != null || runningVm.getStatus() != VMStatus.MigratingFrom) {
                    RefObject<VM> tempRefObj = new RefObject<VM>(vmToUpdate);
                    boolean updateSucceed = updateVmRunTimeInfo(tempRefObj, runningVm);
                    vmToUpdate = tempRefObj.argvalue;
                    if (updateSucceed) {
                        addVmDynamicToList(vmToUpdate.getDynamicData());
                    }
                }
                if (vmToUpdate != null) {
                    updateVmStatistics(vmToUpdate);
                    if (vmDict.containsKey(runningVm.getId())) {
                        staleRunningVms.add(runningVm.getId());
                        if (!vdsManager.isInitialized()) {
                            ResourceManager.getInstance().RemoveVmFromDownVms(vds.getId(), runningVm.getId());
                        }
                    }
                }
            } else {
                if (runningVm.getStatus() == VMStatus.MigratingTo) {
                    staleRunningVms.add(runningVm.getId());
                }

                VmDynamic vmDynamic = getDbFacade().getVmDynamicDao().get(runningVm.getId());
                if (vmDynamic == null || vmDynamic.getStatus() != VMStatus.Unknown) {
                    vmDynamicToSave.remove(runningVm.getId());
                }
            }
        }
        // compare between vm in cache and vm from vdsm
        removeVmsFromCache(staleRunningVms);
    }

    private boolean updateVmRunTimeInfo(RefObject<VM> vmToUpdate, VmDynamic vmNewDynamicData) {
        boolean returnValue = false;
        if (vmToUpdate.argvalue == null) {
            vmToUpdate.argvalue = getDbFacade().getVmDao().get(vmNewDynamicData.getId());
            // if vm exists in db update info
            if (vmToUpdate.argvalue != null) {
                // TODO: This is done to keep consistency with VmDAO.getById(Guid).
                // It should probably be removed, but some research is required.
                vmToUpdate.argvalue.setInterfaces(getDbFacade()
                        .getVmNetworkInterfaceDao()
                        .getAllForVm(vmToUpdate.argvalue.getId()));

                vmDict.put(vmToUpdate.argvalue.getId(), vmToUpdate.argvalue);
                if (vmNewDynamicData.getStatus() == VMStatus.Up) {
                    if (!succededToRunVms.contains(vmToUpdate.argvalue.getId())) {
                        succededToRunVms.add(vmToUpdate.argvalue.getId());
                    }
                }
            }
        }
        if (vmToUpdate.argvalue != null) {
            // check if dynamic data changed - update cache and DB
            List<String> props = ObjectIdentityChecker.GetChangedFields(
                    vmToUpdate.argvalue.getDynamicData(), vmNewDynamicData);
            // remove all fields that should not be checked:
            props.removeAll(UNCHANGEABLE_FIELDS_BY_VDSM);

            if (vmNewDynamicData.getStatus() != VMStatus.Up) {
                props.remove(VmDynamic.APPLICATIONS_LIST_FIELD_NAME);
                vmNewDynamicData.setAppList(vmToUpdate.argvalue.getAppList());
            } else if (props.contains(VmDynamic.STATUS_FIELD_NAME)
                    && vmToUpdate.argvalue.getDynamicData().getStatus() == VMStatus.PreparingForHibernate) {
                vmNewDynamicData.setStatus(VMStatus.PreparingForHibernate);
                props.remove(VmDynamic.STATUS_FIELD_NAME);
            }
            // if anything else changed
            if (!props.isEmpty()) {
                vmToUpdate.argvalue.updateRunTimeDynamicData(vmNewDynamicData, vds.getId(), vds.getName());
                returnValue = true;
            }
        } else {
            // This should only happened when someone run a VM from command
            // line.
            if (Config.<Boolean> getValue(ConfigValues.DebugTimerLogging)) {
                log.info("VDS::UpdateVmRunTimeInfo Error: found VM on a VDS that is not in the database!");
            }
        }

        return returnValue;
    }

    private void updateVmStatistics(VM vmToUpdate) {
        // check if time for vm statistics refresh - update cache and DB
        if (vdsManager.getRefreshStatistics()) {
            VmStatistics vmStatistics = runningVms.get(vmToUpdate.getId()).getVmStatistics();
            vmToUpdate.updateRunTimeStatisticsData(vmStatistics, vmToUpdate);
            updateVmNumaNodeRuntimeInfo(vmStatistics, vmToUpdate);
            addVmStatisticsToList(vmToUpdate.getStatisticsData());
            updateInterfaceStatistics(vmToUpdate, vmStatistics);

            for (DiskImageDynamic imageDynamic : runningVms.get(vmToUpdate.getId()).getVmDynamic().getDisks()) {
                Disk disk = getDbFacade().getDiskDao().get(imageDynamic.getId());
                // We have disk_id statistics, which is good, but disk_image_dynamic table contains image_id, so we
                // update for the AI.
                // We also check if the disk is null, as, for external VMs the disk is not in the database
                if (disk != null && disk.getDiskStorageType() == Disk.DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    Guid activeImageId = diskImage.getImageId();
                    imageDynamic.setId(activeImageId);
                    vmDiskImageDynamicToSave.put(activeImageId, imageDynamic);
                }
            }
        }
    }

    private void updateInterfaceStatistics(VM vm, VmStatistics statistics) {
        if (statistics.getInterfaceStatistics() == null) {
            return;
        }

        if (vm.getInterfaces() == null || vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        List<String> macs = new ArrayList<String>();

        vm.setUsageNetworkPercent(0);

        for (VmNetworkInterface ifStats : statistics.getInterfaceStatistics()) {
            boolean firstTime = !macs.contains(ifStats.getMacAddress());

            VmNetworkInterface vmIface = null;
            for (VmNetworkInterface tempIf : vm.getInterfaces()) {
                if (tempIf.getMacAddress().equals(ifStats.getMacAddress())) {
                    vmIface = tempIf;
                    break;
                }
            }
            if (vmIface == null) {
                continue;
            }

            // RX rate and TX rate are reported by VDSM in % (minimum value
            // 0, maximum value 100)
            // Rx drop and TX drop are reported in packet numbers

            // if rtl+pv it will get here 2 times (we take the max one)
            if (firstTime) {

                vmIface.getStatistics().setReceiveRate(ifStats.getStatistics().getReceiveRate());
                vmIface.getStatistics().setReceiveDropRate(ifStats.getStatistics().getReceiveDropRate());
                vmIface.getStatistics().setTransmitRate(ifStats.getStatistics().getTransmitRate());
                vmIface.getStatistics().setTransmitDropRate(ifStats.getStatistics().getTransmitDropRate());
            } else {
                vmIface.getStatistics().setReceiveRate(Math.max(vmIface.getStatistics().getReceiveRate(),
                        ifStats.getStatistics().getReceiveRate()));
                vmIface.getStatistics().setReceiveDropRate(Math.max(vmIface.getStatistics().getReceiveDropRate(),
                        ifStats.getStatistics().getReceiveDropRate()));
                vmIface.getStatistics().setTransmitRate(Math.max(vmIface.getStatistics().getTransmitRate(),
                        ifStats.getStatistics().getTransmitRate()));
                vmIface.getStatistics().setTransmitDropRate(Math.max(vmIface.getStatistics().getTransmitDropRate(),
                        ifStats.getStatistics().getTransmitDropRate()));
            }
            vmIface.setVmId(vm.getId());

            if (ifStats.getSpeed() != null && vmIface.getStatistics().getReceiveRate() != null
                    && vmIface.getStatistics().getReceiveRate() > 0) {

                double rx_percent = vmIface.getStatistics().getReceiveRate();
                double tx_percent = vmIface.getStatistics().getTransmitRate();

                vm.setUsageNetworkPercent(Math.max(vm.getUsageNetworkPercent(),
                        (int) Math.max(rx_percent, tx_percent)));
            }

            if (firstTime) {
                macs.add(ifStats.getMacAddress());
            }
        }

        Integer maxPercent = 100;
        vm.setUsageNetworkPercent((vm.getUsageNetworkPercent() > maxPercent) ? maxPercent : vm.getUsageNetworkPercent());
        addVmInterfaceStatisticsToList(vm.getInterfaces());
    }

    /**
     * Handle changes in all VM devices
     */
    private void handleVmDeviceChange() {
        // Go over all the vms and determine which ones require updating
        // Update only running VMs
        List<String> vmsToUpdateFromVds = new ArrayList<String>();
        for (VmInternalData vmInternalData : runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            if (vmDynamic != null && vmDynamic.getStatus() != VMStatus.MigratingTo) {
                VM vm = vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    String dbHash = vm.getHash();
                    if ((dbHash == null && vmDynamic.getHash() != null) || (dbHash != null)
                            && !dbHash.equals(vmDynamic.getHash())) {
                        vmsToUpdateFromVds.add(vmDynamic.getId().toString());
                        // update new hash value
                        if (vmDynamicToSave.containsKey(vm.getId())) {
                            vmDynamicToSave.get(vm.getId()).setHash(vmDynamic.getHash());
                        } else {
                            addVmDynamicToList(vmDynamic);
                        }
                    }
                }
            }
        }

        if (!vmsToUpdateFromVds.isEmpty()) {
            // If there are vms that require updating,
            // get the new info from VDSM in one call, and then update them all
            updateVmDevices(vmsToUpdateFromVds);
        }
    }

    /**
     * Prepare the VM Guest Agent network devices for update. <br>
     * The evaluation of the network devices for update is done by comparing the calculated hash of the network devices
     * from VDSM to the latest hash kept on engine side.
     */
    private void prepareGuestAgentNetworkDevicesForUpdate() {
        for (VmInternalData vmInternalData : runningVms.values()) {
            VmDynamic vmDynamic = vmInternalData.getVmDynamic();
            if (vmDynamic != null) {
                VM vm = vmDict.get(vmDynamic.getId());
                if (vm != null) {
                    List<VmGuestAgentInterface> vmGuestAgentInterfaces = vmInternalData.getVmGuestAgentInterfaces();
                    int guestAgentNicHash = vmGuestAgentInterfaces == null ? 0 : vmGuestAgentInterfaces.hashCode();
                    if (guestAgentNicHash != vmDynamic.getGuestAgentNicsHash()) {
                        vmGuestAgentNics.put(vmDynamic.getId(), vmGuestAgentInterfaces);

                        // update new hash value
                        if (vmDynamicToSave.containsKey(vm.getId())) {
                            updateGuestAgentInterfacesChanges(vmDynamicToSave.get(vm.getId()),
                                    vmGuestAgentInterfaces,
                                    guestAgentNicHash);
                        } else {
                            updateGuestAgentInterfacesChanges(vmDynamic, vmGuestAgentInterfaces, guestAgentNicHash);
                            addVmDynamicToList(vmDynamic);
                        }
                    }
                }
            }
        }
    }

    protected void updateLunDisks() {
        // Looping only over powering up VMs as LUN device size
        // is updated by VDSM only once when running a VM.
        for (VmDynamic vmDynamic : getPoweringUpVms()) {
            VmInternalData vmInternalData = getRunningVms().get(vmDynamic.getId());
            if (vmInternalData != null) {
                Map<String, LUNs> lunsMap = vmInternalData.getLunsMap();
                if (lunsMap.isEmpty()) {
                    // LUNs list from getVmStats hasn't been updated yet or VDSM doesn't support LUNs list retrieval.
                    continue;
                }

                List<Disk> vmDisks = getDbFacade().getDiskDao().getAllForVm(vmDynamic.getId(), true);
                for (Disk disk : vmDisks) {
                    if (disk.getDiskStorageType() != Disk.DiskStorageType.LUN) {
                        continue;
                    }

                    LUNs lunFromDB = ((LunDisk) disk).getLun();
                    LUNs lunFromMap = lunsMap.get(lunFromDB.getId());

                    // LUN's device size might be returned as zero in case of an error in VDSM;
                    // Hence, verify before updating.
                    if (lunFromMap.getDeviceSize() != 0 && lunFromMap.getDeviceSize() != lunFromDB.getDeviceSize()) {
                        // Found a mismatch - set LUN for update
                        log.info("Updated LUN device size - ID: {}, previous size: {}, new size: {}.",
                                lunFromDB.getLUN_id(), lunFromDB.getDeviceSize(), lunFromMap.getDeviceSize());

                        lunFromDB.setDeviceSize(lunFromMap.getDeviceSize());
                        vmLunDisksToSave.add(lunFromDB);
                    }
                }
            }
        }
    }

    protected void updateVmJobs() {
        // The database vmJob records are synced with the vmJobs returned from each VM.
        vmJobIdsToRemove.clear();
        vmJobsToUpdate.clear();

        for (Map.Entry<Guid, VmInternalData> vmInternalData : runningVms.entrySet()) {
            Set<Guid> vmJobIdsToIgnore = new HashSet<>();
            Map<Guid, VmJob> jobsFromDb = new HashMap<>();
            for (VmJob job : getDbFacade().getVmJobDao().getAllForVm(vmInternalData.getKey())) {
                // Only jobs that were in the DB before our update may be updated/removed;
                // others are completely ignored for the time being
                if (existingVmJobIds.contains(job.getId())) {
                    jobsFromDb.put(job.getId(), job);
                }
            }

            if (vmInternalData.getValue().getVmStatistics().getVmJobs() == null) {
                // If no vmJobs key was returned, we can't presume anything about the jobs; save them all
                log.debug("No vmJob data returned from VDSM, preserving existing jobs");
                continue;
            }

            for (VmJob jobFromVds : vmInternalData.getValue().getVmStatistics().getVmJobs()) {
                if (jobsFromDb.containsKey(jobFromVds.getId())) {
                    if (jobsFromDb.get(jobFromVds.getId()).equals(jobFromVds)) {
                        // Same data, no update needed.  It would be nice if a caching
                        // layer would take care of this for us.
                        vmJobIdsToIgnore.add(jobFromVds.getId());
                        log.info("VM job {}: In progress (no change)", jobFromVds.getId());
                    } else {
                        vmJobsToUpdate.put(jobFromVds.getId(), jobFromVds);
                        log.info("VM job {}: In progress, updating", jobFromVds.getId());
                    }
                }
            }

            // Any existing jobs not saved need to be removed
            for (Guid id : jobsFromDb.keySet()) {
                if (!vmJobsToUpdate.containsKey(id) && !vmJobIdsToIgnore.contains(id)) {
                    vmJobIdsToRemove.add(id);
                    log.info("VM job {}: Deleting", id);
                }
            }
        }
    }

    private void saveVmsToDb() {
        getDbFacade().getVmDynamicDao().updateAllInBatch(vmDynamicToSave.values());
        getDbFacade().getVmStatisticsDao().updateAllInBatch(vmStatisticsToSave.values());

        final List<VmNetworkStatistics> allVmInterfaceStatistics = new LinkedList<VmNetworkStatistics>();
        for (List<VmNetworkInterface> list : vmInterfaceStatisticsToSave.values()) {
            for (VmNetworkInterface iface : list) {
                allVmInterfaceStatistics.add(iface.getStatistics());
            }
        }

        getDbFacade().getVmNetworkStatisticsDao().updateAllInBatch(allVmInterfaceStatistics);

        getDbFacade().getDiskImageDynamicDao().updateAllInBatch(vmDiskImageDynamicToSave.values());
        getDbFacade().getLunDao().updateAllInBatch(vmLunDisksToSave);
        getVdsEventListener().addExternallyManagedVms(externalVmsToAdd);
        saveVmDevicesToDb();
        saveVmJobsToDb();
        saveVmGuestAgentNetworkDevices();
        saveVmNumaNodeRuntimeData();
    }

    private void afterVMsRefreshTreatment() {

        // rerun all vms from rerun list
        for (Guid vm_guid : vmsToRerun) {
            log.error("Rerun VM '{}'. Called from VDS '{}'", vm_guid, vds.getName());
            ResourceManager.getInstance().RerunFailedCommand(vm_guid, vds.getId());

        }

        for (Guid vm_guid : succededToRunVms) {
            vdsManager.succededToRunVm(vm_guid);
        }

        getVdsEventListener().updateSlaPolicies(succededToRunVms, vds.getId());

        // Refrain from auto-start HA VM during its re-run attempts.
        autoVmsToRun.removeAll(vmsToRerun);
        // run all vms that crushed that marked with auto startup
        getVdsEventListener().runFailedAutoStartVMs(autoVmsToRun);

        // process all vms that their ip changed.
        for (Map.Entry<VM, VmDynamic> pair : vmsClientIpChanged.entrySet()) {
            getVdsEventListener().processOnClientIpChange(vds, pair.getValue().getId());
        }

        // process all vms that powering up.
        for (VmDynamic runningVm : poweringUpVms) {
            getVdsEventListener().processOnVmPoweringUp(runningVm.getId());
        }

        // process all vms that went down
        getVdsEventListener().processOnVmStop(vmsMovedToDown);

        for (Guid vm_guid : vmsToRemoveFromAsync) {
            ResourceManager.getInstance().RemoveAsyncRunningVm(vm_guid);
        }
    }

    // ***** DB interaction *****

    private void saveVmGuestAgentNetworkDevices() {
        if (!vmGuestAgentNics.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            for (Guid vmId : vmGuestAgentNics.keySet()) {
                                getDbFacade().getVmGuestAgentInterfaceDao().removeAllForVm(vmId);
                            }

                            for (List<VmGuestAgentInterface> nics : vmGuestAgentNics.values()) {
                                if (nics != null) {
                                    for (VmGuestAgentInterface nic : nics) {
                                        getDbFacade().getVmGuestAgentInterfaceDao().save(nic);
                                    }
                                }
                            }
                            return null;
                        }
                    }
            );
        }
    }

    private void saveVmDevicesToDb() {
        getDbFacade().getVmDeviceDao().updateAllInBatch(vmDeviceToSave.values());

        if (!removedDeviceIds.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().removeAll(removedDeviceIds);
                            return null;
                        }
                    });
        }

        if (!newVmDevices.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().saveAll(newVmDevices);
                            return null;
                        }
                    });
        }
    }

    private void saveVmJobsToDb() {
        getDbFacade().getVmJobDao().updateAllInBatch(vmJobsToUpdate.values());

        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmJobDao().removeAll(vmJobIdsToRemove);
                            return null;
                        }
                    });
        }
    }

    private void saveVmNumaNodeRuntimeData() {
        if (!vmStatisticsToSave.isEmpty()) {
            final List<VmNumaNode> vmNumaNodesToUpdate = new ArrayList<>();
            for(VmStatistics vmStats : vmStatisticsToSave.values()) {
                vmNumaNodesToUpdate.addAll(vmStats.getvNumaNodeStatisticsList());
            }
            if (!vmNumaNodesToUpdate.isEmpty()) {
                getDbFacade().getVmNumaNodeDAO().massUpdateVmNumaNodeRuntimePinning(vmNumaNodesToUpdate);
            }
        }
    }

    private void refreshExistingVmJobList() {
        existingVmJobIds.clear();
        existingVmJobIds.addAll(getDbFacade().getVmJobDao().getAllIds());
    }

    // ***** Helpers and sub-methods *****

    private boolean isBalloonDeviceActiveOnVm(VmInternalData vmInternalData) {
        VM savedVm = vmDict.get(vmInternalData.getVmDynamic().getId());

        if (savedVm != null) {
            VmBalloonInfo balloonInfo = vmInternalData.getVmStatistics().getVmBalloonInfo();
            return savedVm.getMinAllocatedMem() < savedVm.getMemSizeMb() // minimum allocated mem of VM == total mem, ballooning is impossible
                    && balloonInfo.isBalloonDeviceEnabled()
                    && balloonInfo.getBalloonTargetMemory().intValue() != balloonInfo.getBalloonMaxMemory().intValue(); // ballooning was not requested/enabled on this VM
        }
        return false;
    }

    // remove the vm from the list of vms with uncontrolled inflated balloon
    private void guestAgentIsUpOrBalloonDeflated(Guid vmId) {
        vmsWithUncontrolledBalloon.remove(vmId);
    }

    // add the vm to the list of vms with uncontrolled inflated balloon or increment its counter
    // if it is already in the list
    private void guestAgentIsDownAndBalloonInfalted(Guid vmId) {
        Integer currentVal = vmsWithUncontrolledBalloon.get(vmId);
        if (currentVal == null) {
            vmsWithUncontrolledBalloon.put(vmId, 1);
        } else {
            vmsWithUncontrolledBalloon.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmId);
                AuditLogDirector.log(auditLogable, AuditLogType.VM_BALLOON_DRIVER_UNCONTROLLED);
                vmsWithUncontrolledBalloon.put(vmId, 0);
            }
        }
    }

    private void updateGuestAgentInterfacesChanges(VmDynamic vmDynamic,
            List<VmGuestAgentInterface> vmGuestAgentInterfaces,
            int guestAgentNicHash) {
        vmDynamic.setGuestAgentNicsHash(guestAgentNicHash);
        vmDynamic.setVmIp(extractVmIpsFromGuestAgentInterfaces(vmGuestAgentInterfaces));
    }

    private String extractVmIpsFromGuestAgentInterfaces(List<VmGuestAgentInterface> nics) {
        if (nics == null || nics.isEmpty()) {
            return null;
        }

        List<String> ips = new ArrayList<String>();
        for (VmGuestAgentInterface nic : nics) {
            if (nic.getIpv4Addresses() != null) {
                ips.addAll(nic.getIpv4Addresses());
            }
        }
        return ips.isEmpty() ? null : StringUtils.join(ips, " ");
    }

    private void handleVmOnDown(VM cacheVm, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        VmExitStatus exitStatus = vmDynamic.getExitStatus();

        // we don't need to have an audit log for the case where the VM went down on a host
        // which is different than the one it should be running on (must be in migration process)
        if (cacheVm != null) {
            auditVmOnDownEvent(exitStatus, vmDynamic.getExitMessage(), vmStatistics.getId());
        }

        if (exitStatus != VmExitStatus.Normal) {
            // Vm failed to run - try to rerun it on other Vds
            if (cacheVm != null) {
                if (ResourceManager.getInstance().IsVmInAsyncRunningList(vmDynamic.getId())) {
                    log.info("Running on vds during rerun failed vm: '{}'", vmDynamic.getRunOnVds());
                    vmsToRerun.add(vmDynamic.getId());
                } else if (cacheVm.isAutoStartup()) {
                    autoVmsToRun.add(vmDynamic.getId());
                }
            }
            // if failed in destination right after migration
            else { // => cacheVm == null
                ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
                addVmDynamicToList(vmDynamic);
            }
        } else {
            // Vm moved safely to down status. May be migration - just remove it from Async Running command.
            ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
        }
    }

    /**
     * Generate an error or information event according to the exit status of a VM in status 'down'
     */
    private void auditVmOnDownEvent(VmExitStatus exitStatus, String exitMessage, Guid vmStatisticsId) {
        AuditLogType type = exitStatus == VmExitStatus.Normal ? AuditLogType.VM_DOWN : AuditLogType.VM_DOWN_ERROR;
        AuditLogableBase logable = new AuditLogableBase(vds.getId(), vmStatisticsId);
        if (exitMessage != null) {
            logable.addCustomValue("ExitMessage", "Exit message: " + exitMessage);
        }
        auditLog(logable, type);
    }

    private void afterSuspendTreatment(VmDynamic vm) {
        AuditLogType type = vm.getExitStatus() == VmExitStatus.Normal ? AuditLogType.USER_SUSPEND_VM_OK
                : AuditLogType.USER_FAILED_SUSPEND_VM;

        AuditLogableBase logable = new AuditLogableBase(vds.getId(), vm.getId());
        auditLog(logable, type);
        ResourceManager.getInstance().RemoveAsyncRunningVm(vm.getId());
    }

    private void clearVm(VM vm, VmExitStatus exitStatus, String exitMessage, VmExitReason exitReason) {
        if (vm.getStatus() != VMStatus.MigratingFrom) {
            // we must check that vm.getStatus() != VMStatus.Down because if it was set to down
            // the exit status and message were set, and we don't want to override them here.
            // we will add it to vmDynamicToSave though because it might been removed from it in #updateRepository
            if (vm.getStatus() != VMStatus.Suspended && vm.getStatus() != VMStatus.Down) {
                ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.Down, exitStatus, exitMessage, exitReason);
            }
            addVmDynamicToList(vm.getDynamicData());
            addVmStatisticsToList(vm.getStatisticsData());
            addVmInterfaceStatisticsToList(vm.getInterfaces());
            if (!ResourceManager.getInstance().IsVmInAsyncRunningList(vm.getId())) {
                vmsMovedToDown.add(vm.getId());
            }
        }
    }
    private void proceedVmBeforeDeletion(VM curVm, VmDynamic vmDynamic) {
        AuditLogType type = AuditLogType.UNASSIGNED;
        AuditLogableBase logable = new AuditLogableBase(vds.getId(), curVm.getId());
        switch (curVm.getStatus()) {
            case MigratingFrom: {
                // if a VM that was a source host in migration process is now down with normal
                // exit status that's OK, otherwise..
                if (vmDynamic != null && vmDynamic.getExitStatus() != VmExitStatus.Normal) {
                    if (curVm.getMigratingToVds() != null) {
                        vmsToDestroy.add(new Pair<VM, Guid>(curVm, curVm.getMigratingToVds()));
                    }
                    // set vm status to down if source vm crushed
                    ResourceManager.getInstance().InternalSetVmStatus(curVm,
                            VMStatus.Down,
                            vmDynamic.getExitStatus(),
                            vmDynamic.getExitMessage(),
                            vmDynamic.getExitReason());
                    addVmDynamicToList(curVm.getDynamicData());
                    addVmStatisticsToList(curVm.getStatisticsData());
                    addVmInterfaceStatisticsToList(curVm.getInterfaces());
                    type = AuditLogType.VM_MIGRATION_ABORT;
                    logable.addCustomValue("MigrationError", vmDynamic.getExitMessage());

                    ResourceManager.getInstance().RemoveAsyncRunningVm(vmDynamic.getId());
                }
                break;
            }
            default:
                break;
        }
        if (type != AuditLogType.UNASSIGNED) {
            auditLog(logable, type);
        }
    }


    /**
     * Update the given list of VMs properties in DB
     *
     * @param vmsToUpdate
     */
    protected void updateVmDevices(List<String> vmsToUpdate) {
        Map[] vms = getVmInfo(vmsToUpdate);
        if (vms != null) {
            for (Map vm : vms) {
                processVmDevices(vm);
            }
        }
    }

    /**
     * Actually process the VM device update in DB.
     *
     * @param vm
     */
    private void processVmDevices(Map vm) {
        if (vm == null || vm.get(VdsProperties.vm_guid) == null) {
            log.error("Received NULL VM or VM id when processing VM devices, abort.");
            return;
        }

        Guid vmId = new Guid((String) vm.get(VdsProperties.vm_guid));
        Set<Guid> processedDevices = new HashSet<Guid>();
        List<VmDevice> devices = getDbFacade().getVmDeviceDao().getVmDeviceByVmId(vmId);
        Map<VmDeviceId, VmDevice> deviceMap = Entities.businessEntitiesById(devices);

        for (Object o : (Object[]) vm.get(VdsProperties.Devices)) {
            Map device = (Map<String, Object>) o;
            if (device.get(VdsProperties.Address) == null) {
                logDeviceInformation(vmId, device);
                continue;
            }

            Guid deviceId = getDeviceId(device);
            VmDevice vmDevice = deviceMap.get(new VmDeviceId(deviceId, vmId));
            String logicalName = null;
            if (deviceId != null && FeatureSupported.reportedDisksLogicalNames(vds.getVdsGroupCompatibilityVersion()) &&
                    VmDeviceType.DISK.getName().equals(device.get(VdsProperties.Device))) {
                try {
                    logicalName = getDeviceLogicalName((Map<?, ?>) vm.get(VdsProperties.GuestDiskMapping), deviceId);
                } catch (Exception e) {
                    log.error("error while getting device name when processing, vm '{}', device info '{}' with exception, skipping '{}'",
                            vmId, device, e.getMessage());
                    log.error("Exception", e);
                }
            }

            if (deviceId == null || vmDevice == null) {
                deviceId = addNewVmDevice(vmId, device, logicalName);
            } else {
                vmDevice.setAddress(((Map<String, String>) device.get(VdsProperties.Address)).toString());
                vmDevice.setAlias(StringUtils.defaultString((String) device.get(VdsProperties.Alias)));
                vmDevice.setLogicalName(logicalName);
                addVmDeviceToList(vmDevice);
            }

            processedDevices.add(deviceId);
        }

        handleRemovedDevices(vmId, processedDevices, devices);
    }

    private String getDeviceLogicalName(Map<?, ?> diskMapping, Guid deviceId) {
        if (diskMapping == null) {
            return null;
        }

        Map<?, ?> deviceMapping = null;
        String modifiedDeviceId = deviceId.toString().substring(0, 20);
        for (Map.Entry<?, ?> entry : diskMapping.entrySet()) {
            String serial = (String) entry.getKey();
            if (serial != null && serial.contains(modifiedDeviceId)) {
                deviceMapping = (Map<?, ?>) entry.getValue();
                break;
            }
        }

        return deviceMapping == null ? null : (String) deviceMapping.get(VdsProperties.Name);
    }

    /**
     * Removes unmanaged devices from DB if were removed by libvirt. Empties device address with isPlugged = false
     *
     * @param vmId
     * @param processedDevices
     */
    private void handleRemovedDevices(Guid vmId, Set<Guid> processedDevices, List<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (processedDevices.contains(device.getDeviceId())) {
                continue;
            }

            if (device.getIsManaged()) {
                if (device.getIsPlugged()) {
                    device.setAddress("");
                    addVmDeviceToList(device);
                    log.debug("VM '{}' managed pluggable device was unplugged : '{}'", vmId, device);
                } else if (!devicePluggable(device)) {
                    log.error("VM '{}' managed non pluggable device was removed unexpectedly from libvirt: '{}'",
                            vmId, device);
                }
            } else {
                removedDeviceIds.add(device.getId());
                log.debug("VM '{}' unmanaged device was marked for remove : {1}", vmId, device);
            }
        }
    }

    private boolean devicePluggable(VmDevice device) {
        return VmDeviceCommonUtils.isDisk(device) || VmDeviceCommonUtils.isBridge(device);
    }

    /**
     * Adds new devices recognized by libvirt
     *
     * @param vmId
     * @param device
     */
    private Guid addNewVmDevice(Guid vmId, Map device, String logicalName) {
        Guid newDeviceId = Guid.Empty;
        String typeName = (String) device.get(VdsProperties.Type);
        String deviceName = (String) device.get(VdsProperties.Device);

        // do not allow null or empty device or type values
        if (StringUtils.isEmpty(typeName) || StringUtils.isEmpty(deviceName)) {
            log.error("Empty or NULL values were passed for a VM '{}' device, Device is skipped", vmId);
        } else {
            String address = ((Map<String, String>) device.get(VdsProperties.Address)).toString();
            String alias = StringUtils.defaultString((String) device.get(VdsProperties.Alias));
            Object o = device.get(VdsProperties.SpecParams);
            newDeviceId = Guid.newGuid();
            VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
            VmDevice newDevice = new VmDevice(id, VmDeviceGeneralType.forValue(typeName), deviceName, address,
                    0,
                    o == null ? new HashMap<String, Object>() : (Map<String, Object>) o,
                    false,
                    true,
                    Boolean.getBoolean((String) device.get(VdsProperties.ReadOnly)),
                    alias,
                    null,
                    null,
                    logicalName);
            newVmDevices.add(newDevice);
            log.debug("New device was marked for adding to VM '{}' Devices : '{}'", vmId, newDevice);
        }

        return newDeviceId;
    }

    /**
     * gets the device id from the structure returned by VDSM device ids are stored in specParams map
     *
     * @param device
     * @return
     */
    private static Guid getDeviceId(Map device) {
        String deviceId = (String) device.get(VdsProperties.DeviceId);
        return deviceId == null ? null : new Guid(deviceId);
    }

    // Some properties were changed recently from String to Integer
    // This method checks what type is the property, and returns int
    private int parseIntVdsProperty(Object vdsProperty) {
        if (vdsProperty instanceof Integer) {
            return (Integer) vdsProperty;
        } else {
            return Integer.parseInt((String) vdsProperty);
        }
    }

    private AuditLogType vmPauseStatusToAuditLogType(VmPauseStatus pauseStatus) {
        switch (pauseStatus) {
            case NOERR:
            case NONE:
                // user requested pause, no log needed
                return AuditLogType.UNASSIGNED;
            case ENOSPC:
                return AuditLogType.VM_PAUSED_ENOSPC;
            case EIO:
                return AuditLogType.VM_PAUSED_EIO;
            case EPERM:
                return AuditLogType.VM_PAUSED_EPERM;
            default:
                return AuditLogType.VM_PAUSED_ERROR;
        }
    }

    private static void logVmStatusTransition(VM vmToUpdate, VmDynamic runningVm) {
        if (vmToUpdate.getStatus() != runningVm.getStatus()) {
            log.info("VM {} {} moved from {} --> {}",
                    vmToUpdate.getName(),
                    vmToUpdate.getId(),
                    vmToUpdate.getStatus().name(),
                    runningVm.getStatus().name());

            if (vmToUpdate.getStatus() == VMStatus.Unknown) {
                logVmStatusTransionFromUnknown(vmToUpdate, runningVm);
            }
        }
    }

    // del from cache all vms that not in vdsm
    private void removeVmsFromCache(List<Guid> staleRunningVms) {
        Guid vmGuid;
        for (VM vmToRemove : vmDict.values()) {
            if (staleRunningVms.contains(vmToRemove.getId())) {
                continue;
            }
            proceedVmBeforeDeletion(vmToRemove, null);
            boolean migrating = vmToRemove.getStatus() == VMStatus.MigratingFrom;
            if (migrating) {
                handOverVM(vmToRemove);
            } else {
                clearVm(vmToRemove,
                        VmExitStatus.Error,
                        String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                                vmToRemove.getName()),
                        VmExitReason.GenericError);
            }

            log.info("VM '{}'({}) is running in db and not running in VDS '{}'",
                    vmToRemove.getName(), vmToRemove.getId(), vds.getName());

            vmGuid = vmToRemove.getId();
            if (!migrating && !vmsToRerun.contains(vmGuid)
                    && ResourceManager.getInstance().IsVmInAsyncRunningList(vmGuid)) {
                vmsToRerun.add(vmGuid);
                log.info("add VM '{}' to rerun treatment", vmToRemove.getName());
            }
            // vm should be auto startup
            // not already in start up list
            // not in reported from vdsm at all
            // or reported from vdsm with error code
            else if (vmToRemove.isAutoStartup()
                    && !autoVmsToRun.contains(vmGuid)
                    && (!runningVms.containsKey(vmGuid)
                        || runningVms.get(vmGuid).getVmDynamic().getExitStatus() != VmExitStatus.Normal)) {
                autoVmsToRun.add(vmGuid);
                log.info("add VM '{}' to HA rerun treatment", vmToRemove.getName());
            }
        }
    }

    private void handOverVM(VM vmToRemove) {
        Guid destinationHostId = vmToRemove.getMigratingToVds();

        // when the destination VDS is NonResponsive put the VM to Uknown like the rest of its VMs, else MigratingTo
        VMStatus newVmStatus =
                (VDSStatus.NonResponsive == getDbFacade().getVdsDao().get(destinationHostId).getStatus())
                        ? VMStatus.Unknown
                        : VMStatus.MigratingTo;

        // handing over the VM to the DST by marking it running on it. it will now be its SRC host.
        vmToRemove.setRunOnVds(destinationHostId);

        log.info("Handing over VM '{}'({}) to Host '{}'. Setting VM to status '{}'",
                vmToRemove.getName(),
                vmToRemove.getId(),
                destinationHostId,
                newVmStatus);

        // if the DST host goes unresponsive it will take care all MigratingTo and unknown VMs
        ResourceManager.getInstance().InternalSetVmStatus(vmToRemove, newVmStatus);

        // save the VM state
        addVmDynamicToList(vmToRemove.getDynamicData());
        addVmStatisticsToList(vmToRemove.getStatisticsData());
        addVmInterfaceStatisticsToList(vmToRemove.getInterfaces());
    }

    private boolean inMigrationTo(VmDynamic runningVm, VM vmToUpdate) {
        boolean returnValue = false;
        if (runningVm.getStatus() == VMStatus.MigratingTo) {
            // in migration
            log.info(
                    "RefreshVmList vm id '{}' is migrating to vds '{}' ignoring it in the refresh until migration is done",
                    runningVm.getId(),
                    vds.getName());
            returnValue = true;
        } else if (vmToUpdate == null && runningVm.getStatus() != VMStatus.MigratingFrom) {
            // check if the vm exists on another vds
            VmDynamic vmDynamic = getDbFacade().getVmDynamicDao().get(runningVm.getId());
            if (vmDynamic != null && vmDynamic.getRunOnVds() != null
                    && !vmDynamic.getRunOnVds().equals(vds.getId()) && runningVm.getStatus() != VMStatus.Up) {
                log.info(
                        "RefreshVmList vm id '{}' status = '{}' on vds '{}' ignoring it in the refresh until migration is done",
                        runningVm.getId(),
                        runningVm.getStatus(),
                        vds.getName());
                returnValue = true;
            }
        }
        return returnValue;
    }

    private void afterMigrationFrom(VmDynamic runningVm, VM vmToUpdate) {
        VMStatus oldVmStatus = vmToUpdate.getStatus();
        VMStatus currentVmStatus = runningVm.getStatus();

        // if the VM's status on source host was MigratingFrom and now the VM is running and its status
        // is not MigratingFrom, it means the migration failed
        if (oldVmStatus == VMStatus.MigratingFrom && currentVmStatus != VMStatus.MigratingFrom
                && currentVmStatus.isRunning()) {
            vmsToRerun.add(runningVm.getId());
            log.info("Adding VM '{}' to re-run list", runningVm.getId());
            vmToUpdate.setMigratingToVds(null);
            vmToUpdate.setMigrationProgressPercent(0);
            addVmStatisticsToList(vmToUpdate.getStatisticsData());
        }
    }

    // remove the vm from the list of vms with balloon driver problem
    private void vmBalloonDriverIsNotRequestedOrAvailable(Guid vmId) {
        vmsWithBalloonDriverProblem.remove(vmId);
    }

    // add the vm to the list of vms with balloon driver problem or increment its counter
    // if it is already in the list
    private void vmBalloonDriverIsRequestedAndUnavailable(Guid vmId) {
        Integer currentVal = vmsWithBalloonDriverProblem.get(vmId);
        if (currentVal == null) {
            vmsWithBalloonDriverProblem.put(vmId, 1);
        } else {
            vmsWithBalloonDriverProblem.put(vmId, currentVal + 1);
            if (currentVal >= Config.<Integer> getValue(ConfigValues.IterationsWithBalloonProblem)) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.setVmId(vmId);
                AuditLogDirector.log(auditLogable, AuditLogType.VM_BALLOON_DRIVER_ERROR);
                vmsWithBalloonDriverProblem.put(vmId, 0);
            }
        }
    }

    /**
     * gets VM full information for the given list of VMs
     *
     * @param vmsToUpdate
     * @return
     */
    protected Map[] getVmInfo(List<String> vmsToUpdate) {
        return (Map[]) (new FullListVdsCommand<FullListVDSCommandParameters>(
                new FullListVDSCommandParameters(vds, vmsToUpdate)).executeWithReturnValue());
    }

    private boolean shouldLogDeviceDetails(String deviceType) {
        return !StringUtils.equalsIgnoreCase(deviceType, VmDeviceType.FLOPPY.getName());
    }

    private void logDeviceInformation(Guid vmId, Map device) {
        String message = "Received a {} Device without an address when processing VM {} devices, skipping device";
        String deviceType = (String) device.get(VdsProperties.Device);

        if (shouldLogDeviceDetails(deviceType)) {
            Map<String, Object> deviceInfo = device;
            log.info(message + ": {}", StringUtils.defaultString(deviceType), vmId, deviceInfo);
        } else {
            log.info(message, StringUtils.defaultString(deviceType), vmId);
        }
    }
    protected static boolean isNewWatchdogEvent(VmDynamic vmDynamic, VM vmTo) {
        Long lastWatchdogEvent = vmDynamic.getLastWatchdogEvent();
        return vmTo != null && lastWatchdogEvent != null
                && (vmTo.getLastWatchdogEvent() == null || vmTo.getLastWatchdogEvent() < lastWatchdogEvent);
    }

    private void updateVmNumaNodeRuntimeInfo(VmStatistics statistics, VM vm) {
        if (!vm.getStatus().isRunning()) {
            vm.getStatisticsData().getvNumaNodeStatisticsList().clear();
            return;
        }

        //Build numa nodes map of the host which the vm is running on with node index as the key
        Map<Integer, VdsNumaNode> runOnVdsAllNumaNodesMap = new HashMap<>();
        List<VdsNumaNode> runOnVdsAllNumaNodes = getDbFacade().getVdsNumaNodeDAO().getAllVdsNumaNodeByVdsId(vm.getRunOnVds());
        for (VdsNumaNode vdsNumaNode : runOnVdsAllNumaNodes) {
            runOnVdsAllNumaNodesMap.put(vdsNumaNode.getIndex(), vdsNumaNode);
        }

        //Build numa nodes map of the vm with node index as the key
        Map<Integer, VmNumaNode> vmAllNumaNodesMap = new HashMap<>();
        List<VmNumaNode> vmAllNumaNodes = getDbFacade().getVmNumaNodeDAO().getAllVmNumaNodeByVmId(vm.getId());
        for (VmNumaNode vmNumaNode : vmAllNumaNodes) {
            vmAllNumaNodesMap.put(vmNumaNode.getIndex(), vmNumaNode);
        }

        //Initialize the unpinned vm numa nodes list with the runtime pinning information
        List<VmNumaNode> vmNumaNodesNeedUpdate = new ArrayList<>();
        for (VmNumaNode vNode : statistics.getvNumaNodeStatisticsList()) {
            VmNumaNode dbVmNumaNode = vmAllNumaNodesMap.get(vNode.getIndex());
            if (dbVmNumaNode != null) {
                vNode.setId(dbVmNumaNode.getId());
                List<Integer> pinnedNodes = NumaUtils.getPinnedNodeIndexList(dbVmNumaNode.getVdsNumaNodeList());
                List<Pair<Guid, Pair<Boolean, Integer>>> runTimePinList = new ArrayList<>();
                for (Pair<Guid, Pair<Boolean, Integer>> pair : vNode.getVdsNumaNodeList()){
                    if ((!pinnedNodes.contains(pair.getSecond().getSecond())) &&
                            (runOnVdsAllNumaNodesMap.containsKey(pair.getSecond().getSecond()))) {
                        pair.setFirst(runOnVdsAllNumaNodesMap.get(pair.getSecond().getSecond()).getId());
                        pair.getSecond().setFirst(false);
                        runTimePinList.add(pair);
                    }
                }
                if (!runTimePinList.isEmpty()) {
                    vNode.setVdsNumaNodeList(runTimePinList);
                    vmNumaNodesNeedUpdate.add(vNode);
                }
            }
        }
        vm.getStatisticsData().getvNumaNodeStatisticsList().addAll(vmNumaNodesNeedUpdate);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDynamic
     */
    private void addVmDynamicToList(VmDynamic vmDynamic) {
        vmDynamicToSave.put(vmDynamic.getId(), vmDynamic);
    }

    /**
     * Add or update vmStatistics to save list
     *
     * @param vmStatistics
     */
    private void addVmStatisticsToList(VmStatistics vmStatistics) {
        vmStatisticsToSave.put(vmStatistics.getId(), vmStatistics);
    }

    private void addVmInterfaceStatisticsToList(List<VmNetworkInterface> list) {
        if (list.isEmpty()) {
            return;
        }
        vmInterfaceStatisticsToSave.put(list.get(0).getVmId(), list);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDevice
     */
    private void addVmDeviceToList(VmDevice vmDevice) {
        vmDeviceToSave.put(vmDevice.getId(), vmDevice);
    }

    /**
     * An access method for test usages
     *
     * @return The devices to be added to the database
     */
    protected List<VmDevice> getNewVmDevices() {
        return Collections.unmodifiableList(newVmDevices);
    }

    /**
     * An access method for test usages
     *
     * @return The devices to be removed from the database
     */
    protected List<VmDeviceId> getRemovedVmDevices() {
        return Collections.unmodifiableList(removedDeviceIds);
    }

    /**
     * An access method for test usages
     *
     * @return The LUNs to update in DB
     */
    protected List<LUNs> getVmLunDisksToSave() {
        return Collections.unmodifiableList(vmLunDisksToSave);
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        AuditLogDirector.log(auditLogable, logType);
    }

    protected Map<Guid, VmInternalData> getRunningVms() {
        return runningVms;
    }
    protected List<VmDynamic> getPoweringUpVms() {
        return poweringUpVms;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    private static void logVmStatusTransionFromUnknown(VM vmToUpdate, VmDynamic runningVm) {
        final AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.setVmId(vmToUpdate.getId());
        auditLogable.addCustomValue("VmStatus", runningVm.getStatus().toString());
        AuditLogDirector.log(auditLogable, AuditLogType.VM_STATUS_RESTORED);
    }

    protected ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }

    protected IVdsEventListener getVdsEventListener() {
        return ResourceManager.getInstance().getEventListener();
    }
}
