package org.ovirt.engine.core.bll.network.cluster.helper;

import java.util.Objects;

import org.ovirt.engine.core.bll.common.predicates.ActiveVmAttachedToClusterPredicate;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

public final class DisplayNetworkClusterHelper {

    private final NetworkClusterDao networkClusterDao;
    private final ClusterDao clusterDao;
    private final NetworkCluster networkCluster;
    private final String networkName;
    private final AuditLogDirector auditLogDirector;

    private final ActiveVmAttachedToClusterPredicate activeVmAttachedToClusterPredicate;

    public DisplayNetworkClusterHelper(
            NetworkClusterDao networkClusterDao,
            VmDao vmDao,
            ClusterDao clusterDao,
            NetworkCluster networkCluster,
            String networkName,
            AuditLogDirector auditLogDirector) {

        this.networkClusterDao = Objects.requireNonNull(networkClusterDao, "networkClusterDao can not be null");
        this.clusterDao = Objects.requireNonNull(clusterDao, "clusterDao cannot be null");
        this.networkCluster = Objects.requireNonNull(networkCluster, "networkCluster can not be null");
        this.networkName = Objects.requireNonNull(networkName, "networkName can not be null");
        this.auditLogDirector = Objects.requireNonNull(auditLogDirector, "auditLogDirector can not be null");
        this.activeVmAttachedToClusterPredicate =
                new ActiveVmAttachedToClusterPredicate(Objects.requireNonNull(vmDao, "vmDao can not be null"));
    }

    public boolean isDisplayToBeUpdated() {
        final NetworkCluster networkClusterBeforeUpdate = networkClusterDao.get(networkCluster.getId());
        return networkClusterBeforeUpdate.isDisplay() != networkCluster.isDisplay();
    }

    public void warnOnActiveVm() {
        if (activeVmAttachedToClusterPredicate.test(networkCluster.getClusterId())) {
            AuditLogable loggable = createLoggable();
            auditLogDirector.log(loggable, AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM);
        }
    }

    private AuditLogable createLoggable() {
        AuditLogable loggable = new AuditLogableImpl();
        loggable.setClusterName(getClusterName());
        loggable.addCustomValue("NetworkName", networkName);
        return loggable;
    }

    private String getClusterName() {
        final Cluster cluster = clusterDao.get(networkCluster.getClusterId());
        return cluster == null ? null : cluster.getName();
    }
}
