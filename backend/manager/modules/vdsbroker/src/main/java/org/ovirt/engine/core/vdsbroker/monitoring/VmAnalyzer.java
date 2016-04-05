package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.ovirt.engine.core.utils.ObjectIdentityChecker.getChangedFields;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UnchangeableByVdsm;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.NumaUtils;
import org.ovirt.engine.core.vdsbroker.NetworkStatisticsBuilder;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible of comparing 2 views of the same VM, one from DB and other as reported from VDSM, run checks, see what changed
 * and record what's changed in its internal state.
 */
public class VmAnalyzer {

    private VM dbVm;
    private final VmInternalData vdsmVm;

    private VmDynamic vmDynamicToSave;
    private boolean saveStatistics;
    private boolean saveVmInterfaces;
    private boolean movedToDown;
    private boolean rerun;
    private boolean clientIpChanged;
    private boolean poweringUp;
    private boolean succeededToRun;
    private boolean removeFromAsync;
    private boolean autoVmToRun;
    private boolean unmanagedVm;
    private boolean coldRebootVmToRun;
    private Collection<Pair<Guid, DiskImageDynamic>> vmDiskImageDynamicToSave;
    private List<VmGuestAgentInterface> vmGuestAgentNics;
    private boolean vmBalloonDriverRequestedAndUnavailable;
    private boolean vmBalloonDriverNotRequestedOrAvailable;
    private boolean guestAgentDownAndBalloonInfalted;
    private boolean guestAgentUpOrBalloonDeflated;
    private List<VmJob> vmJobs;

    private static final int TO_MEGA_BYTES = 1024;
    /** names of fields in {@link org.ovirt.engine.core.common.businessentities.VmDynamic} that are not changed by VDSM */
    private static final List<String> UNCHANGEABLE_FIELDS_BY_VDSM;
    private static final Logger log = LoggerFactory.getLogger(VmAnalyzer.class);

