package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.ovirt.engine.core.vdsbroker.VmsListFetcher.isDevicesChanged;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.utils.Pair;
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
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
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

                    convertEvent(changedVms, devicesChangedVms, map);

                    if (!changedVms.isEmpty() || !devicesChangedVms.isEmpty()) {
                        new VmsMonitoring(manager, changedVms, devicesChangedVms, auditLogDirector, System.nanoTime()).perform();
                    }
                } finally {
                    subscription.request(1);
                }
            }

            @SuppressWarnings("unchecked")
            private void convertEvent(List<Pair<VM, VmInternalData>> changedVms,
                    List<Pair<VM, VmInternalData>> devicesChangedVms, Map<String, Object> map) {
                Double notifyTime = VdsBrokerObjectsBuilder.removeNotifyTimeFromVmStatusEvent(map);

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Guid vmid = new Guid((String) entry.getKey());
                    VM dbVm = dbFacade.getVmDao().get(vmid);
                    if (dbVm == null) {
                        log.error("failed to fetch VM '{}' from db. Status remain unchanged", vmid);
                        return;
                    }
                    VmInternalData vdsmVm = createVmInternalData(dbVm, (Map<String, Object>) map.get(vmid.toString()), notifyTime);
                    // make sure to ignore events from other hosts during migration
                    // and process once the migration is done
                    if (dbVm.getRunOnVds() == null || dbVm.getRunOnVds().equals(manager.getVdsId())
                            || (!dbVm.getRunOnVds().equals(manager.getVdsId()) && vdsmVm.getVmDynamic().getStatus() == VMStatus.Up)) {
                        if (vdsmVm != null) {
                            changedVms.add(new Pair<>(dbVm, vdsmVm));
                        }
                        if (isDevicesChanged(dbVm, vdsmVm)) {
                            devicesChangedVms.add(new Pair<>(dbVm, vdsmVm));
                        }
                    }
                }
            }

            private VmInternalData createVmInternalData(VM dbVm, Map<String, Object> xmlRpcStruct, Double notifyTime) {
                return new VmInternalData(
                        VdsBrokerObjectsBuilder.buildVmDynamicFromEvent(dbVm.getDynamicData(), xmlRpcStruct),
                        dbVm.getStatisticsData(),
                        notifyTime);
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
                    fetcher.getVmsWithChangedDevices(), this.auditLogDirector, System.nanoTime()).perform();
        }
    }
}
