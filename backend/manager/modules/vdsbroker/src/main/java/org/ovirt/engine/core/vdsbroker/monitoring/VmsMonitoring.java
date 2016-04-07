package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * invoke all Vm analyzers in hand and iterate over their report
 * and take actions - fire VDSM commands (destroy,run/rerun,migrate), report complete actions,
 * hand-over migration and save-to-db
 */
@Singleton
public class VmsMonitoring implements BackendService {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private BalloonMonitoring balloonMonitoring;
    @Inject
    private LunDisksMonitoring lunDisksMonitoring;
    @Inject
    private VmJobsMonitoring vmJobsMonitoring;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private VmGuestAgentInterfaceDao vmGuestAgentInterfaceDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    private static final Logger log = LoggerFactory.getLogger(VmsMonitoring.class);

    private static VmsMonitoring instance;

    public VmsMonitoring() {
    }

    @PostConstruct
    private void init() {
        instance = this;
    }

    public static VmsMonitoring getInstance() {
        return instance;
    }

    /**
     * analyze and react upon changes on the monitoredVms. relevant changes would
     * be persisted and state transitions and internal commands would
     * take place accordingly.
     *
     * @param monitoredVms The Vms we want to monitor and analyze for changes.
-    * VM object represent the persisted object(namely the one in db) and the VmInternalData
-    * is the running one as reported from VDSM
     * @param fetchTime When the VMs were fetched
     * @param vdsManager The manager of the monitored host
     * @param updateStatistics Whether or not this monitoring should include VM statistics
     */
    public void perform(
            List<Pair<VM, VmInternalData>> monitoredVms,
            long fetchTime,
            VdsManager vdsManager,
            boolean updateStatistics) {
        if (monitoredVms.isEmpty()) {
            return;
        }

        List<VmAnalyzer> vmAnalyzers = Collections.emptyList();
        try {
            vmAnalyzers = analyzeVms(monitoredVms, fetchTime, vdsManager, updateStatistics);
            // It is important to add the unmanaged VMs before flushing the dynamic data into the database
            addUnmanagedVms(vmAnalyzers, vdsManager.getVdsId());
            flush(vmAnalyzers);
            postFlush(vmAnalyzers, vdsManager);
            vdsManager.vmsMonitoringInitFinished();
        } catch (RuntimeException ex) {
            log.error("Failed during vms monitoring on host {} error is: {}", vdsManager.getVdsName(), ex);
            log.error("Exception:", ex);
        } finally {
            unlockVms(vmAnalyzers);
        }

    }

    private void unlockVms(List<VmAnalyzer> vmAnalyzers) {
        vmAnalyzers.stream().map(VmAnalyzer::getVmId).forEach(vmId -> {
            VmManager vmManager = resourceManager.getVmManager(vmId);
            vmManager.updateVmDataChangedTime();
            vmManager.unlock();
        });
    }

    /**
     * Analyze the VM data pair
     * Skip analysis on VMs which cannot be locked
     * note: metrics calculation like memCommited and vmsCoresCount should be calculated *before*
     *   this filtering.
     * @return The analyzers which hold all the data per VM
     */
    private List<VmAnalyzer> analyzeVms(
            List<Pair<VM, VmInternalData>> monitoredVms,
            long fetchTime,
            VdsManager vdsManager,
            boolean updateStatistics) {
        VmAnalyzerFactory vmAnalyzerFactory = getVmAnalyzerFactory(vdsManager, updateStatistics);
        List<VmAnalyzer> vmAnalyzers = new ArrayList<>(monitoredVms.size());
        monitoredVms.forEach(vm -> {
            // TODO filter out migratingTo VMs if no action is taken on them
            if (shouldAnalyzeVm(vm, fetchTime, vdsManager.getVdsId())) {
                VmAnalyzer vmAnalyzer = vmAnalyzerFactory.getVmAnalyzer(vm);
                vmAnalyzers.add(vmAnalyzer);
                vmAnalyzer.analyze();
            }
        });
        return vmAnalyzers;
    }

    protected VmAnalyzerFactory getVmAnalyzerFactory(VdsManager vdsManager, boolean statistics) {
        return new VmAnalyzerFactory(
                vdsManager,
                statistics,
                auditLogDirector,
                resourceManager,
                vmDao,
                vmNetworkInterfaceDao,
                vdsDynamicDao,
                vdsNumaNodeDao,
                vmNumaNodeDao);
    }

