package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;

public class MultipleServicesMonitoringStrategy implements MonitoringStrategy {

    List<MonitoringStrategy> strategies = new ArrayList<>();

    public MultipleServicesMonitoringStrategy() {
    }

    public void addMonitoringStrategy( MonitoringStrategy monitoringStrategy ) {
        strategies.add(monitoringStrategy);
    }

    @Override
    public void processHardwareCapabilities(VDS vds) {
        for ( MonitoringStrategy monitoringStrategy : strategies ) {
            monitoringStrategy.processHardwareCapabilities(vds);
        }
    }

    @Override
    public void processSoftwareCapabilities(VDS vds) {
        for ( MonitoringStrategy monitoringStrategy : strategies ) {
            monitoringStrategy.processSoftwareCapabilities(vds);
        }
    }

    @Override
    public boolean canMoveToMaintenance(VDS vds) {
        // In this case, if all the services can move the VDS to maintenance then we return true
        for ( MonitoringStrategy monitoringStrategy : strategies ) {
            if (!monitoringStrategy.canMoveToMaintenance(vds)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMonitoringNeeded(VDS vds) {
        // In this case, if one of the services needs monitoring then we return true
        for ( MonitoringStrategy monitoringStrategy : strategies ) {
            if (monitoringStrategy.isMonitoringNeeded(vds)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean processHardwareCapabilitiesNeeded(VDS oldVds, VDS newVds) {
        // In this case, if one of the services needs hardware capabilities processing then we return true
        for ( MonitoringStrategy monitoringStrategy : strategies ) {
            if (monitoringStrategy.processHardwareCapabilitiesNeeded(oldVds, newVds)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPowerManagementSupported() {
        for (MonitoringStrategy monitoringStrategy : strategies) {
            if (monitoringStrategy.isPowerManagementSupported()) {
                return true;
            }
        }
        return false;
    }

}
