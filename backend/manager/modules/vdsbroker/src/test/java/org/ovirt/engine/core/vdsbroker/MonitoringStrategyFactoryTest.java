package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;

public class MonitoringStrategyFactoryTest {

    @Test
    public void testMonitoringStrategyFactoryVirtStrategy() {
        VDS vds = new VDS();
        MonitoringStrategy monitoringStrategy = MonitoringStrategyFactory.getMonitoringStrategyForVds(vds);
        assertTrue(monitoringStrategy instanceof VirtMonitoringStrategy);
    }

}
