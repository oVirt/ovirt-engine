package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * invoke all Vm analyzers in hand and iterate over their report
 * and take actions - fire VDSM commands (destroy,run/rerun,migrate), report complete actions,
 * hand-over migration and save-to-db
 */
public class VmsMonitoring {

    private final AuditLogDirector auditLogDirector;
    /**
     * The managers of the monitored VMs in this cycle.
     */
    private Map<Guid, VmManager> vmManagers = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(VmsMonitoring.class);

    /**
     * @param vdsManager the host manager related to this cycle.
     * @param monitoredVms the vms we want to monitor/analyze/react on. this structure is
     *                     a pair of the persisted (db currently) VM and the running VM which was reported from vdsm.
     *                     Analysis and reactions would be taken on those VMs only.
     */
    public VmsMonitoring(AuditLogDirector auditLogDirector) {
        this.auditLogDirector = auditLogDirector;
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
     * @param timeToUpdateStatistics Whether or not this monitoring should include VM statistics
     */
    public void perform(
            List<Pair<VM, VmInternalData>> monitoredVms,
            long fetchTime,
            VdsManager vdsManager,
            boolean timeToUpdateStatistics) {
        try {
            List<VmAnalyzer> vmAnalyzers = refreshVmStats(monitoredVms, fetchTime, vdsManager, timeToUpdateStatistics);
            afterVMsRefreshTreatment(vmAnalyzers, vdsManager);
            vdsManager.vmsMonitoringInitFinished();
        } catch (RuntimeException ex) {
            log.error("Failed during vms monitoring on host {} error is: {}", vdsManager.getVdsName(), ex);
            log.error("Exception:", ex);
        } finally {
            unlockVmsManager();
        }

    }

    /**
     * lock Vms which has db entity i.e they are managed by a VmManager
     * @return true if lock acquired
     */
    private boolean tryLockVmForUpdate(Pair<VM, VmInternalData> pair, long fetchTime,
            Guid vdsId) {
        Guid vmId = getVmId(pair);

        if (vmId != null) {
            VmManager vmManager = getResourceManager().getVmManager(vmId);

            if (vmManager.trylock()) {
                if (!vmManager.isLatestData(pair.getSecond(), vdsId)) {
                    log.warn("skipping VM '{}' from this monitoring cycle" +
                            " - newer VM data was already processed", vmId);
                    vmManager.unlock();
                } else if (vmManager.getVmDataChangedTime() != null && fetchTime - vmManager.getVmDataChangedTime() <= 0) {
                    log.warn("skipping VM '{}' from this monitoring cycle" +
                            " - the VM data has changed since fetching the data", vmId);
                    vmManager.unlock();
                } else {
                    // store the locked managers to finally release them at the end of the cycle
                    vmManagers.put(vmId, vmManager);
                    return true;
                }
            } else {
                log.debug("skipping VM '{}' from this monitoring cycle" +
                        " - the VM is locked by its VmManager ", getVmId(pair));
            }
        }
        return false;
    }

    private void unlockVmsManager() {
        for (VmManager vmManager : vmManagers.values()) {
            vmManager.updateVmDataChangedTime();
            vmManager.unlock();
        }
    }

    /**
     * Analyze the VM data pair
     * Skip analysis on VMs which cannot be locked
     * note: metrics calculation like memCommited and vmsCoresCount should be calculated *before*
     *   this filtering.
     * @return The analyzers which hold all the data per VM
     */
    private List<VmAnalyzer> refreshVmStats(
            List<Pair<VM, VmInternalData>> monitoredVms,
            long fetchTime,
            VdsManager vdsManager,
            boolean timeToUpdateStatistics) {
        List<VmAnalyzer> vmAnalyzers = new ArrayList<>();
        for (Pair<VM, VmInternalData> monitoredVm : monitoredVms) {
            // TODO filter out migratingTo VMs if no action is taken on them
            if (tryLockVmForUpdate(monitoredVm, fetchTime, vdsManager.getVdsId())) {
                VmAnalyzer vmAnalyzer = getVmAnalyzer(monitoredVm, vdsManager, timeToUpdateStatistics);
                vmAnalyzers.add(vmAnalyzer);
                vmAnalyzer.analyze();
            }
        }

        addUnmanagedVms(vmAnalyzers, vdsManager.getVdsId());
        flush(vmAnalyzers);
        return vmAnalyzers;
    }

    protected VmAnalyzer getVmAnalyzer(
            Pair<VM, VmInternalData> pair,
            VdsManager vdsManager,
            boolean timeToUpdateStatistics) {
        VmAnalyzer vmAnalyzer = new VmAnalyzer(pair.getFirst(), pair.getSecond(), this, timeToUpdateStatistics);
        vmAnalyzer.setAuditLogDirector(auditLogDirector);
        vmAnalyzer.setVdsManager(vdsManager);
        return vmAnalyzer;
    }

    private void afterVMsRefreshTreatment(List<VmAnalyzer> vmAnalyzers, VdsManager vdsManager) {
        Collection<Guid> movedToDownVms = new ArrayList<>();
        List<Guid> succeededToRunVms = new ArrayList<>();
        List<Guid> autoVmsToRun = new ArrayList<>();
        List<Guid> coldRebootVmsToRun = new ArrayList<>();

        // now loop over the result and act
        for (VmAnalyzer vmAnalyzer : vmAnalyzers) {

            // rerun all vms from rerun list
            if (vmAnalyzer.isRerun()) {
                log.error("Rerun VM '{}'. Called from VDS '{}'", vmAnalyzer.getDbVm().getId(), vdsManager.getVdsName());
                getResourceManager().rerunFailedCommand(vmAnalyzer.getDbVm().getId(), vdsManager.getVdsId());
            }

            if (vmAnalyzer.isSuccededToRun()) {
                vdsManager.succeededToRunVm(vmAnalyzer.getDbVm().getId());
                succeededToRunVms.add(vmAnalyzer.getDbVm().getId());
            }

            // Refrain from auto-start HA VM during its re-run attempts.
            if (vmAnalyzer.isAutoVmToRun() && !vmAnalyzer.isRerun()) {
                autoVmsToRun.add(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isColdRebootVmToRun()) {
                coldRebootVmsToRun.add(vmAnalyzer.getDbVm().getId());
            }

            // process all vms that their ip changed.
            if (vmAnalyzer.isClientIpChanged()) {
                final VmDynamic vmDynamic = vmAnalyzer.getVdsmVm().getVmDynamic();
                getVdsEventListener().processOnClientIpChange(vmDynamic.getId(),
                        vmDynamic.getClientIp());
            }

            // process all vms that powering up.
            if (vmAnalyzer.isPoweringUp()) {
                getVdsEventListener().processOnVmPoweringUp(vmAnalyzer.getVdsmVm().getVmDynamic().getId());
            }

            if (vmAnalyzer.isMovedToDown()) {
                movedToDownVms.add(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isRemoveFromAsync()) {
                getResourceManager().removeAsyncRunningVm(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isHostedEngineUnmanaged()) {
                // @since 3.6 - we take existing HE VM and reimport it
                importHostedEngineVM(getVmInfo(Collections.singletonList(vmAnalyzer.getVdsmVm()
                        .getVmDynamic()
                        .getId()
                        .toString()),
                        vdsManager.getVdsId())[0], vdsManager);
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
    }

    private void importHostedEngineVM(Map<String, Object> vmStruct, VdsManager vdsManager) {
        VM vm = VdsBrokerObjectsBuilder.buildVmsDataFromExternalProvider(vmStruct);
        if (vm != null) {
            vm.setImages(VdsBrokerObjectsBuilder.buildDiskImagesFromDevices(vmStruct));
            vm.setInterfaces(VdsBrokerObjectsBuilder.buildVmNetworkInterfacesFromDevices(vmStruct));
            for (DiskImage diskImage : vm.getImages()) {
                vm.getDiskMap().put(Guid.newGuid(), diskImage);
            }
            vm.setClusterId(vdsManager.getClusterId());
            vm.setRunOnVds(vdsManager.getVdsId());
            getVdsEventListener().importHostedEngineVm(vm);
        }
    }

    private void flush(List<VmAnalyzer> vmAnalyzers) {
        saveVmDynamic(vmAnalyzers);
        saveVmStatistics(vmAnalyzers);
        saveVmInterfaceStatistics(vmAnalyzers);
        saveVmDiskImageStatistics(vmAnalyzers);
        saveVmLunDiskStatistics(vmAnalyzers);
        saveVmGuestAgentNetworkDevices(vmAnalyzers);
        saveVmJobsToDb(vmAnalyzers);
    }

    private void saveVmLunDiskStatistics(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getLunDao().updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmLunDisksToSave)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private void saveVmDiskImageStatistics(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getDiskImageDynamicDao().updateAllDiskImageDynamicWithDiskIdByVmId(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmDiskImageDynamicToSave)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    private void saveVmDynamic(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getVmDynamicDao().updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmDynamicToSave)
                .filter(vmDynamic -> vmDynamic != null)
                .collect(Collectors.toList()));
    }

    private void saveVmInterfaceStatistics(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getVmNetworkStatisticsDao().updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmNetworkStatistics)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private void saveVmStatistics(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getVmStatisticsDao().updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmStatisticsToSave)
                .filter(statistics -> statistics != null)
                .collect(Collectors.toList()));
    }

    protected void addUnmanagedVms(List<VmAnalyzer> vmAnalyzers, Guid vdsId) {
        List<Guid> unmanagedVmIds = vmAnalyzers.stream()
                .filter(VmAnalyzer::isExternalVm)
                .map(analyzer -> analyzer.getVdsmVm().getVmDynamic().getId())
                .collect(Collectors.toList());
        getVdsEventListener().addUnmanagedVms(vdsId, unmanagedVmIds);
    }

    // ***** DB interaction *****

    private void saveVmGuestAgentNetworkDevices(List<VmAnalyzer> vmAnalyzers) {
        Map<Guid, List<VmGuestAgentInterface>> vmGuestAgentNics = vmAnalyzers.stream()
                .filter(analyzer -> !analyzer.getVmGuestAgentNics().isEmpty())
                .map(analyzer -> new Pair<>(analyzer.getDbVm().getId(), analyzer.getVmGuestAgentNics()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        if (!vmGuestAgentNics.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    () -> {
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
            );
        }
    }

    private void saveVmJobsToDb(List<VmAnalyzer> vmAnalyzers) {
        getDbFacade().getVmJobDao().updateAllInBatch(vmAnalyzers.stream()
                .map(VmAnalyzer::getVmJobsToUpdate)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        List<Guid> vmJobIdsToRemove = vmAnalyzers.stream()
                .map(VmAnalyzer::getVmJobIdsToRemove)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    () -> {
                        getDbFacade().getVmJobDao().removeAll(vmJobIdsToRemove);
                        return null;
                    });
        }
    }


    // ***** Helpers and sub-methods *****

    /**
     * gets VM full information for the given list of VMs
     */
    protected Map<String, Object>[] getVmInfo(List<String> vmsToUpdate, Guid vdsId) {
        // TODO refactor commands to use vdsId only - the whole vds object here is useless
        VDS vds = new VDS();
        vds.setId(vdsId);
        Map<String, Object>[] result = new Map[0];
        VDSReturnValue vdsReturnValue = getResourceManager().runVdsCommand(VDSCommandType.FullList,
                new FullListVDSCommandParameters(vds, vmsToUpdate));
        if (vdsReturnValue.getSucceeded()) {
            result = (Map<String, Object>[]) vdsReturnValue.getReturnValue();
        }
        return result;
    }

    private Guid getVmId(Pair<VM, VmInternalData> pair) {
        return (pair.getFirst() != null) ?
                pair.getFirst().getId() :
                pair.getSecond() != null ? pair.getSecond().getVmDynamic().getId() : null;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }

    protected IVdsEventListener getVdsEventListener() {
        return ResourceManager.getInstance().getEventListener();
    }

    public VmManager getVmManager(Guid vmId) {
        return vmManagers.get(vmId);
    }

}
