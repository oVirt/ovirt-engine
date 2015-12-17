package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

@Singleton
public class MoveMacsOfUpdatedCluster {

    @Inject
    VmNicDao vmNicDao;

    @Inject
    private MacPoolPerCluster poolPerCluster;

    @Inject
    private ClusterDao clusterDao;

    /**
     * All MACs of given cluster are found, and all of them are {@link MacPool#freeMac(String) freed}
     * from source {@link MacPool macPool} and are
     * {@link MacPool#forceAddMac(String) added}
     * to target {@link MacPool macPool}. Because source macPool may contain duplicates and/or allow
     * duplicates, {@link MacPool#forceAddMac(String)} is used to add them override
     * <em>allowDuplicates</em> setting of target macPool.
     * @param oldMacPoolId id of macPool before update
     * @param newMacPoolId macPool Id of updated cluster.
     * @param clusterId
     *
     */
    public void moveMacsOfUpdatedCluster(Guid oldMacPoolId,
            Guid newMacPoolId,
            Guid clusterId,
            CommandContext commandContext) {
        Objects.requireNonNull(oldMacPoolId); //this should not happen, just make sure this invariant is fulfilled.
        Objects.requireNonNull(newMacPoolId); //this should not happen, just make sure this invariant is fulfilled.

        if (needToMigrateMacs(oldMacPoolId, newMacPoolId)) {
            migrateMacs(oldMacPoolId, newMacPoolId, clusterId, commandContext);
        }
    }

    private boolean needToMigrateMacs(Guid oldMacPoolId, Guid newMacPoolId) {
        return !oldMacPoolId.equals(newMacPoolId);
    }

    private void migrateMacs(Guid oldMacPoolId,
            Guid newMacPoolId,
            Guid clusterId,
            CommandContext commandContext) {
        List<String> vmInterfaceMacs = vmNicDao.getAllMacsByClusterId(clusterId);
        Objects.requireNonNull(vmInterfaceMacs);

        MacPool sourcePool = poolPerCluster.getMacPoolById(oldMacPoolId, commandContext);
        MacPool targetPool = poolPerCluster.getMacPoolById(newMacPoolId, commandContext);

        for (String mac : vmInterfaceMacs) {
            sourcePool.freeMac(mac);
            targetPool.forceAddMac(mac);
        }
    }

    public void updateClusterAndMoveMacs(Cluster cluster, Guid newMacPoolId, CommandContext commandContext) {
        Guid oldMacPoolId = cluster.getMacPoolId();
        moveMacsOfUpdatedCluster(oldMacPoolId, newMacPoolId, cluster.getId(), commandContext);

        if (needToMigrateMacs(oldMacPoolId, newMacPoolId)) {
            cluster.setMacPoolId(newMacPoolId);
            clusterDao.update(cluster);
        }
    }
}
