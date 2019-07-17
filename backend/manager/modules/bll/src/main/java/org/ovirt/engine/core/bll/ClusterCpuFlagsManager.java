package org.ovirt.engine.core.bll;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.dao.ClusterDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes sure that the cluster has non empty cpu flags and cpu verb.
 *
 */
@Singleton
public class ClusterCpuFlagsManager implements BackendService {

    private static Logger log = LoggerFactory.getLogger(ClusterCpuFlagsManager.class);

    @Inject
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private ClusterDao clusterDao;

    @PostConstruct
    public void updateClusterCpuFlags() {
        for (Cluster cluster : clusterDao.getAll()) {
            try {
                if (!StringUtils.isEmpty(cluster.getCpuName()) &&
                        (StringUtils.isEmpty(cluster.getCpuFlags()) ||
                                StringUtils.isEmpty(cluster.getCpuVerb()))) {
                    updateCpuFlags(cluster);
                    clusterDao.update(cluster);
                }
            } catch (Exception e) {
                log.error("Update of cluster's CPU flags failed", e);
            }
        }
    }


    public void updateCpuFlags(Cluster cluster) {
        String flags = cpuFlagsManagerHandler.getFlagsByCpuName(
                cluster.getCpuName(),
                cluster.getCompatibilityVersion());
        if (flags != null) {
            cluster.setCpuFlags(flags);
            cluster.setCpuVerb(cpuFlagsManagerHandler.getCpuId(
                    cluster.getCpuName(),
                    cluster.getCompatibilityVersion()));
        }
    }
}
