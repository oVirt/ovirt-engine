package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.ovirt.engine.core.vdsbroker.VmsListFetcher.isDevicesChanged;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.PollAllVmStatsOnlyRefresher;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmStatsRefresher;
import org.ovirt.engine.core.vdsbroker.VmsMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventVmStatsRefresher extends VmStatsRefresher {
    private static final Logger log = LoggerFactory.getLogger(EventVmStatsRefresher.class);
    private Subscription subscription;
    private DbFacade dbFacade;
    private ResourceManager resourceManager;
    private PollAllVmStatsOnlyRefresher allVmStatsOnlyRefresher;

    public EventVmStatsRefresher(VdsManager manager) {
        super(manager);
        // we still want to fetch GetAllVmStats as we did before
        this.allVmStatsOnlyRefresher = Injector.injectMembers(new PollAllVmStatsOnlyRefresher(vdsManager));
        dbFacade = DbFacade.getInstance();
        resourceManager = ResourceManager.getInstance();
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

                log.debug("processing event for host {} data:\n{}", vdsManager.getVdsName(), sb);
            }

            private VmsMonitoring getVmsMonitoring(List<Pair<VM, VmInternalData>> changedVms, List<Pair<VM, VmInternalData>> devicesChangedVms) {
                return new VmsMonitoring(vdsManager, changedVms, devicesChangedVms, auditLogDirector, System.nanoTime());
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
                        if (!vdsManager.getVdsId().equals(dbVm.getRunOnVds())) {
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
                // send a clone of vm dynamic to be overridden with new data
                VmDynamic clonedVmDynamic = new VmDynamic(dbVm.getDynamicData());
                VdsBrokerObjectsBuilder.updateVMDynamicData(clonedVmDynamic, xmlRpcStruct, vdsManager.getCopyVds());
                return new VmInternalData(clonedVmDynamic, dbVm.getStatisticsData(), notifyTime);
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
        allVmStatsOnlyRefresher.stopMonitoring();
        subscription.cancel();
    }
}
