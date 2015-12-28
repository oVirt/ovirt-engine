package org.ovirt.engine.core.bll.network.cluster.helper;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.common.predicates.ActiveVmAttachedToClusterPredicate;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

public final class DisplayNetworkClusterHelper {

    private final NetworkClusterDao networkClusterDao;
    private final NetworkCluster networkCluster;
    private final String networkName;
    private final AuditLogDirector auditLogDirector;

    private final ActiveVmAttachedToClusterPredicate activeVmAttachedToClusterPredicate;

    public DisplayNetworkClusterHelper(
            NetworkClusterDao networkClusterDao,
            VmDao vmDao,
            NetworkCluster networkCluster,
            String networkName,
            AuditLogDirector auditLogDirector) {

        Validate.notNull(networkClusterDao, "networkClusterDao can not be null");
        Validate.notNull(vmDao, "vmDao can not be null");
        Validate.notNull(networkCluster, "networkCluster can not be null");
        Validate.notNull(networkName, "networkName can not be null");
        Validate.notNull(auditLogDirector, "auditLogDirector can not be null");

        this.networkClusterDao = networkClusterDao;
        this.networkCluster = networkCluster;
        this.networkName = networkName;
        this.auditLogDirector = auditLogDirector;
        this.activeVmAttachedToClusterPredicate = new ActiveVmAttachedToClusterPredicate(vmDao);
    }

    public boolean isDisplayToBeUpdated() {
        final NetworkCluster networkClusterBeforeUpdate = networkClusterDao.get(networkCluster.getId());
        return networkClusterBeforeUpdate.isDisplay() != networkCluster.isDisplay();
    }

    public void warnOnActiveVm() {
        if (activeVmAttachedToClusterPredicate.test(networkCluster.getClusterId())) {
            AuditLogableBase loggable = createLoggable();
            auditLogDirector.log(loggable, AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM);
        }
    }

    private AuditLogableBase createLoggable() {
        AuditLogableBase loggable = new AuditLogableBase();
        loggable.setClusterId(networkCluster.getClusterId());
        loggable.addCustomValue("NetworkName", networkName);
        return loggable;
    }
}
