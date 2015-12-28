package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;

/**
 * This class is responsible for returning the correct service strategy, according to the service the cluster supports
 */
public class MonitoringStrategyFactory {
    private static MonitoringStrategy virtMonitoringStrategy = new VirtMonitoringStrategy(DbFacade.getInstance().getClusterDao(), DbFacade.getInstance().getVdsDao());
    private static MonitoringStrategy glusterMonitoringStrategy = new GlusterMonitoringStrategy();
    private static MultipleServicesMonitoringStrategy multipleMonitoringStrategy = new MultipleServicesMonitoringStrategy();

    static {
        multipleMonitoringStrategy.addMonitoringStrategy(virtMonitoringStrategy);
        multipleMonitoringStrategy.addMonitoringStrategy(glusterMonitoringStrategy);
    }

    private static ClusterDao clusterDao = DbFacade.getInstance().getClusterDao();

    /**
     * This method gets the VDS, and returns the correct service strategy, according to the service the cluster that the VDS belongs to supports
     */
    public static MonitoringStrategy getMonitoringStrategyForVds(VDS vds) {
        MonitoringStrategy returnedStrategy = virtMonitoringStrategy;
        Guid clusterId = vds.getClusterId();
        Cluster cluster = clusterDao.get(clusterId);

        if (cluster.supportsVirtService() && cluster.supportsGlusterService()) {
            returnedStrategy = multipleMonitoringStrategy;
        } else if (cluster.supportsVirtService()) {
            returnedStrategy = virtMonitoringStrategy;
        } else if (cluster.supportsGlusterService()) {
            returnedStrategy = glusterMonitoringStrategy;
        }

        return returnedStrategy;
    }
}
