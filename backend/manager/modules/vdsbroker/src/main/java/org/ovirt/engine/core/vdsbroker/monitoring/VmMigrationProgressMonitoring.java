package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.Map;
import java.util.concurrent.Flow;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmMigrationProgressMonitoring extends EventSubscriber implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(VmMigrationProgressMonitoring.class);

    @Inject
    private ResourceManager resourceManager;

    private Flow.Subscription subscription;

    public VmMigrationProgressMonitoring() {
        super("*|*|VM_migration_status|*");
    }

    @PostConstruct
    private void subscribe() {
        resourceManager.subscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription sub) {
        subscription = sub;
        subscription.request(1);
    }

    @Override
    public void onNext(Map<String, Object> map) {
        try {
            map.remove(VdsProperties.notify_time);
            map.entrySet().forEach(vmInfo -> {
                Guid vmId = new Guid(vmInfo.getKey());
                Map<?, ?> properties = (Map<?, ?>) vmInfo.getValue();
                int progress = Integer.parseInt(properties.get(VdsProperties.vm_migration_progress).toString());
                VmStatistics vmStatistics = resourceManager.getVmManager(vmId).getStatistics();
                vmStatistics.setMigrationProgressPercent(progress);
                resourceManager.getEventListener().migrationProgressReported(vmId, progress);
                Integer actualDowntime = (Integer) properties.get(VdsProperties.MIGRATION_DOWNTIME);
                if (actualDowntime != null) {
                    resourceManager.getEventListener().actualDowntimeReported(vmId, actualDowntime);
                }
            });
        } finally {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable t) {
        // communication issue is delivered as a message so we need to request for more
        subscription.request(1);
    }

    @Override
    public void onComplete() {
    }

}
