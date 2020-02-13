package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kubevirt.io.V1VirtualMachineInstanceMigration;

@Singleton
public class KubevirtMigrationMonitoring {
    private static final Logger log = LoggerFactory.getLogger(KubevirtMigrationMonitoring.class);

    private Map<Guid, KubevirtClusterMigrationMonitoring> clusterToMonitoring;

    public KubevirtMigrationMonitoring() {
        clusterToMonitoring = new ConcurrentHashMap<>();
    }

    public void register(Guid clusterId, KubevirtClusterMigrationMonitoring monitoring) {
        clusterToMonitoring.put(clusterId, monitoring);
    }

    public void unregister(Guid clusterId) {
        clusterToMonitoring.remove(clusterId);
    }

    public V1VirtualMachineInstanceMigration getMigration(Guid clusterId, KubeResourceId vmId) {
        return clusterToMonitoring.get(clusterId).getMigrationForVm(vmId);
    }
}
