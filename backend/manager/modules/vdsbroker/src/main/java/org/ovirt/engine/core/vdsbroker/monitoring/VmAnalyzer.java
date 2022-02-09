package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.ovirt.engine.core.utils.CollectionUtils.nullToEmptyList;
import static org.ovirt.engine.core.utils.ObjectIdentityChecker.isAnyFieldChanged;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SaveVmExternalDataParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UnchangeableByVdsm;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.vdsbroker.NetworkStatisticsBuilder;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible of comparing 2 views of the same VM, one from DB and other as reported from VDSM, run checks, see what changed
 * and record what's changed in its internal state.
 */
public class VmAnalyzer {

    private final VmDynamic dbVm;
    private final VdsmVm vdsmVm;

    private VmDynamic vmDynamicToSave;
    private boolean movedToDown;
    private boolean rerun;
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
    private VmStatistics statistics;
    private List<VmNetworkInterface> ifaces;

    private static final int TO_MEGA_BYTES = 1024;
    /** names of fields in {@link org.ovirt.engine.core.common.businessentities.VmDynamic} that may change by VDSM */
    private static final Set<String> CHANGEABLE_FIELDS_BY_VDSM;
    private static final Logger log = LoggerFactory.getLogger(VmAnalyzer.class);

    static {
        Set<String> tmpList = Arrays.stream(VmDynamic.class.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(UnchangeableByVdsm.class))
                .map(Field::getName)
                .collect(Collectors.toSet());
        CHANGEABLE_FIELDS_BY_VDSM = Collections.unmodifiableSet(tmpList);
    }

    private AuditLogDirector auditLogDirector;
    private VdsManager vdsManager;
    private ResourceManager resourceManager;

    private final boolean updateStatistics;

    private VdsDynamicDao vdsDynamicDao;
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public VmAnalyzer(
            VmDynamic dbVm,
            VdsmVm vdsmVm,
            boolean updateStatistics,
            VdsManager vdsManager,
            AuditLogDirector auditLogDirector,
            ResourceManager resourceManager,
            VdsDynamicDao vdsDynamicDao,
            VmNetworkInterfaceDao vmNetworkInterfaceDao) {
        this.dbVm = dbVm;
        this.vdsmVm = vdsmVm;
        this.updateStatistics = updateStatistics;
        this.vdsManager = vdsManager;
        this.auditLogDirector = auditLogDirector;
        this.resourceManager = resourceManager;
        this.vdsDynamicDao = vdsDynamicDao;
        this.vmNetworkInterfaceDao = vmNetworkInterfaceDao;
    }

