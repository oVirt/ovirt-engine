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
public class MoveMacs {

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
     * @param sourceCluster {@link Cluster} cluster instance before update.
     * @param targetMacPoolId macPool Id of updated cluster.
     * @param commandContext {@link CommandContext} instance of calling command.
     *
     */
    public void moveMacsOfUpdatedCluster(Cluster sourceCluster, Guid targetMacPoolId, CommandContext commandContext) {
        Objects.requireNonNull(sourceCluster);
        Objects.requireNonNull(targetMacPoolId);

        Guid sourceMacPoolId = sourceCluster.getMacPoolId();
        Guid clusterId = sourceCluster.getId();
        Objects.requireNonNull(sourceMacPoolId);
        Objects.requireNonNull(clusterId);

        if (needToMigrateMacs(sourceMacPoolId, targetMacPoolId)) {
            List<String> macsToMigrate = vmNicDao.getAllMacsByClusterId(clusterId);
            migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, commandContext);
        }
    }

    private boolean needToMigrateMacs(Guid oldMacPoolId, Guid newMacPoolId) {
        return !oldMacPoolId.equals(newMacPoolId);
    }

    private void migrateMacsToAnotherMacPool(Guid sourceMacPoolId,
            Guid targetMacPoolId,
            List<String> macsToMigrate,
            CommandContext commandContext) {
        Objects.requireNonNull(macsToMigrate);

        MacPool sourcePool = poolPerCluster.getMacPoolById(sourceMacPoolId, commandContext);
        MacPool targetPool = poolPerCluster.getMacPoolById(targetMacPoolId, commandContext);

        for (String mac : macsToMigrate) {
            sourcePool.freeMac(mac);
            targetPool.forceAddMac(mac);
        }
    }

    public void updateClusterAndMoveMacs(Cluster cluster, Guid newMacPoolId, CommandContext commandContext) {
        Guid oldMacPoolId = cluster.getMacPoolId();
        moveMacsOfUpdatedCluster(cluster, newMacPoolId, commandContext);

        if (needToMigrateMacs(oldMacPoolId, newMacPoolId)) {
            cluster.setMacPoolId(newMacPoolId);
            clusterDao.update(cluster);
        }
    }
}
