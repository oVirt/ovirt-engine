package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

@Singleton
public class DcSingleMacPoolFinder {
    private final ClusterDao clusterDao;

    @Inject
    DcSingleMacPoolFinder(ClusterDao clusterDao) {
        this.clusterDao = Objects.requireNonNull(clusterDao);
    }

    /**
     * @param dcId
     *            the DC ID to be scanned.
     * @return The mac-pool ID if a single one is present in the given DC, otherwise <code>null</code>.
     */
    public Guid find(Guid dcId) {
        final List<Cluster> clusters = clusterDao.getAllForStoragePool(dcId);
        final Set<Guid> macPoolIds = clusters.stream().map(Cluster::getMacPoolId).collect(Collectors.toSet());

        // each cluster, due to db constraint, must have not null reference to pool, thus collection cannot be empty.
        return macPoolIds.size() != 1 ? null : macPoolIds.iterator().next();
    }
}