    /**
     * update the internals of this VM against its match
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

        if (isVmDown()) {
            proceedDownVm();
            return;
        }

        if (!isVmRunningInDatabaseOnMonitoredHost()) {
            proceedVmReportedOnOtherHost();
            return;
        }

        proceedVmReportedOnExpectedHost();
    }

    private boolean isVmStoppedBeingReported() {
        if (vdsmVm == null) {
            logVmDisappeared();
            return true;
        }
        return false;
    }

    private boolean isExternalOrUnmanagedHostedEngineVm() {
        if (dbVm == null) {
            logExternalVmDiscovery();
            return true;
        }
        if (getVmManager().getOrigin() == OriginType.HOSTED_ENGINE) {
            logUnmanagedHostedEngineDiscovery();
            return true;
        }
        return false;
    }

    private boolean isVmDown() {
        if (vdsmVm.getVmDynamic().getStatus() == VMStatus.Down) {
            logVmDown();
            return true;
        }
        return false;
    }

    private boolean isVmRunningInDatabaseOnMonitoredHost() {
        if (vdsManager.getVdsId().equals(dbVm.getRunOnVds())) {
            return true;
        }
        boolean migratingToThisVds = vdsmVm.getVmDynamic().getStatus() == VMStatus.MigratingTo
                && vdsManager.getVdsId().equals(dbVm.getMigratingToVds());
        if (!migratingToThisVds) {
            logVmDetectedOnUnexpectedHost();
        }
        return false;
    }

    private void processUnmanagedVm() {
        VmDynamic vmDynamic = vdsmVm.getVmDynamic();
        if (vmDynamic.getStatus() == VMStatus.Down ||
                (vmDynamic.getStatus() == VMStatus.Paused && vmDynamic.getPauseStatus() == VmPauseStatus.EIO)) {
            destroyVm();
            return;
        }
        unmanagedVm = true;
        vmDynamic.setRunOnVds(vdsManager.getVdsId());
        saveDynamic(vmDynamic);
    }

    void proceedVmReportedOnOtherHost() {
        switch(vdsmVm.getVmDynamic().getStatus()) {
        case MigratingTo:
            if (dbVm.getRunOnVds() == null) {
                log.info("VM '{}' is found as migrating on VDS '{}'({}) ",
                        vdsmVm.getVmDynamic().getId(), vdsManager.getVdsId(), vdsManager.getVdsName());
                updateRuntimeData();
                if (!vdsManager.isInitialized()) {
                    resourceManager.removeVmFromDownVms(vdsManager.getVdsId(), vdsmVm.getVmDynamic().getId());
                }
            } else {
                log.info("VM '{}' is migrating to VDS '{}'({}) ignoring it in the refresh until migration is done",
                        vdsmVm.getVmDynamic().getId(), vdsManager.getVdsId(), vdsManager.getVdsName());
            }

            return;

        case MigratingFrom:
            // do nothing
            return;

        case Paused:
            if (vdsmVm.getVmDynamic().getPauseStatus() == VmPauseStatus.POSTCOPY) {
                // do nothing
                return;
            }

            if (dbVm.getRunOnVds() != null // The VM is supposedly running elsewhere
                    && dbVm.getStatus() != VMStatus.Unknown
                    && !isVmMigratingToThisVds()) {
                destroyVm();
                return;
            }

            // otherwise continue with default processing
            break;

        case WaitForLaunch:
            if (dbVm.getStatus() == VMStatus.Unknown) {
                // do nothing, better keep the VM as unknown on the previous host
                // until we are sure that the VM is actually running on this host
                return;
            }

            // otherwise continue with default processing
            break;

        default:
        }

        if (isVmMigratingToThisVds() && vdsmVm.getVmDynamic().getStatus().isRunningOrPaused()) {
            succeededToRun = true;
        }

        if (vdsmVm.getVmDynamic().getStatus() == VMStatus.Up) {
            succeededToRun = true;
        }

        updateRuntimeData();
        updateStatistics();

        if (!vdsManager.isInitialized()) {
            resourceManager.removeVmFromDownVms(vdsManager.getVdsId(), vdsmVm.getVmDynamic().getId());
        }
    }

    void proceedDownVm() {
        // destroy the VM as soon as possible, but save VM data first
        if (isVmRunningInDatabaseOnMonitoredHost() &&
            // not a successful migration on a source host
            (vdsmVm.getVmDynamic().getExitStatus() != VmExitStatus.Normal ||
             vdsmVm.getVmDynamic().getExitReason() != VmExitReason.MigrationSucceeded)) {
            if (!saveVmExternalData()) {
                return;
            }
        }
        destroyVm();

        // VM is running on another host - must be during migration
        if (!isVmRunningInDatabaseOnMonitoredHost()) {
            if (dbVm.getStatus() == VMStatus.MigratingFrom) {
                log.error("Migration of VM '{}' to host '{}' failed: {}.",
                        getVmManager().getName(),
                        vdsManager.getVdsName(),
                        vdsmVm.getVmDynamic().getExitMessage());
            }
            return;
        }

        logVmStatusTransition();
        switch (dbVm.getStatus()) {
        case SavingState:
            resourceManager.internalSetVmStatus(dbVm, VMStatus.Suspended);
            clearVm(vdsmVm.getVmDynamic().getExitStatus(),
                    vdsmVm.getVmDynamic().getExitMessage(),
                    vdsmVm.getVmDynamic().getExitReason());
            resourceManager.removeAsyncRunningVm(dbVm.getId());
            auditVmSuspended();
            break;

        case MigratingFrom:
            if (vdsmVm.getVmDynamic().getExitStatus() == VmExitStatus.Normal &&
                    vdsmVm.getVmDynamic().getExitReason() == VmExitReason.MigrationSucceeded) {
                handOverVm();
                break;
            }

            abortVmMigration(vdsmVm.getVmDynamic().getExitStatus(),
                    vdsmVm.getVmDynamic().getExitMessage(),
                    vdsmVm.getVmDynamic().getExitReason());

            if (vdsmVm.getVmDynamic().getExitStatus() == VmExitStatus.Error && getVmManager().isAutoStart()) {
                setAutoRunFlag();
            }

            break;

        default:
            switch (vdsmVm.getVmDynamic().getExitStatus()) {
            case Error:
                auditVmOnDownError();
                clearVm(vdsmVm.getVmDynamic().getExitStatus(),
                        vdsmVm.getVmDynamic().getExitMessage(),
                        vdsmVm.getVmDynamic().getExitReason());

                if (resourceManager.isVmInAsyncRunningList(vdsmVm.getVmDynamic().getId())) {
                    setRerunFlag();
                    break;
                }

                if (getVmManager().isAutoStart()) {
                    setAutoRunFlag();
                    break;
                }

                break;

            case Normal:
                boolean powerOff = System.nanoTime() - getVmManager().getPowerOffTimeout() < 0;
                auditVmOnDownNormal(powerOff);
                resourceManager.removeAsyncRunningVm(vdsmVm.getVmDynamic().getId());
                clearVm(vdsmVm.getVmDynamic().getExitStatus(),
                        powerOff ? getPowerOffExitMessage() : vdsmVm.getVmDynamic().getExitMessage(),
                        vdsmVm.getVmDynamic().getExitReason());

                if (getVmManager().isColdReboot() || vdsmVm.getVmDynamic().getExitReason() == VmExitReason.DestroyedOnReboot) {
                    setColdRebootFlag();
                }
            }
        }
    }

    boolean saveVmExternalData() {
        SaveVmExternalDataParameters parameters = new SaveVmExternalDataParameters(dbVm.getId(),
                getVmManager().getExternalDataStatus(), true);
        return resourceManager.getEventListener().saveExternalData(parameters).getSucceeded();
    }

    private String getPowerOffExitMessage() {
        return String.format("VM %s power off complete", getVmManager().getName());
    }

    private void destroyVm() {
        DestroyVmVDSCommandParameters parameters = new DestroyVmVDSCommandParameters(vdsManager.getVdsId(), getVmId());
        parameters.setIgnoreNoVm(true);
        runVdsCommand(VDSCommandType.Destroy, parameters);
    }

    private void saveDynamic(VmDynamic vmDynamic) {
        vmDynamicToSave = vmDynamic;
    }

    private void auditVmOnDownNormal(boolean powerOff) {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), getVmId()));
        logable.addCustomValue("ExitMessage",
                !powerOff && vdsmVm.getVmDynamic().getExitMessage() != null ?
                        "Exit message: " + vdsmVm.getVmDynamic().getExitMessage()
                        : " ");
        auditLog(logable, AuditLogType.VM_DOWN);
    }

    private void auditVmOnDownError() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), getVmId()));
        logable.addCustomValue("ExitMessage",
                vdsmVm.getVmDynamic().getExitMessage() != null ?
                        "Exit message: " + vdsmVm.getVmDynamic().getExitMessage()
                        : " ");
        auditLog(logable, AuditLogType.VM_DOWN_ERROR);
    }

    private void auditVmOnRebooting() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), getVmId()));
        logable.addCustomValue("UserName", "Guest OS");
        auditLog(logable, AuditLogType.USER_REBOOT_VM);
    }

    private void auditVmSuspended() {
        VmDynamic vm = vdsmVm.getVmDynamic();
        AuditLogType type = vm.getExitStatus() == VmExitStatus.Normal ? AuditLogType.USER_SUSPEND_VM_OK
                : AuditLogType.USER_FAILED_SUSPEND_VM;

        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), vm.getId()));
        auditLog(logable, type);
    }

    private void clearVm(VmExitStatus exitStatus, String exitMessage, VmExitReason vmExitReason) {
        if (dbVm.getStatus() != VMStatus.MigratingFrom) {
            if (dbVm.getStatus() != VMStatus.Suspended) {
                // if the VM is set to down then the actual exit fields were set and we don't want to
                // override them here. However, we must make sure that other fields like run_on_vds are updated
                boolean alreadyDown = dbVm.getStatus() == VMStatus.Down;
                resourceManager.internalSetVmStatus(dbVm,
                        VMStatus.Down,
                        alreadyDown ? dbVm.getExitStatus() : exitStatus,
                        alreadyDown ? dbVm.getExitMessage() : exitMessage,
                        alreadyDown ? dbVm.getExitReason() : vmExitReason);
                dbVm.setStopReason(getVmManager().getStopReason(getVmId()));
            }
            saveDynamic(dbVm);
            resetVmStatistics();
            resetVmInterfaceStatistics();
            if (!resourceManager.isVmInAsyncRunningList(dbVm.getId())) {
                movedToDown = true;
            }
        }
    }

    private void resetVmStatistics() {
        statistics = new VmStatistics(getVmId());
    }

    protected void resetVmInterfaceStatistics() {
        loadVmNetworkInterfaces();
        ifaces.stream().map(VmNetworkInterface::getStatistics).forEach(VmNetworkStatistics::resetVmStatistics);
    }

    public VmStatistics getVmStatisticsToSave() {
        return statistics;
    }

    public VmDynamic getVmDynamicToSave() {
        return vmDynamicToSave;
    }

    public List<VmNetworkStatistics> getVmNetworkStatistics() {
        return ifaces != null ?
                ifaces.stream().map(VmNetworkInterface::getStatistics).collect(Collectors.toList())
                : Collections.emptyList();
    }

    // TODO Method with Side-Effect - move to VmsMonitoring
    // switch command execution with state change and let a final execution point at #VmsMonitoring crate tasks out of the new state. this can be delegated to some task Q instead of running in-thread
    private void abortVmMigration(VmExitStatus exitStatus, String exitMessage, VmExitReason exitReason) {
        if (dbVm.getMigratingToVds() != null) {
            destroyVmOnDestinationHost();
        }
        // set vm status to down if source vm crushed
        resourceManager.internalSetVmStatus(dbVm, VMStatus.Down, exitStatus, exitMessage, exitReason);
        saveDynamic(dbVm);
        resetVmStatistics();
        resetVmInterfaceStatistics();
        auditVmMigrationAbort(exitMessage);
        resourceManager.removeAsyncRunningVm(dbVm.getId());
        movedToDown = true;
    }

    private void auditVmMigrationAbort(String exitMessage) {
        AuditLogableBase logable =Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        logable.addCustomValue("MigrationError", exitMessage);
        auditLog(logable, AuditLogType.VM_MIGRATION_ABORT);
    }

    private void destroyVmOnDestinationHost() {
        DestroyVmVDSCommandParameters params = new DestroyVmVDSCommandParameters(dbVm.getMigratingToVds(), getVmId());
        params.setIgnoreNoVm(true);
        VDSReturnValue destoryReturnValue = runVdsCommand(VDSCommandType.DestroyVm, params);
        if (destoryReturnValue.getSucceeded()) {
            log.info("Stopped migrating VM: '{}'({}) on VDS: '{}'",
                    dbVm.getId(), getVmManager().getName(), dbVm.getMigratingToVds());
        } else {
            log.info("Could not stop migrating VM: '{}'({}) on VDS: '{}', Error: '{}'",
                    dbVm.getId(), getVmManager().getName(), dbVm.getMigratingToVds(), destoryReturnValue.getExceptionString());
        }
    }

    private void proceedWatchdogEvents() {
        Long lastWatchdogEvent = vdsmVm.getVmDynamic().getLastWatchdogEvent();
        if (lastWatchdogEvent == null) {
            // no watchdog event is reported
            return;
        }
        if (dbVm.getLastWatchdogEvent() == null || dbVm.getLastWatchdogEvent() < lastWatchdogEvent) {
            VmDynamic vmDynamic = vdsmVm.getVmDynamic();
            AuditLogableBase auditLogable = Injector.injectMembers(new AuditLogableBase());
            auditLogable.setVmId(vmDynamic.getId());
            auditLogable.addCustomValue("wdaction", vmDynamic.getLastWatchdogAction());
            // for the interpretation of vdsm's response see http://docs.python.org/2/library/time.html
            auditLogable.addCustomValue("wdevent", new Date(vmDynamic.getLastWatchdogEvent() * 1000).toString());
            auditLog(auditLogable, AuditLogType.WATCHDOG_EVENT);
        }
    }

    private void proceedBalloonCheck() {
        VmBalloonInfo balloonInfo = vdsmVm.getVmBalloonInfo();
        if (balloonInfo == null) {
            return;
        }

        if (!vdsManager.getCopyVds().isBalloonEnabled()) {
            return;
        }

        // last memory is null the first time we check it or when
        // we're not getting the balloon info from vdsm
        // TODO: getBalloonLastMemory always returns null - need to fix
        if (balloonInfo.getBalloonLastMemory() == null || balloonInfo.getBalloonLastMemory() == 0) {
            balloonInfo.setBalloonLastMemory(balloonInfo.getCurrentMemory());
            return;
        }

        if (isBalloonDeviceActiveOnVm()
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

    private boolean isBalloonDeviceActiveOnVm() {
        VmBalloonInfo balloonInfo = vdsmVm.getVmBalloonInfo();
        return getVmManager().getMinAllocatedMem() < getVmManager().getMemSizeMb() // minimum allocated mem of VM == total mem, ballooning is impossible
                && balloonInfo.isBalloonDeviceEnabled()
                && balloonInfo.getBalloonTargetMemory().intValue() != balloonInfo.getBalloonMaxMemory().intValue(); // ballooning was not requested/enabled on this VM
    }

    private void proceedGuaranteedMemoryCheck() {
        VmBalloonInfo vmBalloonInfo = vdsmVm.getVmBalloonInfo();
        if (vmBalloonInfo != null &&
                vmBalloonInfo.getCurrentMemory() != null &&
                vmBalloonInfo.getCurrentMemory() > 0 &&
                getVmManager().getMinAllocatedMem() > vmBalloonInfo.getCurrentMemory() / TO_MEGA_BYTES) {
            AuditLogableBase auditLogable = Injector.injectMembers(new AuditLogableBase());
            auditLogable.addCustomValue("VmName", getVmManager().getName());
            auditLogable.addCustomValue("VdsName", vdsManager.getVdsName());
            auditLogable.addCustomValue("MemGuaranteed", String.valueOf(getVmManager().getMinAllocatedMem()));
            auditLogable.addCustomValue("MemActual",
                    Long.toString(vmBalloonInfo.getCurrentMemory() / TO_MEGA_BYTES));
            auditLog(auditLogable, AuditLogType.VM_MEMORY_UNDER_GUARANTEED_VALUE);
        }
    }


    private void proceedVmReportedOnExpectedHost() {
        if (vdsmVm.getVmDynamic().getStatus() == VMStatus.MigratingTo) {
            log.info("VM '{}' is migrating to VDS '{}'({}) ignoring it in the refresh until migration is done",
                    vdsmVm.getVmDynamic().getId(), vdsManager.getVdsId(), vdsManager.getVdsName());
            return;
        }

        proceedWatchdogEvents();

        VmDynamic vdsmVmDynamic = vdsmVm.getVmDynamic();

        if (dbVm.getClientIp() != null && !Objects.equals(vdsmVmDynamic.getClientIp(), dbVm.getClientIp())) {
            auditClientIpChange();
        }

        logVmStatusTransition();

        if (dbVm.getStatus() == VMStatus.Unknown && vdsmVmDynamic.getStatus() != VMStatus.Unknown) {
            auditVmRestoredFromUnknown();
            if (!EnumSet.of(VMStatus.WaitForLaunch, VMStatus.MigratingTo).contains(vdsmVmDynamic.getStatus())) {
                resourceManager.removeAsyncRunningVm(dbVm.getId());
            }
        }

        if (dbVm.getStatus() != VMStatus.Up && vdsmVmDynamic.getStatus() == VMStatus.Up ||
                dbVm.getStatus() != VMStatus.PoweringUp && vdsmVmDynamic.getStatus() == VMStatus.PoweringUp) {
            poweringUp = true;
        }

        // Generate an event for those machines that transition from "PoweringDown" to
        // "Up" as this means that the power down operation failed:
        if (dbVm.getStatus() == VMStatus.PoweringDown && vdsmVmDynamic.getStatus() == VMStatus.Up) {
            auditVmPowerDownFailed();
        }

        // log vm recovered from error
        if (dbVm.getStatus() == VMStatus.Paused
                && dbVm.getPauseStatus().isError()
                && vdsmVmDynamic.getStatus() == VMStatus.Up) {
            auditVmRecoveredFromError();
        }

        // Generates an event for those machines that are transitioning to
        // "RebootInProgress". This means that reboot has started/been triggered from the guest OS.
        // Engine-initialed reboots shall set the state directly within the RebootVmCommand.
        if (vdsmVmDynamic.getStatus() == VMStatus.RebootInProgress &&
                dbVm.getStatus() != VMStatus.RebootInProgress) {
            getVmManager().rebootCleanup();
            auditVmOnRebooting();
        }

        if (isRunSucceeded() || isMigrationSucceeded()) {
            // Vm moved to Up status - remove its record from Async
            // reportedAndUnchangedVms handling
            log.debug("removing VM '{}' from successful run VMs list", dbVm.getId());
            succeededToRun = true;
        }

        // if the VM's status on source host was MigratingFrom and now the VM is running and its status
        // is not MigratingFrom, it means the migration failed
        if (dbVm.getStatus() == VMStatus.MigratingFrom
                && vdsmVmDynamic.getStatus() != VMStatus.MigratingFrom
                && vdsmVmDynamic.getStatus().isRunning()) {
            rerun = true;
            log.info("Adding VM '{}'({}) to re-run list", dbVm.getId(), getVmManager().getName());
            dbVm.setMigratingToVds(null);
            getVmManager().getStatistics().setMigrationProgressPercent(0);
        }

        if (dbVm.getStatus() != VMStatus.NotResponding
                && vdsmVmDynamic.getStatus() == VMStatus.NotResponding) {
            auditVmNotResponding();
        }

        if (vdsmVmDynamic.getStatus() == VMStatus.Paused) {
            if (vdsmVmDynamic.getPauseStatus() == VmPauseStatus.POSTCOPY) {
                handOverVm();
                // no need to do anything else besides handing over the VM
                return;
            }

            switch (dbVm.getStatus()) {
            case Paused:
                break;

            default:
                // otherwise, remove the vm from async list
                removeFromAsync = true;
                auditVmPaused();
                // check exit message to determine why the VM is paused
                if (vdsmVmDynamic.getPauseStatus().isError()) {
                    auditVmPausedError(vdsmVmDynamic);
                }
            }
        }

        updateVmDynamicData();
        updateStatistics();

        if (!vdsManager.isInitialized()) {
            resourceManager.removeVmFromDownVms(vdsManager.getVdsId(), vdsmVm.getVmDynamic().getId());
        }
    }

    public void auditClientIpChange() {
        final AuditLogableBase event = Injector.injectMembers(new AuditLogableBase());
        event.setVmId(dbVm.getId());
        event.setUserName(dbVm.getConsoleCurrentUserName());
        String clientIp = vdsmVm.getVmDynamic().getClientIp();
        auditLogDirector.log(event, clientIp == null || clientIp.isEmpty() ?
                AuditLogType.VM_CONSOLE_DISCONNECTED : AuditLogType.VM_CONSOLE_CONNECTED);
    }

    private void auditVmPausedError(VmDynamic vdsmVmDynamic) {
        AuditLogType logType = vmPauseStatusToAuditLogType(vdsmVmDynamic.getPauseStatus());
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        auditLog(logable, logType);
    }

    private void auditVmPaused() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        auditLog(logable, AuditLogType.VM_PAUSED);
    }

    private void auditVmRecoveredFromError() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        auditLog(logable, AuditLogType.VM_RECOVERED_FROM_PAUSE_ERROR);
    }

    private void auditVmNotResponding() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        auditLog(logable, AuditLogType.VM_NOT_RESPONDING);
    }

    private void auditVmPowerDownFailed() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase(vdsManager.getVdsId(), dbVm.getId()));
        auditLog(logable, AuditLogType.VM_POWER_DOWN_FAILED);
    }

    private boolean isRunSucceeded() {
        return !EnumSet.of(VMStatus.Up, VMStatus.MigratingFrom).contains(dbVm.getStatus())
                && vdsmVm.getVmDynamic().getStatus() == VMStatus.Up;
    }

    private boolean isMigrationSucceeded() {
        return dbVm.getStatus() == VMStatus.MigratingTo && vdsmVm.getVmDynamic().getStatus().isRunningOrPaused();
    }

    private void updateVmDynamicData() {
        // do not consider changes to app list before the status is UP (since the guest agent is not loaded yet)
        if (vdsmVm.getVmDynamic().getStatus() != VMStatus.Up) {
            vdsmVm.getVmDynamic().setAppList(dbVm.getAppList());
        }

        // if something relevant changed
        if (isAnyRuntimeFieldChanged()) {
            updateRuntimeData();
        }
    }

    private void updateRuntimeData() {
        if (vdsmVm.getVmDynamic().getGuestAgentNicsHash() != dbVm.getGuestAgentNicsHash()) {
            vmGuestAgentNics = filterGuestAgentInterfaces(nullToEmptyList(vdsmVm.getVmGuestAgentInterfaces()));
            dbVm.setIp(extractVmIps(vmGuestAgentNics));
        }

        dbVm.updateRuntimeData(vdsmVm.getVmDynamic(), vdsManager.getVdsId());
        saveDynamic(dbVm);
    }

    private boolean isAnyRuntimeFieldChanged() {
        switch (getVmManager().getOrigin()) {
        case KUBEVIRT:
            // that is a workaround for kubevirt since we place the IP we get from kubevirt in VmDynamic
            // rather than setting vmGuestAgentInterfaces
            if (!Objects.equals(dbVm.getIp(), vdsmVm.getVmDynamic().getIp())) {
                dbVm.setIp(vdsmVm.getVmDynamic().getIp());
                return true;
            }
        default:
            return isAnyFieldChanged(dbVm, vdsmVm.getVmDynamic(), CHANGEABLE_FIELDS_BY_VDSM);
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

    private void logVmHandOver(Guid destinationHostId, VMStatus newVmStatus) {
        log.info("Handing over VM '{}'({}) to Host '{}'. Setting VM to status '{}'",
                dbVm.getId(),
                getVmManager().getName(),
                destinationHostId,
                newVmStatus);
    }

    private void logVmStatusTransition() {
        if (dbVm.getStatus() != vdsmVm.getVmDynamic().getStatus()) {
            log.info("VM '{}'({}) moved from '{}' --> '{}'",
                    dbVm.getId(),
                    getVmManager().getName(),
                    dbVm.getStatus().name(),
                    vdsmVm.getVmDynamic().getStatus().name());
        }
    }

    private void logVmDetectedOnUnexpectedHost() {
        log.info("VM '{}'({}) was unexpectedly detected as '{}' on VDS '{}'({}) (expected on '{}')",
                dbVm.getId(),
                getVmManager().getName(),
                vdsmVm.getVmDynamic().getStatus().name(),
                vdsManager.getVdsId(),
                vdsManager.getVdsName(),
                dbVm.getRunOnVds());
    }

    private void logExternalVmDiscovery() {
        log.info("VM '{}' was discovered as '{}' on VDS '{}'({})",
                vdsmVm.getVmDynamic().getId(),
                vdsmVm.getVmDynamic().getStatus().name(),
                vdsManager.getVdsId(),
                vdsManager.getVdsName());
    }

    private void logUnmanagedHostedEngineDiscovery() {
        log.info("VM '{}' that is set as hosted-engine was discovered as '{}' on VDS '{}'({})",
                vdsmVm.getVmDynamic().getId(),
                vdsmVm.getVmDynamic().getStatus().name(),
                vdsManager.getVdsId(),
                vdsManager.getVdsName());
    }

    private void logVmDisappeared() {
        log.info("VM '{}'({}) is running in db and not running on VDS '{}'({})",
                dbVm.getId(), getVmManager().getName(), vdsManager.getVdsId(), vdsManager.getVdsName());
    }

    private void logVmDown() {
        log.info("VM '{}' was reported as Down on VDS '{}'({})",
                vdsmVm.getVmDynamic().getId(), vdsManager.getVdsId(), vdsManager.getVdsName());
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
        if (System.nanoTime() - getVmManager().getPowerOffTimeout() < 0) {
            auditVmOnDownNormal(true);
            resourceManager.removeAsyncRunningVm(dbVm.getId());
            clearVm(VmExitStatus.Normal,
                    getPowerOffExitMessage(),
                    VmExitReason.Success);
            return;
        }

        switch (dbVm.getStatus()) {
        case MigratingFrom:
            if (dbVm.getMigratingToVds() != null) {
                handOverVm();
                break;
            }

            abortVmMigration(VmExitStatus.Error,
                    String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                            getVmManager().getName()),
                    VmExitReason.GenericError);

            // TODO: cold reboot + auto restart

            break;

        case PoweringDown:
            clearVm(VmExitStatus.Normal,
                    String.format("VM %s shutdown complete", getVmManager().getName()),
                    VmExitReason.Success);

            // not sure about that..
            if (getVmManager().isColdReboot()) {
                setColdRebootFlag();
            }
            break;

        default:
            clearVm(VmExitStatus.Error,
                    String.format("Could not find VM %s on host, assuming it went down unexpectedly",
                            getVmManager().getName()),
                    VmExitReason.GenericError);

            if (resourceManager.isVmInAsyncRunningList(dbVm.getId())) {
                setRerunFlag();
                break;
            }

            if (getVmManager().isColdReboot()) {
                setColdRebootFlag();
                break;
            }

            if (getVmManager().isAutoStart()) {
                setAutoRunFlag();
                break;
            }
        }
    }

    private void handOverVm() {
        Guid dstHostId = dbVm.getMigratingToVds();
        // when the destination VDS is NonResponsive put the VM to Unknown like the rest of its VMs
        VMStatus newVmStatus = isVdsNonResponsive(dstHostId) ? VMStatus.Unknown : VMStatus.MigratingTo;
        dbVm.setRunOnVds(dstHostId);
        logVmHandOver(dstHostId, newVmStatus);
        resourceManager.internalSetVmStatus(dbVm, newVmStatus);
        saveDynamic(dbVm);
    }

    private boolean isVdsNonResponsive(Guid vdsId) {
        return vdsId != null && vdsDynamicDao.get(vdsId).getStatus() == VDSStatus.NonResponsive;
    }

    private void auditVmRestoredFromUnknown() {
        final AuditLogableBase auditLogable = Injector.injectMembers(new AuditLogableBase());
        auditLogable.setVmId(dbVm.getId());
        auditLogable.addCustomValue("VmStatus", vdsmVm.getVmDynamic().getStatus().toString());
        auditLog(auditLogable, AuditLogType.VM_STATUS_RESTORED);
    }

    private void updateStatistics() {
        if (!updateStatistics) {
            return;
        }

        proceedBalloonCheck();
        proceedGuaranteedMemoryCheck();
        updateVmStatistics();
        updateInterfaceStatistics();
        updateDiskImageDynamics();
        updateVmJobs();
    }

    private void updateVmStatistics() {
        statistics = getVmManager().getStatistics();
        Integer reportedMigrationProgress = vdsmVm.getVmStatistics().getMigrationProgressPercent();
        boolean updateMigrationProgress = reportedMigrationProgress == null;
        statistics.updateRuntimeData(
                vdsmVm.getVmStatistics(),
                getVmManager().getNumOfCpus(),
                updateMigrationProgress);
        // Apparently, users want to see the time that the guest is running rather than that of the VM and therefore
        // it needs to be cleared when the guest reboots. So let's keep using the value that is reported by VDSM.
        /*if (dbVm.getBootTime() != null) {
            statistics.setElapsedTime((System.currentTimeMillis() - dbVm.getBootTime().getTime()  - dbVm.getDowntime()) / 1000.0);
        }*/
    }

    private void updateDiskImageDynamics() {
        vmDiskImageDynamicToSave =  vdsmVm.getDiskStatistics().stream()
                .map(diskImageDynamic -> new Pair<>(dbVm.getId(), diskImageDynamic))
                .collect(Collectors.toList());
    }

    private void updateInterfaceStatistics() {
        List<VmNetworkInterface> ifsStats = vdsmVm.getInterfaceStatistics();
        if (ifsStats == null || ifsStats.isEmpty()) {
            return;
        }

        loadVmNetworkInterfaces();
        List<String> macs = new ArrayList<>();

        statistics.setUsageNetworkPercent(0);

        NetworkStatisticsBuilder statsBuilder = new NetworkStatisticsBuilder();

        for (VmNetworkInterface ifStats : ifsStats) {
            boolean firstTime = !macs.contains(ifStats.getMacAddress());

            VmNetworkInterface vmIface = ifaces.stream()
                    .filter(iface -> iface.getMacAddress().equals(ifStats.getMacAddress()))
                    .findFirst()
                    .orElse(null);

            if (vmIface == null) {
                continue;
            }

            // VDSM is going to stop reporting the speed, so we override it with the value from the database
            // TODO: the speed should not be part of the statistics at all, needs to move it elsewhere
            ifStats.setSpeed(vmIface.getSpeed());
            // RX rate and TX rate are reported by VDSM in % (minimum value
            // 0, maximum value 100)
            // Rx drop and TX drop are reported in packet numbers

            // if rtl+pv it will get here 2 times (we take the max one)
            if (firstTime) {
                statsBuilder.updateExistingInterfaceStatistics(vmIface, ifStats);
            } else {
                vmIface.getStatistics().setReceiveRate(max(vmIface.getStatistics().getReceiveRate(),
                        ifStats.getStatistics().getReceiveRate()));
                vmIface.getStatistics().setReceiveDrops(
                        vmIface.getStatistics().getReceiveDrops().max(ifStats.getStatistics().getReceiveDrops())
                );
                vmIface.getStatistics().setTransmitRate(max(vmIface.getStatistics().getTransmitRate(),
                        ifStats.getStatistics().getTransmitRate()));
                vmIface.getStatistics().setTransmitDrops(
                        vmIface.getStatistics().getTransmitDrops().max(ifStats.getStatistics().getTransmitDrops())
                );
            }
            vmIface.setVmId(dbVm.getId());

            if (ifStats.getSpeed() != null && vmIface.getStatistics().getReceiveRate() != null
                    && vmIface.getStatistics().getReceiveRate() > 0) {

                double rx_percent = vmIface.getStatistics().getReceiveRate();
                double tx_percent = vmIface.getStatistics().getTransmitRate();

                statistics.setUsageNetworkPercent(max(statistics.getUsageNetworkPercent(),
                        (int) max(rx_percent, tx_percent)));
            }

            if (firstTime) {
                macs.add(ifStats.getMacAddress());
            }
        }

        statistics.setUsageNetworkPercent(min(statistics.getUsageNetworkPercent(), 100));
        Integer usageHistoryLimit = Config.getValue(ConfigValues.UsageHistoryLimit);
        statistics.addNetworkUsageHistory(statistics.getUsageNetworkPercent(), usageHistoryLimit);
    }

    private void updateVmJobs() {
        vmJobs = vdsmVm.getVmJobs();
    }

    /**** Helpers and sub-methods ****/

    private List<VmGuestAgentInterface> filterGuestAgentInterfaces(List<VmGuestAgentInterface> nics) {
        if (!nics.isEmpty()) {
            loadVmNetworkInterfaces();
            Map<String, VmNetworkInterface> ifacesByMac = ifaces.stream()
                    .collect(Collectors.toMap(VmNic::getMacAddress, iface -> iface));
            nics = nics.stream()
                    .filter(this::isNotBlacklisted)
                    .filter(nic -> ifacesByMac.get(nic.getMacAddress()) != null)
                    .filter(nic -> ifacesByMac.get(nic.getMacAddress()).getVnicProfileId() != null)
                    .collect(Collectors.toList());

            nics.forEach(this::filterIpv4Addresses);
            nics.forEach(this::filterIpv6Addresses);
        }
        return nics;
    }

    private boolean isNotBlacklisted(VmGuestAgentInterface nic) {
        List<String> blacklist = Config.getValue(ConfigValues.GuestNicNamesBlacklist);
        return nic.getInterfaceName() == null || blacklist.stream().noneMatch(nic.getInterfaceName()::matches);
    }

    private void filterIpv4Addresses(VmGuestAgentInterface nic) {
        nic.setIpv4Addresses(nic.getIpv4Addresses().stream()
                .filter(ValidationUtils::isValidIpv4)
                .collect(Collectors.toList()));
    }

    private void filterIpv6Addresses(VmGuestAgentInterface nic) {
        nic.setIpv6Addresses(nic.getIpv6Addresses().stream()
                .map(NetworkUtils::stripIpv6ZoneIndex)
                .filter(ValidationUtils::isValidIpv6)
                .collect(Collectors.toList()));
    }

    private String extractVmIps(List<VmGuestAgentInterface> nics) {
        Stream<String> ipsV4 = nics.stream().map(VmGuestAgentInterface::getIpv4Addresses).flatMap(List::stream);
        Stream<String> ipsV6 = nics.stream().map(VmGuestAgentInterface::getIpv6Addresses).flatMap(List::stream);
        String ips = Stream.of(ipsV4, ipsV6).flatMap(stream -> stream).collect(Collectors.joining(" "));
        return ips.isEmpty() ? null : ips;
    }

    protected boolean isBalloonWorking(VmBalloonInfo balloonInfo) {
        return abs(balloonInfo.getBalloonLastMemory() - balloonInfo.getBalloonTargetMemory())
                > abs(balloonInfo.getCurrentMemory() - balloonInfo.getBalloonTargetMemory());
    }

    protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }

    private void setAutoRunFlag() {
        autoVmToRun = true;
        log.info("add VM '{}'({}) to HA rerun treatment", dbVm.getId(), getVmManager().getName());
    }

    private void setRerunFlag() {
        rerun = true;
        log.info("add VM '{}'({}) to rerun treatment", dbVm.getId(), getVmManager().getName());
    }

    private void setColdRebootFlag() {
        coldRebootVmToRun = true;
        getVmManager().setColdReboot(false);
        log.info("add VM '{}'({}) to cold reboot treatment", dbVm.getId(), getVmManager().getName());
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

    public VdsmVm getVdsmVm() {
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
        return resourceManager.getVmManager(dbVm.getId());
    }

    protected void loadVmNetworkInterfaces() {
        if (ifaces == null) {
            ifaces = vmNetworkInterfaceDao.getAllForMonitoredVm(getVmId());
        }
    }

    public boolean isColdRebootVmToRun() {
        return coldRebootVmToRun;
    }

    public Collection<Pair<Guid, DiskImageDynamic>> getVmDiskImageDynamicToSave() {
        return vmDiskImageDynamicToSave != null ? vmDiskImageDynamicToSave : Collections.emptyList();
    }

    public List<VmGuestAgentInterface> getVmGuestAgentNics() {
        return vmGuestAgentNics;
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