    private boolean shouldAnalyzeVm(Pair<VM, VmInternalData> pair, long fetchTime, Guid vdsId) {
        Guid vmId = getVmId(pair.getFirst(), pair.getSecond());
        VmManager vmManager = resourceManager.getVmManager(vmId);

        if (!vmManager.trylock()) {
            log.debug("skipping VM '{}' from this monitoring cycle" +
                    " - the VM is locked by its VmManager ", vmId);
            return false;
        }

        if (!vmManager.isLatestData(pair.getSecond(), vdsId)) {
            log.warn("skipping VM '{}' from this monitoring cycle" +
                    " - newer VM data was already processed", vmId);
            vmManager.unlock();
            return false;
        }

        if (vmManager.getVmDataChangedTime() != null && fetchTime - vmManager.getVmDataChangedTime() <= 0) {
            log.warn("skipping VM '{}' from this monitoring cycle" +
                    " - the VM data has changed since fetching the data", vmId);
            vmManager.unlock();
            return false;
        }

        return true;
    }

    private void postFlush(List<VmAnalyzer> vmAnalyzers, VdsManager vdsManager) {
        Collection<Guid> movedToDownVms = new ArrayList<>();
        List<Guid> succeededToRunVms = new ArrayList<>();
        List<Guid> autoVmsToRun = new ArrayList<>();
        List<Guid> coldRebootVmsToRun = new ArrayList<>();
        List<Guid> vmIdsWithBalloonDriverNotRequestedOrAvailable = new ArrayList<>();
        List<Guid> vmIdsWithBalloonDriverRequestedAndUnavailable = new ArrayList<>();
        List<Guid> vmIdsWithGuestAgentUpOrBalloonDeflated = new ArrayList<>();
        List<Guid> vmIdsWithGuestAgentDownAndBalloonInfalted = new ArrayList<>();

        // now loop over the result and act
        for (VmAnalyzer vmAnalyzer : vmAnalyzers) {

            // rerun all vms from rerun list
            if (vmAnalyzer.isRerun()) {
                log.error("Rerun VM '{}'. Called from VDS '{}'", vmAnalyzer.getVmId(), vdsManager.getVdsName());
                resourceManager.rerunFailedCommand(vmAnalyzer.getVmId(), vdsManager.getVdsId());
            }

            if (vmAnalyzer.isSuccededToRun()) {
                vdsManager.succeededToRunVm(vmAnalyzer.getVmId());
                succeededToRunVms.add(vmAnalyzer.getVmId());
            }

            // Refrain from auto-start HA VM during its re-run attempts.
            if (vmAnalyzer.isAutoVmToRun() && !vmAnalyzer.isRerun()) {
                autoVmsToRun.add(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isColdRebootVmToRun()) {
                coldRebootVmsToRun.add(vmAnalyzer.getVmId());
            }

            // process all vms that powering up.
            if (vmAnalyzer.isPoweringUp()) {
                getVdsEventListener().processOnVmPoweringUp(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isMovedToDown()) {
                movedToDownVms.add(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isRemoveFromAsync()) {
                resourceManager.removeAsyncRunningVm(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isVmBalloonDriverNotRequestedOrAvailable()) {
                vmIdsWithBalloonDriverNotRequestedOrAvailable.add(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isVmBalloonDriverRequestedAndUnavailable()) {
                vmIdsWithBalloonDriverRequestedAndUnavailable.add(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isGuestAgentUpOrBalloonDeflated()) {
                vmIdsWithGuestAgentUpOrBalloonDeflated.add(vmAnalyzer.getVmId());
            }

            if (vmAnalyzer.isGuestAgentDownAndBalloonInfalted()) {
                vmIdsWithGuestAgentDownAndBalloonInfalted.add(vmAnalyzer.getVmId());
            }
        }

        getVdsEventListener().updateSlaPolicies(succeededToRunVms, vdsManager.getVdsId());

        // run all vms that crashed that marked with auto startup
        getVdsEventListener().runFailedAutoStartVMs(autoVmsToRun);

        // run all vms that went down as a part of cold reboot process
        getVdsEventListener().runColdRebootVms(coldRebootVmsToRun);

        // process all vms that went down
        getVdsEventListener().processOnVmStop(movedToDownVms, vdsManager.getVdsId());

        getVdsEventListener().refreshHostIfAnyVmHasHostDevices(succeededToRunVms, vdsManager.getVdsId());

        // Looping only over powering up VMs as LUN device size
        // is updated by VDSM only once when running a VM.
        lunDisksMonitoring.process(vmAnalyzers.stream()
                .filter(VmAnalyzer::isPoweringUp)
                .collect(Collectors.toMap(VmAnalyzer::getVmId, VmAnalyzer::getVmLunsMap)));

        vmJobsMonitoring.process(vmAnalyzers.stream()
                .filter(analyzer -> analyzer.getVmJobs() != null)
                .collect(Collectors.toMap(VmAnalyzer::getVmId, VmAnalyzer::getVmJobs)));

        balloonMonitoring.process(
                vmIdsWithBalloonDriverNotRequestedOrAvailable,
                vmIdsWithBalloonDriverRequestedAndUnavailable,
                vmIdsWithGuestAgentUpOrBalloonDeflated,
                vmIdsWithGuestAgentDownAndBalloonInfalted);
    }

    private void flush(List<VmAnalyzer> vmAnalyzers) {
        saveVmDynamic(vmAnalyzers);
        saveVmStatistics(vmAnalyzers);
        saveVmInterfaceStatistics(vmAnalyzers);
        saveVmDiskImageStatistics(vmAnalyzers);
        saveVmGuestAgentNetworkDevices(vmAnalyzers);
    }

    private void saveVmDiskImageStatistics(List<VmAnalyzer> vmAnalyzers) {
        diskImageDynamicDao.updateAllDiskImageDynamicWithDiskIdByVmId(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmDiskImageDynamicToSave)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    private void saveVmDynamic(List<VmAnalyzer> vmAnalyzers) {
        vmDynamicDao.updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmDynamicToSave)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void saveVmInterfaceStatistics(List<VmAnalyzer> vmAnalyzers) {
        vmNetworkStatisticsDao.updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmNetworkStatistics)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private void saveVmStatistics(List<VmAnalyzer> vmAnalyzers) {
        Map<Guid, VmStatistics> vmIdToStatistics = vmAnalyzers.stream()
                .filter(analyzer -> analyzer.getVmStatisticsToSave() != null)
                .collect(Collectors.toMap(VmAnalyzer::getVmId, VmAnalyzer::getVmStatisticsToSave));
        vmStatisticsDao.updateAllInBatch(vmIdToStatistics.values());
        vmIdToStatistics.forEach((vmId, stats) -> resourceManager.getVmManager(vmId).setStatistics(stats));
    }

    protected void addUnmanagedVms(List<VmAnalyzer> vmAnalyzers, Guid vdsId) {
        List<Guid> unmanagedVmIds = vmAnalyzers.stream()
                .filter(VmAnalyzer::isUnmanagedVm)
                .map(VmAnalyzer::getVmId)
                .collect(Collectors.toList());
        getVdsEventListener().addUnmanagedVms(vdsId, unmanagedVmIds);
    }

    // ***** DB interaction *****

    private void saveVmGuestAgentNetworkDevices(List<VmAnalyzer> vmAnalyzers) {
        Map<Guid, List<VmGuestAgentInterface>> vmGuestAgentNics = vmAnalyzers.stream()
                .filter(analyzer -> !analyzer.getVmGuestAgentNics().isEmpty())
                .map(analyzer -> new Pair<>(analyzer.getVmId(), analyzer.getVmGuestAgentNics()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        if (!vmGuestAgentNics.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                for (Guid vmId : vmGuestAgentNics.keySet()) {
                    vmGuestAgentInterfaceDao.removeAllForVm(vmId);
                }

                for (List<VmGuestAgentInterface> nics : vmGuestAgentNics.values()) {
                    if (nics != null) {
                        for (VmGuestAgentInterface nic : nics) {
                            vmGuestAgentInterfaceDao.save(nic);
                        }
                    }
                }
                return null;
            });
        }
    }

    // ***** Helpers and sub-methods *****

    static Guid getVmId(VM dbVm, VmInternalData vdsmVm) {
        return dbVm != null ? dbVm.getId() : vdsmVm.getVmDynamic().getId();
    }

    protected IVdsEventListener getVdsEventListener() {
        return resourceManager.getEventListener();
    }

}
