package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.ovirt.engine.core.vdsbroker.VmsListFetcher.isDevicesChanged;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.convertToVmStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VMStatsRefresher;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmsListFetcher;
import org.ovirt.engine.core.vdsbroker.VmsMonitoring;
import org.ovirt.engine.core.vdsbroker.VmsStatisticsFetcher;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventVMStatsRefresher extends VMStatsRefresher {
    private static final Logger log = LoggerFactory.getLogger(EventVMStatsRefresher.class);
    private Subscription subscription;
    private DbFacade dbFacade;
    private ResourceManager resourceManager;

    public EventVMStatsRefresher(VdsManager manager, AuditLogDirector auditLogDirector, SchedulerUtil scheduler) {
        super(manager, auditLogDirector, scheduler);
        this.resourceManager = ResourceManager.getInstance();
        this.dbFacade = DbFacade.getInstance();
    }

    @Override
    public void startMonitoring() {
        super.startMonitoring();
        this.resourceManager.subscribe(new EventSubscriber(manager.getVdsHostname() + "|*|VM_status|*") {

            @Override
            public void onSubscribe(Subscription sub) {
                subscription = sub;
                subscription.request(1);
            }

            @Override
            public void onNext(Map<String, Object> map) {
                try {
                    List<Pair<VM, VmInternalData>> changedVms = new ArrayList<>();
                    List<Pair<VM, VmInternalData>> devicesChangedVms = new ArrayList<>();

                    List<VmInternalData> vdsmVms = convertEvent(map);
                    prepareChanges(vdsmVms, changedVms, devicesChangedVms);

                    new VmsMonitoring(manager, changedVms, devicesChangedVms, auditLogDirector).perform();
                } finally {
                    subscription.request(1);
                }
            }

            private List<VmInternalData> convertEvent(Map<String, Object> map) {
                List<VmInternalData> returnVMs = new ArrayList<VmInternalData>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    VmDynamic vmdynamic = new VmDynamic();
                    vmdynamic.setId(new Guid((String) entry.getKey()));
                    vmdynamic.setStatus(convertToVmStatus((String) map.get(entry.getKey())));
                    VmInternalData vmData = new VmInternalData(vmdynamic, null, null, null);
                    returnVMs.add(vmData);
                }
                return returnVMs;
            }

            private void prepareChanges(List<VmInternalData> vms, List<Pair<VM, VmInternalData>> changedVms,
                    List<Pair<VM, VmInternalData>> devicesChangedVms) {
                Map<Guid, VM> dbVms = dbFacade.getVmDao().getAllRunningByVds(manager.getVdsId());
                for (VmInternalData vdsmVm : vms) {
                    VM dbVm = dbVms.get(vdsmVm.getVmDynamic().getId());

                    VmInternalData vmData = fetchStats(dbVm, vdsmVm);
                    if (vmData != null) {
                        changedVms.add(new Pair<>(dbVm, vmData));
                    }
                    if (isDevicesChanged(dbVm, vdsmVm)) {
                        devicesChangedVms.add(new Pair<>(dbVm, vdsmVm));
                    }
                }
            }

            private VmInternalData fetchStats(VM dbVm, VmInternalData vdsmVm) {
                // TODO move VmStats to be part of an event
                VDSReturnValue vmStats =
                        resourceManager.runVdsCommand(
                                VDSCommandType.GetVmStats,
                                new GetVmStatsVDSCommandParameters(manager.getCopyVds(), vdsmVm.getVmDynamic().getId()));
                if (vmStats.getSucceeded()) {
                    return (VmInternalData) vmStats.getReturnValue();
                } else {
                    if (dbVm != null) {
                        log.error(
                                "failed to fetch VM '{}' stats. status remain unchanged ({})",
                                dbVm.getName(),
                                dbVm.getStatus());
                    }
                }
                return null;
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    public void stopMonitoring() {
        super.stopMonitoring();
        this.subscription.cancel();
    }

    @Override
    @OnTimerMethodAnnotation("perform")
    public void perform() {
        // we still want to fetch GetAllVmStats as we did before
        if (this.manager.isMonitoringNeeded() && getRefreshStatistics()) {
            VmsListFetcher fetcher = new VmsStatisticsFetcher(this.manager);

            fetcher.fetch();

            new VmsMonitoring(this.manager,
                    fetcher.getChangedVms(),
                    fetcher.getVmsWithChangedDevices(), this.auditLogDirector).perform();
        }
    }
}