    static {
        List<String> tmpList = new ArrayList<>();
        for (Field field : VmDynamic.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(UnchangeableByVdsm.class)) {
                tmpList.add(field.getName());
            }
        }
        UNCHANGEABLE_FIELDS_BY_VDSM = Collections.unmodifiableList(tmpList);
    }

    private AuditLogDirector auditLogDirector;
    private VdsManager vdsManager;
    private ResourceManager resourceManager;

    private final boolean updateStatistics;
    private Supplier<Map<Integer, VdsNumaNode>> vdsNumaNodesProvider;

    private VmStaticDao vmStaticDao;
    private VmDynamicDao vmDynamicDao;
    private VmDao vmDao;
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    private VdsDao vdsDao;
    private VmNumaNodeDao vmNumaNodeDao;

    public VmAnalyzer(
            VM dbVm,
            VmInternalData vdsmVm,
            boolean updateStatistics,
            VdsManager vdsManager,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            VmStaticDao vmStaticDao,
            VmDynamicDao vmDynamicDao,
            VmDao vmDao,
            VmNetworkInterfaceDao vmNetworkInterfaceDao,
            VdsDao vdsDao,
            Supplier<Map<Integer, VdsNumaNode>> vdsNumaNodesProvider,
            VmNumaNodeDao vmNumaNodeDao) {
        this.dbVm = dbVm;
        this.vdsmVm = vdsmVm;
        this.updateStatistics = updateStatistics;
        this.vdsManager = vdsManager;
        this.auditLogDirector = auditLogDirector;
        this.resourceManager = resourceManager;
        this.vmStaticDao = vmStaticDao;
        this.vmDynamicDao = vmDynamicDao;
        this.vmDao = vmDao;
        this.vmNetworkInterfaceDao = vmNetworkInterfaceDao;
        this.vdsDao = vdsDao;
        this.vdsNumaNodesProvider = vdsNumaNodesProvider;
        this.vmNumaNodeDao = vmNumaNodeDao;
    }

    /**
     * update the internals of this VM
     * against its match
     * this method shouldn't throw exception but fail in isolated way.
     * TODO consider throwing a checked exception or catching one inside
     */
    protected void analyze() {
        if (isVmStoppedBeingReported()) {
            proceedDisappearedVm();
            return;
        }

        if (isExternalOrUnmanagedHostedEngineVm()) {
            processUnmanagedVm();
            return;
        }

        proceedDownVm();
        proceedWatchdogEvents();
        proceedBalloonCheck();
        proceedGuaranteedMemoryCheck();
        updateRepository();
        prepareGuestAgentNetworkDevicesForUpdate();
    }

    private boolean isVmStoppedBeingReported() {
        if (vdsmVm == null) {
            logVmDisappeared();
            return true;
        }
        return false;
    }

    private boolean isExternalOrUnmanagedHostedEngineVm() {
        if (dbVm == null && vmStaticDao.get(vdsmVm.getVmDynamic().getId()) == null) {
            logExternalVmDiscovery();
            return true;
        }
        if (dbVm != null && dbVm.getOrigin() == OriginType.HOSTED_ENGINE) {
            logUnmanagedHostedEngineDiscovery();
            return true;
        }
        return false;
    }

    private void processUnmanagedVm() {
        unmanagedVm = true;
        saveDynamic(vdsmVm.getVmDynamic());
    }

    void proceedDownVm() {
        if (vdsmVm.getVmDynamic().getStatus() == VMStatus.Down) {
            VMStatus prevStatus;
            if (dbVm != null) {
                prevStatus = dbVm.getStatus();
                proceedVmBeforeDeletion();

                // when going to suspend, delete vm from cache later
                if (prevStatus == VMStatus.SavingState) {
                    resourceManager.internalSetVmStatus(dbVm, VMStatus.Suspended);
                }

                clearVm(vdsmVm.getVmDynamic().getExitStatus(),
                        vdsmVm.getVmDynamic().getExitMessage(),
                        vdsmVm.getVmDynamic().getExitReason());
            } else {
                prevStatus = vmDynamicDao.get(vdsmVm.getVmDynamic().getId()).getStatus();
            }

            destroyVm();

            if (dbVm != null && prevStatus == VMStatus.SavingState) {
                afterSuspendTreatment();
            } else if (prevStatus != VMStatus.MigratingFrom) {
                handleVmOnDown();
            }

            if (dbVm != null) {
                removeVmFromCache();
            }
        }
    }

    private void destroyVm() {
        runVdsCommand(
                VDSCommandType.Destroy,
                new DestroyVmVDSCommandParameters(vdsManager.getVdsId(),
                        vdsmVm.getVmDynamic().getId(), null, false, false, 0, true));
    }

    private void handleVmOnDown() {
        // we don't need to have an audit log for the case where the VM went down on a host
        // which is different than the one it should be running on (must be in migration process)
        if (dbVm != null) {
            auditVmOnDownEvent();
        }

        if (vdsmVm.getVmDynamic().getExitStatus() != VmExitStatus.Normal) {
            // Vm failed to run - try to rerun it on other Vds
            if (dbVm != null) {
                if (resourceManager.isVmInAsyncRunningList(vdsmVm.getVmDynamic().getId())) {
                    log.info("Running on VDS '{}' during rerun failed VM: '{}'({})",
                            vdsmVm.getVmDynamic().getRunOnVds(), dbVm.getId(), dbVm.getName());
                    rerun = true;
                } else if (dbVm.isAutoStartup()) {
                    autoVmToRun = true;
                }
            }
            // if failed in destination right after migration
            else { // => cacheVm == null
                resourceManager.removeAsyncRunningVm(vdsmVm.getVmDynamic().getId());
                saveDynamic(vdsmVm.getVmDynamic());
            }
        } else {
            // Vm moved safely to down status. May be migration - just remove it from Async Running command.
            resourceManager.removeAsyncRunningVm(vdsmVm.getVmDynamic().getId());
            if (getVmManager() != null && getVmManager().isColdReboot()) {
                setColdRebootFlag();
            }
        }
    }

    private void saveDynamic(VmDynamic vmDynamic) {
        vmDynamicToSave = vmDynamic;
    }

    private void auditVmOnDownEvent() {
        AuditLogType type = vdsmVm.getVmDynamic().getExitStatus() == VmExitStatus.Normal ?
                AuditLogType.VM_DOWN : AuditLogType.VM_DOWN_ERROR;
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), vdsmVm.getVmDynamic().getId());
        if (vdsmVm.getVmDynamic().getExitMessage() != null) {
            logable.addCustomValue("ExitMessage", "Exit message: " + vdsmVm.getVmDynamic().getExitMessage());
        }
        auditLog(logable, type);
    }

    private void afterSuspendTreatment() {
        VmDynamic vm = vdsmVm.getVmDynamic();
        AuditLogType type = vm.getExitStatus() == VmExitStatus.Normal ? AuditLogType.USER_SUSPEND_VM_OK
                : AuditLogType.USER_FAILED_SUSPEND_VM;

        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), vm.getId());
        auditLog(logable, type);
        resourceManager.removeAsyncRunningVm(vm.getId());
    }

    private void clearVm(VmExitStatus exitStatus, String exitMessage, VmExitReason vmExistReason) {
        if (dbVm.getStatus() != VMStatus.MigratingFrom) {
            // we must check that vm.getStatus() != VMStatus.Down because if it was set to down
            // the exit status and message were set, and we don't want to override them here.
            // we will add it to vmDynamicToSave though because it might been removed from it in #updateRepository
            if (dbVm.getStatus() != VMStatus.Suspended && dbVm.getStatus() != VMStatus.Down) {
                resourceManager.internalSetVmStatus(dbVm,
                        VMStatus.Down,
                        exitStatus,
                        exitMessage,
                        vmExistReason);
            }
            saveDynamic(dbVm.getDynamicData());
            saveStatistics();
            saveVmInterfaces();
            if (!resourceManager.isVmInAsyncRunningList(dbVm.getId())) {
                movedToDown = true;
            }
        }
    }

    private void saveStatistics() {
        saveStatistics = true;
    }

    public VmStatistics getVmStatisticsToSave() {
        return saveStatistics ? dbVm.getStatisticsData() : null;
    }

    public VmDynamic getVmDynamicToSave() {
        return vmDynamicToSave;
    }

    public List<VmNetworkStatistics> getVmNetworkStatistics() {
        return saveVmInterfaces ?
                dbVm.getInterfaces().stream().map(VmNetworkInterface::getStatistics).collect(Collectors.toList())
                : Collections.emptyList();
    }

    // TODO Method with Side-Effect - move to VmsMonitoring
    // switch command execution with state change and let a final execution point at #VmsMonitoring crate tasks out of the new state. this can be delegated to some task Q instead of running in-thread
    private void proceedVmBeforeDeletion() {
        if (dbVm.getStatus() == VMStatus.MigratingFrom) {
            // if a VM that was a source host in migration process is now down with normal
            // exit status that's OK, otherwise..
            if (vdsmVm.getVmDynamic().getExitStatus() != VmExitStatus.Normal) {
                if (dbVm.getMigratingToVds() != null) {
                    destroyVmOnDestinationHost();
                }
                // set vm status to down if source vm crushed
                resourceManager.internalSetVmStatus(dbVm,
                        VMStatus.Down,
                        vdsmVm.getVmDynamic().getExitStatus(),
                        vdsmVm.getVmDynamic().getExitMessage(),
                        vdsmVm.getVmDynamic().getExitReason());
                saveDynamic(dbVm.getDynamicData());
                saveStatistics();
                saveVmInterfaces();
                auditVmMigrationAbort();

                resourceManager.removeAsyncRunningVm(vdsmVm.getVmDynamic().getId());
            }
        }
    }

    private void auditVmMigrationAbort() {
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        logable.addCustomValue("MigrationError", vdsmVm.getVmDynamic().getExitMessage());
        auditLog(logable, AuditLogType.VM_MIGRATION_ABORT);
    }

    private void destroyVmOnDestinationHost() {
        VDSReturnValue destoryReturnValue = runVdsCommand(
                VDSCommandType.DestroyVm,
                new DestroyVmVDSCommandParameters(dbVm.getMigratingToVds(), dbVm.getId(), true, false, 0));
        if (destoryReturnValue.getSucceeded()) {
            log.info("Stopped migrating VM: '{}'({}) on VDS: '{}'",
                    dbVm.getId(), dbVm.getName(), dbVm.getMigratingToVds());
        } else {
            log.info("Could not stop migrating VM: '{}'({}) on VDS: '{}', Error: '{}'",
                    dbVm.getId(), dbVm.getName(), dbVm.getMigratingToVds(), destoryReturnValue.getExceptionString());
        }
    }

    private void proceedWatchdogEvents() {
        VmDynamic vmDynamic = vdsmVm.getVmDynamic();
        VM vmTo = dbVm;
        if (isNewWatchdogEvent(vmDynamic, vmTo)) {
            AuditLogableBase auditLogable = new AuditLogableBase();
            auditLogable.setVmId(vmDynamic.getId());
            auditLogable.addCustomValue("wdaction", vmDynamic.getLastWatchdogAction());
            // for the interpretation of vdsm's response see http://docs.python.org/2/library/time.html
            auditLogable.addCustomValue("wdevent", new Date(vmDynamic.getLastWatchdogEvent() * 1000).toString());
            auditLog(auditLogable, AuditLogType.WATCHDOG_EVENT);
        }
    }

    protected static boolean isNewWatchdogEvent(VmDynamic vmDynamic, VM vmTo) {
        Long lastWatchdogEvent = vmDynamic.getLastWatchdogEvent();
        return vmTo != null && lastWatchdogEvent != null
                && (vmTo.getLastWatchdogEvent() == null || vmTo.getLastWatchdogEvent() < lastWatchdogEvent);
    }

    private void proceedBalloonCheck() {
        if (vdsManager.getCopyVds().isBalloonEnabled()) {
            if (dbVm == null) {
                return;
            }
            VmBalloonInfo balloonInfo = vdsmVm.getVmStatistics().getVmBalloonInfo();
            if (balloonInfo == null) {
                return;
            }
            // last memory is null the first time we check it or when
            // we're not getting the balloon info from vdsm
            // TODO: getBalloonLastMemory always returns null - need to fix
            if (balloonInfo.getBalloonLastMemory() == null || balloonInfo.getBalloonLastMemory() == 0) {
                balloonInfo.setBalloonLastMemory(balloonInfo.getCurrentMemory());
                return;
            }

            if (isBalloonDeviceActiveOnVm(vdsmVm)
                    && (Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())
                    || !isBalloonWorking(balloonInfo))) {
                vmBalloonDriverRequestedAndUnavailable = true;
            } else {
                vmBalloonDriverNotRequestedOrAvailable = true;
            }

            // save the current value for the next time we check it
            balloonInfo.setBalloonLastMemory(balloonInfo.getCurrentMemory());

            if (vdsmVm.getVmStatistics().getUsageMemPercent() != null
                    && vdsmVm.getVmStatistics().getUsageMemPercent() == 0  // guest agent is down
                    && balloonInfo.isBalloonDeviceEnabled() // check if the device is present
                    && !Objects.equals(balloonInfo.getCurrentMemory(), balloonInfo.getBalloonMaxMemory())) {
                guestAgentDownAndBalloonInfalted = true;
            } else {
                guestAgentUpOrBalloonDeflated = true;
            }
        }
    }

    private boolean isBalloonDeviceActiveOnVm(VmInternalData vmInternalData) {
        VmBalloonInfo balloonInfo = vmInternalData.getVmStatistics().getVmBalloonInfo();
        return dbVm.getMinAllocatedMem() < dbVm.getMemSizeMb() // minimum allocated mem of VM == total mem, ballooning is impossible
                && balloonInfo.isBalloonDeviceEnabled()
                && balloonInfo.getBalloonTargetMemory().intValue() != balloonInfo.getBalloonMaxMemory().intValue(); // ballooning was not requested/enabled on this VM
    }

    private void proceedGuaranteedMemoryCheck() {
        if (dbVm != null) {
            VmStatistics vmStatistics = vdsmVm.getVmStatistics();
            if (vmStatistics != null && vmStatistics.getVmBalloonInfo() != null &&
                    vmStatistics.getVmBalloonInfo().getCurrentMemory() != null &&
                    vmStatistics.getVmBalloonInfo().getCurrentMemory() > 0 &&
                    dbVm.getMinAllocatedMem() > vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES) {
                AuditLogableBase auditLogable = new AuditLogableBase();
                auditLogable.addCustomValue("VmName", dbVm.getName());
                auditLogable.addCustomValue("VdsName", vdsManager.getVdsName());
                auditLogable.addCustomValue("MemGuaranteed", String.valueOf(dbVm.getMinAllocatedMem()));
                auditLogable.addCustomValue("MemActual",
                        Long.toString(vmStatistics.getVmBalloonInfo().getCurrentMemory() / TO_MEGA_BYTES));
                auditLog(auditLogable, AuditLogType.VM_MEMORY_UNDER_GUARANTEED_VALUE);
            }
        }
    }


    private void updateRepository() {
        VmDynamic vdsmVmDynamic = vdsmVm.getVmDynamic();

        // if not migrating here and not down
        if (!inMigrationTo() && vdsmVmDynamic.getStatus() != VMStatus.Down) {
            if (dbVm != null) {
                if (!StringUtils.equals(vdsmVmDynamic.getClientIp(), dbVm.getClientIp())) {
                    clientIpChanged = true;
                }

                logVmStatusTransition();

                if (dbVm.getStatus() != VMStatus.Up && vdsmVmDynamic.getStatus() == VMStatus.Up
                        || dbVm.getStatus() != VMStatus.PoweringUp
                        && vdsmVmDynamic.getStatus() == VMStatus.PoweringUp) {
                    poweringUp = true;
                }

                // Generate an event for those machines that transition from "PoweringDown" to
                // "Up" as this means that the power down operation failed:
                if (dbVm.getStatus() == VMStatus.PoweringDown && vdsmVmDynamic.getStatus() == VMStatus.Up) {
                    auditVmPowerDownFailed();
                }

                // log vm recovered from error
                if (dbVm.getStatus() == VMStatus.Paused && dbVm.getVmPauseStatus().isError()
                        && vdsmVmDynamic.getStatus() == VMStatus.Up) {
                    auditVmRecoveredFromError();
                }

                if (isRunSucceeded(vdsmVmDynamic) || isMigrationSucceeded(vdsmVmDynamic)) {
                    // Vm moved to Up status - remove its record from Async
                    // reportedAndUnchangedVms handling
                    log.debug("removing VM '{}' from successful run VMs list", dbVm.getId());
                    succeededToRun = true;
                }
                afterMigrationFrom(vdsmVmDynamic, dbVm);

                if (dbVm.getStatus() != VMStatus.NotResponding
                        && vdsmVmDynamic.getStatus() == VMStatus.NotResponding) {
                    auditVmNotResponding();
                }
                // check if vm is suspended and remove it from async list
                else if (vdsmVmDynamic.getStatus() == VMStatus.Paused) {
                    removeFromAsync = true;
                    if (dbVm.getStatus() != VMStatus.Paused) {
                        auditVmPaused();

                        // check exit message to determine why the VM is paused
                        if (vdsmVmDynamic.getPauseStatus().isError()) {
                            auditVmPausedError(vdsmVmDynamic);
                        }
                    }

                }
            }
            if (dbVm != null || vdsmVmDynamic.getStatus() != VMStatus.MigratingFrom) {
                if (dbVm == null) {
                    dbVm = vmDao.get(vdsmVm.getVmDynamic().getId());
                    // TODO: This is done to keep consistency with VmDao.getById(Guid).
                    // It should probably be removed, but some research is required.
                    dbVm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(dbVm.getId()));

                    if ((isVmMigratingToThisVds() && vdsmVm.getVmDynamic().getStatus().isRunning())
                            || vdsmVm.getVmDynamic().getStatus() == VMStatus.Up) {
                        succeededToRun = true;
                    }
                }

                updateVmDynamicData();
                updateVmStatistics();
                if (!vdsManager.isInitialized()) {
                    resourceManager.removeVmFromDownVms(vdsManager.getVdsId(), vdsmVmDynamic.getId());
                }
            }
        }
    }

    private void auditVmPausedError(VmDynamic vdsmVmDynamic) {
        AuditLogType logType = vmPauseStatusToAuditLogType(vdsmVmDynamic.getPauseStatus());
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        auditLog(logable, logType);
    }

    private void auditVmPaused() {
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        auditLog(logable, AuditLogType.VM_PAUSED);
    }

    private void auditVmRecoveredFromError() {
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        auditLog(logable, AuditLogType.VM_RECOVERED_FROM_PAUSE_ERROR);
    }

    private void auditVmNotResponding() {
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        auditLog(logable, AuditLogType.VM_NOT_RESPONDING);
    }

    private void auditVmPowerDownFailed() {
        AuditLogableBase logable = new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId());
        auditLog(logable, AuditLogType.VM_POWER_DOWN_FAILED);
    }

    private boolean isRunSucceeded(VmDynamic vdsmVmDynamic) {
        return !EnumSet.of(VMStatus.Up, VMStatus.MigratingFrom).contains(dbVm.getStatus())
                && vdsmVmDynamic.getStatus() == VMStatus.Up;
    }

    private boolean isMigrationSucceeded(VmDynamic vdsmVmDynamic) {
        return dbVm.getStatus() == VMStatus.MigratingTo && vdsmVmDynamic.getStatus().isRunning();
    }

    private void updateVmDynamicData() {
        // check if dynamic data changed - update cache and DB
        List<String> changedFields = getChangedFields(dbVm.getDynamicData(), vdsmVm.getVmDynamic());
        // remove all fields that should not be checked:
        changedFields.removeAll(UNCHANGEABLE_FIELDS_BY_VDSM);

        if (vdsmVm.getVmDynamic().getStatus() != VMStatus.Up) {
            changedFields.remove(VmDynamic.APPLICATIONS_LIST_FIELD_NAME);
            vdsmVm.getVmDynamic().setAppList(dbVm.getAppList());
        }

        // if something relevant changed
        if (!changedFields.isEmpty()) {
            dbVm.getDynamicData().updateRuntimeData(vdsmVm.getVmDynamic(), vdsManager.getVdsId());
            saveDynamic(dbVm.getDynamicData());
        }
    }

    private boolean isVmMigratingToThisVds() {
        return dbVm.getStatus() == VMStatus.MigratingFrom && vdsManager.getVdsId().equals(dbVm.getMigratingToVds());
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

    private void logVmStatusTransition() {
        if (dbVm.getStatus() != vdsmVm.getVmDynamic().getStatus()) {
            log.info("VM '{}'({}) moved from '{}' --> '{}'",
                    dbVm.getId(),
                    dbVm.getName(),
                    dbVm.getStatus().name(),
                    vdsmVm.getVmDynamic().getStatus().name());

            if (dbVm.getStatus() == VMStatus.Unknown) {
                logVmStatusTransionFromUnknown();
            }
        }
    }

    private void logExternalVmDiscovery() {
        log.info("VM '{}' was discovered with status '{}' on VDS '{}'({})",
                vdsmVm.getVmDynamic().getId(),
                vdsmVm.getVmDynamic().getStatus().name(),
                vdsManager.getVdsId(),
                vdsManager.getVdsName());
    }

    private void logUnmanagedHostedEngineDiscovery() {
        log.info("VM '{}' that is set as hosted-engine was discovered with status '{}' on VDS '{}'({})",
                vdsmVm.getVmDynamic().getId(),
                vdsmVm.getVmDynamic().getStatus().name(),
                vdsManager.getVdsId(),
                vdsManager.getVdsName());
    }

    private void logVmDisappeared() {
        log.info("VM '{}'({}) is running in db and not running on VDS '{}'({})",
                dbVm.getId(), dbVm.getName(), vdsManager.getVdsId(), vdsManager.getVdsName());
    }

    /**
     * The VM is no longer reported by VDSM.
     * There are 3 different cases:
     * 1. The VM was in MigratingFrom state. We expect it to be down by maybe for some reason it
     * got destroyed so lets move it to the destination host and see (hand-over)..
     * 2. The VM was in PoweringDown state. If it is no longer reported - mission accomplished
     * 3. Otherwise, the VM went down unexpectedly
     */
    private void proceedDisappearedVm() {
        switch (dbVm.getStatus()) {
        case MigratingFrom:
            handOverVM(dbVm);
            break;

        case PoweringDown:
            clearVm(VmExitStatus.Normal,
                    String.format("VM %s shutdown complete", dbVm.getName()),
                    VmExitReason.Success);

            // not sure about that..
            if (getVmManager() != null && getVmManager().isColdReboot()) {
                setColdRebootFlag();
            }
            break;

        default:
            clearVm(VmExitStatus.Error,
                    String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                            dbVm.getName()),
                    VmExitReason.GenericError);

            if (resourceManager.isVmInAsyncRunningList(dbVm.getId())) {
                setRerunFlag();
                break;
            }

            if (getVmManager() != null && getVmManager().isColdReboot()) {
                setColdRebootFlag();
                break;
            }

            if (dbVm.isAutoStartup()) {
                setAutoRunFlag();
                break;
            }
        }
    }

    // del from cache all vms that not in vdsm
    private void removeVmFromCache() {
        // marks the vm was powered down by user but not reported as Down afterwards by vdsm
        boolean poweredDown = false;
        proceedVmBeforeDeletion();
        boolean migrating = dbVm.getStatus() == VMStatus.MigratingFrom;
        if (migrating) {
            handOverVM(dbVm);
        } else if (dbVm.getStatus() == VMStatus.PoweringDown) {
            poweredDown = true;
            clearVm(VmExitStatus.Normal,
                    String.format("VM %s shutdown complete", dbVm.getName()),
                    VmExitReason.Success);
        } else {
            clearVm(VmExitStatus.Error,
                    String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                            dbVm.getName()),
                    VmExitReason.GenericError);
        }

        log.info("VM '{}'({}) is running in db and not running in VDS '{}'",
                dbVm.getId(), dbVm.getName(), vdsManager.getVdsName());

        if (!migrating && !rerun && resourceManager.isVmInAsyncRunningList(dbVm.getId())) {
            setRerunFlag();
        }
        // vm should be auto startup
        // not already in start up list
        // not in reported from vdsm at all (and was not powered-down before)
        // or reported from vdsm with error code
        else if (dbVm.isAutoStartup()
                && !autoVmToRun
                && vdsmVm.getVmDynamic().getExitStatus() != VmExitStatus.Normal
                && !poweredDown) {
            setAutoRunFlag();
        } else if (getVmManager() != null && getVmManager().isColdReboot()) {
            setColdRebootFlag();
        }
    }

    private void handOverVM(VM vmToRemove) {
        Guid destinationHostId = vmToRemove.getMigratingToVds();

        // when the destination VDS is NonResponsive put the VM to Uknown like the rest of its VMs, else MigratingTo
        VMStatus newVmStatus =
                (VDSStatus.NonResponsive == vdsDao.get(destinationHostId).getStatus())
                        ? VMStatus.Unknown
                        : VMStatus.MigratingTo;

        // handing over the VM to the DST by marking it running on it. it will now be its SRC host.
        vmToRemove.setRunOnVds(destinationHostId);

        log.info("Handing over VM '{}'({}) to Host '{}'. Setting VM to status '{}'",
                vmToRemove.getId(),
                vmToRemove.getName(),
                destinationHostId,
                newVmStatus);

        // if the DST host goes unresponsive it will take care all MigratingTo and unknown VMs
        resourceManager.internalSetVmStatus(vmToRemove, newVmStatus);

        // save the VM state
        saveDynamic(vmToRemove.getDynamicData());
        saveStatistics();
        saveVmInterfaces();
    }

    private boolean inMigrationTo() {
        if (vdsmVm.getVmDynamic().getStatus() == VMStatus.MigratingTo) {
            // in migration
            log.info("RefreshVmList VM id '{}' is migrating to VDS '{}'({}) ignoring it in the refresh until migration is done",
                    vdsmVm.getVmDynamic().getId(), vdsManager.getVdsId(), vdsManager.getVdsName());
            return true;
        }

        if (dbVm == null && vdsmVm.getVmDynamic().getStatus() != VMStatus.MigratingFrom) {
            // the VM exists on another VDS
            VmDynamic vmDynamic = vmDynamicDao.get(vdsmVm.getVmDynamic().getId());
            if (vmDynamic.getRunOnVds() != null && vdsmVm.getVmDynamic().getStatus() != VMStatus.Up) {
                log.info("RefreshVmList VM id '{}' status = '{}' on VDS '{}'({}) ignoring it in the refresh until migration is done",
                        vdsmVm.getVmDynamic().getId(), vdsmVm.getVmDynamic().getStatus(), vdsManager.getVdsId(), vdsManager.getVdsName());
                return true;
            }
        }

        return false;
    }

    private void afterMigrationFrom(VmDynamic runningVm, VM vmToUpdate) {
        VMStatus oldVmStatus = vmToUpdate.getStatus();
        VMStatus currentVmStatus = runningVm.getStatus();

        // if the VM's status on source host was MigratingFrom and now the VM is running and its status
        // is not MigratingFrom, it means the migration failed
        if (oldVmStatus == VMStatus.MigratingFrom && currentVmStatus != VMStatus.MigratingFrom
                && currentVmStatus.isRunning()) {
            rerun = true;
            log.info("Adding VM '{}'({}) to re-run list", vmToUpdate.getId(), vmToUpdate.getName());
            vmToUpdate.setMigratingToVds(null);
            vmToUpdate.setMigrationProgressPercent(0);
            saveStatistics();
        }
    }

    private void logVmStatusTransionFromUnknown() {
        final AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.setVmId(dbVm.getId());
        auditLogable.addCustomValue("VmStatus", vdsmVm.getVmDynamic().getStatus().toString());
        auditLog(auditLogable, AuditLogType.VM_STATUS_RESTORED);
    }

    private void updateVmStatistics() {
        if (!updateStatistics) {
            return;
        }

        dbVm.getStatisticsData().updateRuntimeData(vdsmVm.getVmStatistics(), dbVm.getStaticData());
        saveStatistics();
        saveVmInterfaces();
        updateInterfaceStatistics();
        updateVmNumaNodeRuntimeInfo();
        updateDiskImageDynamics();
        updateVmJobs();
    }

    private void updateDiskImageDynamics() {
        vmDiskImageDynamicToSave =  vdsmVm.getVmDynamic().getDisks().stream()
                .map(diskImageDynamic -> new Pair<>(dbVm.getId(), diskImageDynamic))
                .collect(Collectors.toList());
    }

    private void updateInterfaceStatistics() {
        if (vdsmVm.getVmStatistics().getInterfaceStatistics() == null) {
            return;
        }

        if (dbVm.getInterfaces() == null || dbVm.getInterfaces().isEmpty()) {
            dbVm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(dbVm.getId()));
        }
        List<String> macs = new ArrayList<>();

        dbVm.setUsageNetworkPercent(0);

        NetworkStatisticsBuilder statsBuilder = new NetworkStatisticsBuilder(dbVm.getCompatibilityVersion());

        for (VmNetworkInterface ifStats : vdsmVm.getVmStatistics().getInterfaceStatistics()) {
            boolean firstTime = !macs.contains(ifStats.getMacAddress());

            VmNetworkInterface vmIface = null;
            for (VmNetworkInterface tempIf : dbVm.getInterfaces()) {
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
                statsBuilder.updateExistingInterfaceStatistics(vmIface, ifStats);
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
            vmIface.setVmId(dbVm.getId());

            if (ifStats.getSpeed() != null && vmIface.getStatistics().getReceiveRate() != null
                    && vmIface.getStatistics().getReceiveRate() > 0) {

                double rx_percent = vmIface.getStatistics().getReceiveRate();
                double tx_percent = vmIface.getStatistics().getTransmitRate();

                dbVm.setUsageNetworkPercent(Math.max(dbVm.getUsageNetworkPercent(),
                        (int) Math.max(rx_percent, tx_percent)));
            }

            if (firstTime) {
                macs.add(ifStats.getMacAddress());
            }
        }

        Integer maxPercent = 100;
        dbVm.setUsageNetworkPercent((dbVm.getUsageNetworkPercent() > maxPercent) ?
                maxPercent :
                dbVm.getUsageNetworkPercent());
        Integer usageHistoryLimit = Config.getValue(ConfigValues.UsageHistoryLimit);
        dbVm.addNetworkUsageHistory(dbVm.getUsageNetworkPercent(), usageHistoryLimit);
    }

    private void saveVmInterfaces() {
        saveVmInterfaces = true;
    }

    /**
     * Prepare the VM Guest Agent network devices for update. <br>
     * The evaluation of the network devices for update is done by comparing the calculated hash of the network devices
     * from VDSM to the latest hash kept on engine side.
     */
    private void prepareGuestAgentNetworkDevicesForUpdate() {
        VmDynamic vdsmVmDynamic = vdsmVm.getVmDynamic();
        if (vdsmVmDynamic != null) {
            if (dbVm != null) {
                List<VmGuestAgentInterface> vmGuestAgentInterfaces = vdsmVm.getVmGuestAgentInterfaces();
                int guestAgentNicHash = vmGuestAgentInterfaces == null ? 0 : vmGuestAgentInterfaces.hashCode();
                if (guestAgentNicHash != vdsmVmDynamic.getGuestAgentNicsHash()) {
                    if (vmDynamicToSave == null) {
                        saveDynamic(dbVm.getDynamicData());
                    }
                    updateGuestAgentInterfacesChanges(
                            vmDynamicToSave,
                            vmGuestAgentInterfaces,
                            guestAgentNicHash);
                }
            }
        }
    }

    private void updateVmJobs() {
        vmJobs = vdsmVm.getVmStatistics().getVmJobs();
    }

    private void updateVmNumaNodeRuntimeInfo() {
        VmStatistics statistics = vdsmVm.getVmStatistics();
        if (!dbVm.getStatus().isRunning()) {
            dbVm.getStatisticsData().getvNumaNodeStatisticsList().clear();
            return;
        }

        //Build numa nodes map of the host which the dbVm is running on with node index as the key
        Map<Integer, VdsNumaNode> vdsNumaNodes = vdsNumaNodesProvider.get();

        //Build numa nodes map of the dbVm with node index as the key
        Map<Integer, VmNumaNode> vmAllNumaNodesMap = vmNumaNodeDao
                .getAllVmNumaNodeByVmId(dbVm.getId()).stream()
                .collect(Collectors.toMap(VmNumaNode::getIndex, Function.identity()));

        //Initialize the unpinned dbVm numa nodes list with the runtime pinning information
        List<VmNumaNode> vmNumaNodesNeedUpdate = new ArrayList<>();
        for (VmNumaNode vNode : statistics.getvNumaNodeStatisticsList()) {
            VmNumaNode dbVmNumaNode = vmAllNumaNodesMap.get(vNode.getIndex());
            if (dbVmNumaNode != null) {
                vNode.setId(dbVmNumaNode.getId());
                List<Integer> pinnedNodes = NumaUtils.getPinnedNodeIndexList(dbVmNumaNode.getVdsNumaNodeList());
                List<Pair<Guid, Pair<Boolean, Integer>>> runTimePinList = new ArrayList<>();
                for (Pair<Guid, Pair<Boolean, Integer>> pair : vNode.getVdsNumaNodeList()) {
                    if (!pinnedNodes.contains(pair.getSecond().getSecond()) &&
                            vdsNumaNodes.containsKey(pair.getSecond().getSecond())) {
                        pair.setFirst(vdsNumaNodes.get(pair.getSecond().getSecond()).getId());
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
        dbVm.getStatisticsData().getvNumaNodeStatisticsList().addAll(vmNumaNodesNeedUpdate);
    }

    /**** Helpers and sub-methods ****/

    private void updateGuestAgentInterfacesChanges(
            VmDynamic vmDynamic,
            List<VmGuestAgentInterface> vmGuestAgentInterfaces,
            int guestAgentNicHash) {
        vmDynamic.setGuestAgentNicsHash(guestAgentNicHash);
        vmDynamic.setVmIp(extractVmIpsFromGuestAgentInterfaces(vmGuestAgentInterfaces));
        vmGuestAgentNics = vmGuestAgentInterfaces;
    }

    private String extractVmIpsFromGuestAgentInterfaces(List<VmGuestAgentInterface> nics) {
        if (nics == null || nics.isEmpty()) {
            return null;
        }

        List<String> ips = new ArrayList<>();
        for (VmGuestAgentInterface nic : nics) {
            if (nic.getIpv4Addresses() != null) {
                ips.addAll(nic.getIpv4Addresses());
            }
        }
        return ips.isEmpty() ? null : StringUtils.join(ips, " ");
    }

    protected boolean isBalloonWorking(VmBalloonInfo balloonInfo) {
        return Math.abs(balloonInfo.getBalloonLastMemory() - balloonInfo.getBalloonTargetMemory())
                > Math.abs(balloonInfo.getCurrentMemory() - balloonInfo.getBalloonTargetMemory());
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }

    private void setAutoRunFlag() {
        autoVmToRun = true;
        log.info("add VM '{}'({}) to HA rerun treatment", dbVm.getId(), dbVm.getName());
    }

    private void setRerunFlag() {
        rerun = true;
        log.info("add VM '{}'({}) to rerun treatment", dbVm.getId(), dbVm.getName());
    }

    private void setColdRebootFlag() {
        coldRebootVmToRun = true;
        getVmManager().setColdReboot(false);
        log.info("add VM '{}'({}) to cold reboot treatment", dbVm.getId(), dbVm.getName());
    }

    public boolean isRerun() {
        return rerun;
    }

    public boolean isSuccededToRun() {
        return succeededToRun;
    }

    public boolean isAutoVmToRun() {
        return autoVmToRun;
    }

    public boolean isClientIpChanged() {
        return clientIpChanged;
    }

    public VmInternalData getVdsmVm() {
        return vdsmVm;
    }

    public boolean isPoweringUp() {
        return poweringUp;
    }

    public boolean isMovedToDown() {
        return movedToDown;
    }

    public boolean isRemoveFromAsync() {
        return removeFromAsync;
    }

    protected VmManager getVmManager() {
        return dbVm != null ? resourceManager.getVmManager(dbVm.getId()) : null;
    }

    public boolean isColdRebootVmToRun() {
        return coldRebootVmToRun;
    }

    public Collection<Pair<Guid, DiskImageDynamic>> getVmDiskImageDynamicToSave() {
        return vmDiskImageDynamicToSave != null ? vmDiskImageDynamicToSave : Collections.emptyList();
    }

    public List<VmGuestAgentInterface> getVmGuestAgentNics() {
        return vmGuestAgentNics != null ? vmGuestAgentNics : Collections.emptyList();
    }

    protected <P extends VDSParametersBase> VDSReturnValue runVdsCommand(VDSCommandType commandType, P parameters) {
        return resourceManager.runVdsCommand(commandType, parameters);
    }

    public boolean isUnmanagedVm() {
        return unmanagedVm;
    }

    public boolean isVmBalloonDriverRequestedAndUnavailable() {
        return vmBalloonDriverRequestedAndUnavailable;
    }

    public boolean isVmBalloonDriverNotRequestedOrAvailable() {
        return vmBalloonDriverNotRequestedOrAvailable;
    }

    public boolean isGuestAgentDownAndBalloonInfalted() {
        return guestAgentDownAndBalloonInfalted;
    }

    public boolean isGuestAgentUpOrBalloonDeflated() {
        return guestAgentUpOrBalloonDeflated;
    }

    public List<VmJob> getVmJobs() {
        return vmJobs;
    }

    public Guid getVmId() {
        return VmsMonitoring.getVmId(dbVm, vdsmVm);
    }

    public Map<String, LUNs> getVmLunsMap() {
        return vdsmVm.getLunsMap();
    }
}
