package org.ovirt.engine.core.dao.network;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.dao.FixturesTool;

public class HostNetworkStatisticsDaoTest extends NetworkStatisticsDaoTest<InterfaceDao, VdsNetworkStatistics> {

    @Override
    protected List<? extends NetworkInterface<VdsNetworkStatistics>> getAllInterfaces() {
        return dao.getAllInterfacesForVds(FixturesTool.VDS_RHEL6_NFS_SPM);
    }

    @Override
    protected void updateStatistics(VdsNetworkStatistics stats) {
        dao.updateStatisticsForVds(stats);
    }

    @Test
    public void testUpdateStatisticsWithValues() {
        testUpdateStatistics(999.0, new BigInteger("999"));
    }

    @Test
    public void testUpdateStatisticsNullValues() {
        testUpdateStatistics(null, null);
    }
}
