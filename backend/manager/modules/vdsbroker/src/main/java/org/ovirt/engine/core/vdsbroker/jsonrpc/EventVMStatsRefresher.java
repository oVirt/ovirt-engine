package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.ovirt.engine.core.vdsbroker.VmsListFetcher.isDevicesChanged;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
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
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
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
                    printEventInDebug(map);

                    List<Pair<VM, VmInternalData>> changedVms = new ArrayList<>();
                    List<Pair<VM, VmInternalData>> devicesChangedVms = new ArrayList<>();

                    convertEvent(changedVms, devicesChangedVms, map);

                    if (!changedVms.isEmpty() || !devicesChangedVms.isEmpty()) {
                        getVmsMonitoring(changedVms, devicesChangedVms).perform();
                    }
                } finally {
                    subscription.request(1);
                }
            }

            private void printEventInDebug(Map<String, Object> map) {
                if (!log.isDebugEnabled()) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                XmlRpcObjectDescriptor.toStringBuilder(map, sb);

                log.debug("processing event for host {} data:\n{}", manager.getVdsName(), sb);
            }

            private VmsMonitoring getVmsMonitoring(List<Pair<VM, VmInternalData>> changedVms, List<Pair<VM, VmInternalData>> devicesChangedVms) {
                return new VmsMonitoring(manager, changedVms, devicesChangedVms, auditLogDirector, System.nanoTime());
            }

            @SuppressWarnings("unchecked")
            private void convertEvent(List<Pair<VM, VmInternalData>> changedVms,
                    List<Pair<VM, VmInternalData>> devicesChangedVms, Map<String, Object> map) {
                Double notifyTime = VdsBrokerObjectsBuilder.removeNotifyTimeFromVmStatusEvent(map);

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Guid vmid = new Guid((String) entry.getKey());
                    VM dbVm = dbFacade.getVmDao().get(vmid);
                    VmInternalData vdsmVm;
                    if (dbVm == null) {
                        vdsmVm = createVmInternalData(vmid, (Map<String, Object>) map.get(vmid.toString()), notifyTime);
                    } else {
                        vdsmVm = createVmInternalData(dbVm, (Map<String, Object>) map.get(vmid.toString()), notifyTime);

                        // if dbVm runs on different host, monitoring expect it to be null
                        if (!manager.getVdsId().equals(dbVm.getRunOnVds())) {
                            dbVm = null;
                        }
                    }

                    changedVms.add(new Pair<>(dbVm, vdsmVm));
                    if (isDevicesChanged(dbVm, vdsmVm)) {
                        devicesChangedVms.add(new Pair<>(dbVm, vdsmVm));
                    }
                }
            }

            private VmInternalData createVmInternalData(Guid vmId, Map<String, Object> xmlRpcStruct, Double notifyTime) {
                VM fakeVm = new VM();
                fakeVm.setId(vmId);
                return createVmInternalData(fakeVm, xmlRpcStruct, notifyTime);
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
    @OnTimerMethodAnnotation("poll")
    public void poll() {
        // we still want to fetch GetAllVmStats as we did before
        if (this.manager.isMonitoringNeeded() && getRefreshStatistics()) {
            VmsListFetcher fetcher = new VmsStatisticsFetcher(this.manager);

            long fetchTime = System.nanoTime();

            if (fetcher.fetch()) {
                new VmsMonitoring(this.manager,
                        fetcher.getChangedVms(),
                        fetcher.getVmsWithChangedDevices(), this.auditLogDirector, fetchTime, true)
                .perform();
            } else {
                log.info("Failed to fetch vms info for host '{}' - skipping VMs monitoring.", manager.getVdsName());
            }
        }
        updateIteration();
    }
}
