package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventVmStatsRefresher extends VmStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(EventVmStatsRefresher.class);
    private Subscription subscription;
    @Inject
    private VmDynamicDao vmDynamicDao;
    private final ResourceManager resourceManager;
    private final PollVmStatsRefresher allVmStatsOnlyRefresher;

    @SuppressWarnings("deprecation")
    public EventVmStatsRefresher(VdsManager manager, ResourceManager resourceManager) {
        super(manager);
        // we still want to fetch GetAllVmStats as we did before
        allVmStatsOnlyRefresher = Injector.injectMembers(new PollVmStatsRefresher(vdsManager));
        this.resourceManager = resourceManager;
    }

    @Override
    public void startMonitoring() {
        allVmStatsOnlyRefresher.startMonitoring();
        final String hostname = vdsManager.getVdsHostname();
        resourceManager.subscribe(new EventSubscriber(hostname + "|*|VM_status|*") {

            @Override
            public void onSubscribe(Subscription sub) {
                subscription = sub;
                subscription.request(1);
            }

            @Override
            public void onNext(Map<String, Object> map) {
                try {
                    long fetchTime = System.nanoTime();
                    printEventInDebug(map);
                    List<Pair<VmDynamic, VdsmVm>> vms = convertEvent(map);
                    if (!vms.isEmpty()) {
                        getVmsMonitoring().perform(vms, fetchTime, vdsManager, false);
                        processDevices(vms.stream().map(Pair::getSecond), fetchTime);
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
                ObjectDescriptor.toStringBuilder(map, sb);

                log.debug("processing event for host {} data:\n{}", vdsManager.getVdsName(), sb);
            }

            @SuppressWarnings("unchecked")
            private List<Pair<VmDynamic, VdsmVm>> convertEvent(Map<String, Object> map) {
                Double notifyTime = VdsBrokerObjectsBuilder.removeNotifyTimeFromVmStatusEvent(map);
                return map.entrySet().stream()
                        .map(idToMap -> toMonitoredVm(
                                new Guid(idToMap.getKey()),
                                (Map<String, Object>) idToMap.getValue(),
                                notifyTime))
                        .collect(Collectors.toList());
            }

            private Pair<VmDynamic, VdsmVm> toMonitoredVm(Guid vmId, Map<String, Object> vmMap, Double notifyTime) {
                VmDynamic dbVm = vmDynamicDao.get(vmId);
                VdsmVm vdsmVm = dbVm == null ?
                        createVdsmVm(vmId, vmMap, notifyTime)
                        : createVdsmVm(dbVm, vmMap, notifyTime);
                return new Pair<>(dbVm, vdsmVm);
            }

            private VdsmVm createVdsmVm(Guid vmId, Map<String, Object> struct, Double notifyTime) {
                VmDynamic fakeVm = new VmDynamic();
                fakeVm.setId(vmId);
                return createVdsmVm(fakeVm, struct, notifyTime);
            }

            private VdsmVm createVdsmVm(VmDynamic dbVmDynamic, Map<String, Object> struct, Double notifyTime) {
                // send a clone of vm dynamic to be overridden with new data
                VmDynamic clonedVmDynamic = new VmDynamic(dbVmDynamic);
                VdsBrokerObjectsBuilder.updateVMDynamicData(clonedVmDynamic, struct, vdsManager.getCopyVds());
                return new VdsmVm(notifyTime)
                        .setVmDynamic(clonedVmDynamic)
                        .setDevicesHash(VdsBrokerObjectsBuilder.getVmDevicesHash(struct));
            }

            @Override
            public void onError(Throwable t) {
                // communication issue is delivered as a message so we need to request for more
                subscription.request(1);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    public void stopMonitoring() {
        allVmStatsOnlyRefresher.stopMonitoring();
        subscription.cancel();
    }

}
