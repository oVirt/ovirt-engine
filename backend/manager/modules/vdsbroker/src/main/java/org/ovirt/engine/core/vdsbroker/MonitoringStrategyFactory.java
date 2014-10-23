package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * This class is responsible for returning the correct service strategy, according to the service the cluster supports
 */
public class MonitoringStrategyFactory {
    private static MonitoringStrategy virtMonitoringStrategy = new VirtMonitoringStrategy(DbFacade.getInstance().getVdsGroupDao(), DbFacade.getInstance().getVdsDao());
    private static MonitoringStrategy glusterMonitoringStrategy = new GlusterMonitoringStrategy();
    private static MultipleServicesMonitoringStrategy multipleMonitoringStrategy = new MultipleServicesMonitoringStrategy();

    static {
        multipleMonitoringStrategy.addMonitoringStrategy(virtMonitoringStrategy);
        multipleMonitoringStrategy.addMonitoringStrategy(glusterMonitoringStrategy);
    }

    private static VdsGroupDAO vdsGroupDao = DbFacade.getInstance().getVdsGroupDao();

    /**
     * This method gets the VDS, and returns the correct service strategy, according to the service the cluster that the VDS belongs to supports
     */
    public static MonitoringStrategy getMonitoringStrategyForVds(VDS vds) {
        MonitoringStrategy returnedStrategy = virtMonitoringStrategy;
        Guid vdsGroupId = vds.getVdsGroupId();
        VDSGroup vdsGroup = vdsGroupDao.get(vdsGroupId);

        if (vdsGroup.supportsVirtService() && vdsGroup.supportsGlusterService()) {
            returnedStrategy = multipleMonitoringStrategy;
        } else if (vdsGroup.supportsVirtService()) {
            returnedStrategy = virtMonitoringStrategy;
        } else if (vdsGroup.supportsGlusterService()) {
            returnedStrategy = glusterMonitoringStrategy;
        }

        return returnedStrategy;
    }
}
