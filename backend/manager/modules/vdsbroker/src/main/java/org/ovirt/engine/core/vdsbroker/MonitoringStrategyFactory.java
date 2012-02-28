package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * This class is responsible for returning the correct service strategy, according to the service the cluster supports
 */
public class MonitoringStrategyFactory {
    private static MonitoringStrategy virtMonitoringStrategy = new VirtMonitoringStrategy();

    /**
     * This method gets the VDS, and returns the correct service strategy, according to the service the cluster that the VDS belongs to supports
     */
    public static MonitoringStrategy getMonitoringStrategyForVds(VDS vds) {
        // In here we will test if the cluster is a gluster cluster, or a virt one, and return the strategy accordingly
        // In the meantime we return the virt one
        return virtMonitoringStrategy;
    }
}
