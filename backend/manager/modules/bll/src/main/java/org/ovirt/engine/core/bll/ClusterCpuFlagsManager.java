package org.ovirt.engine.core.bll;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the Cluster cpu_flags and cpu_verb fields.
 *
 */
@Singleton
public class ClusterCpuFlagsManager implements BackendService {

    private static Logger log = LoggerFactory.getLogger(ClusterCpuFlagsManager.class);

    @Inject
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private VdsDao vdsDao;

    @PostConstruct
    public void updateClusterCpuFlags() {
        for (Cluster cluster : clusterDao.getAll()) {
            updateClusterCpuFlags(cluster);
        }
    }

    public void updateClusterCpuFlags(Cluster cluster) {
        try {
            if (shouldUpdateFlags(cluster)) {
                log.info("Updating cluster CPU flags and verb according to the configuration of the " + cluster.getCpuName() + " cpu");
                updateCpuFlags(cluster);
                clusterDao.update(cluster);
            }
        } catch (Exception e) {
            log.error("Update of cluster's CPU flags failed", e);
        }
    }

    private boolean shouldUpdateFlags(Cluster cluster) {

        if (StringUtils.isEmpty(cluster.getCpuName())) {
            return false;
        }

        if (flagsOrVerbEmpty(cluster)) {
            return true;
        }

        String flags = cpuFlagsManagerHandler.getFlagsByCpuName(cluster.getCpuName(), cluster.getCompatibilityVersion());
        String verb = cpuFlagsManagerHandler.getCpuId(cluster.getCpuName(), cluster.getCompatibilityVersion());

        if (cluster.getCpuFlags().equals(flags) && cluster.getCpuVerb().equals(verb)) {
            return false;
        }

        List<VDS> vdss = vdsDao.getAllForCluster(cluster.getId());
        for (VDS vds : vdss) {
            if (!vds.getStatus().isEligibleForClusterCpuConfigurationChange()) {
                return false;
            }

            List<String> missingFlags = cpuFlagsManagerHandler.missingServerCpuFlags(
                    cluster.getCpuName(),
                    vds.getCpuFlags(),
                    cluster.getCompatibilityVersion());
            if (missingFlags != null) {
                return false;
            }
        }
        return true;
    }

    private boolean flagsOrVerbEmpty(Cluster cluster) {
        return StringUtils.isEmpty(cluster.getCpuFlags())
                || StringUtils.isEmpty(cluster.getCpuVerb());
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
