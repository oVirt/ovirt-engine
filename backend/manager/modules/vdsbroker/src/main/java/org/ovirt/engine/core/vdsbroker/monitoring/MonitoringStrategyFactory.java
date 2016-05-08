package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * This class is responsible for returning the correct service strategy, according to the service the cluster supports
 */
@Singleton
public class MonitoringStrategyFactory {
    private MonitoringStrategy virtMonitoringStrategy;
    private MonitoringStrategy glusterMonitoringStrategy;
    private MultipleServicesMonitoringStrategy multipleMonitoringStrategy;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private VdsDao vdsDao;

    @PostConstruct
    private void init() {
        virtMonitoringStrategy = new VirtMonitoringStrategy(clusterDao, vdsDao);
        glusterMonitoringStrategy = new GlusterMonitoringStrategy();
        multipleMonitoringStrategy = new MultipleServicesMonitoringStrategy();
        multipleMonitoringStrategy.addMonitoringStrategy(virtMonitoringStrategy);
        multipleMonitoringStrategy.addMonitoringStrategy(glusterMonitoringStrategy);
    }

    /**
     * This method gets the VDS, and returns the correct service strategy, according to the service the cluster that the VDS belongs to supports
     */
    public MonitoringStrategy getMonitoringStrategyForVds(VDS vds) {
        Cluster cluster = clusterDao.get(vds.getClusterId());

        if (cluster.supportsVirtService() && cluster.supportsGlusterService()) {
            return multipleMonitoringStrategy;
        }

        if (cluster.supportsVirtService()) {
            return virtMonitoringStrategy;
        }

        if (cluster.supportsGlusterService()) {
            return glusterMonitoringStrategy;
        }

        return virtMonitoringStrategy;
    }
}
