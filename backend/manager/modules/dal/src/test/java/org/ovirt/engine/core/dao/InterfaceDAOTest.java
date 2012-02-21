package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

public class InterfaceDAOTest extends BaseDAOTestCase {
    private static final Guid VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VDS_STATISTICS_ID = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9db");

    private InterfaceDAO dao;
    private VdsNetworkInterface newVdsInterface;
    private VdsNetworkStatistics newVdsStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getInterfaceDAO());

        newVdsInterface = new VdsNetworkInterface();
        newVdsInterface.setStatistics(new VdsNetworkStatistics());
        newVdsInterface.setId(Guid.NewGuid());
        newVdsInterface.setName("eth77");
        newVdsInterface.setNetworkName("enginet");
        newVdsInterface.setAddress("192.168.122.177");
        newVdsInterface.setSubnet("255.255.255.0");
        newVdsInterface.setSpeed(1000);
        newVdsInterface.setType(3);
        newVdsInterface.setBootProtocol(NetworkBootProtocol.StaticIp);
        newVdsInterface.setMacAddress("01:C0:81:21:71:17");
        newVdsInterface.setGateway("192.168.122.1");
        newVdsInterface.setMtu(1500);

        newVdsStatistics = newVdsInterface.getStatistics();
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVdsWithInvalidVds() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of interfaces are returned.
     */
    @Test
    public void testGetAllInterfacesForVds() {
        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsNetworkInterface iface : result) {
            assertEquals(VDS_ID, iface.getVdsId());
        }
    }

    /**
     * Ensures that saving an interface for a VDS works as expected.
     */
    @Test
    public void testSaveInterfaceForVds() {
        newVdsInterface.setVdsId(VDS_ID);

        dao.saveInterfaceForVds(newVdsInterface);
        dao.saveStatisticsForVds(newVdsStatistics);

        List<VdsNetworkInterface> result = dao.getAllInterfacesForVds(VDS_ID);
        boolean found = false;

        for (VdsNetworkInterface iface : result) {
            found |= iface.getName()
                    .equals(newVdsInterface.getName());
        }

        assertTrue(found);
    }

    /**
     * Ensures that the specified VDS's interfaces are deleted.
     */
    @Test
    public void testRemoveInterfacesForVds() {
        List<VdsNetworkInterface> before = dao.getAllInterfacesForVds(VDS_ID);

        // ensure we have records before the test
        boolean found = false;
        for (VdsNetworkInterface iface : before) {
            found |= (VDS_STATISTICS_ID.equals(iface.getId()));
        }
        assertTrue(found);

        dao.removeInterfaceFromVds(VDS_STATISTICS_ID);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);

        for (VdsNetworkInterface iface : after) {
            assertNotSame(VDS_STATISTICS_ID, iface.getId());
        }
    }

    /**
     * Ensures that all statistics are removed for the specified VDS.
     */
    @Test
    public void testRemoveStatisticsForVds() {
        List<VdsNetworkInterface> before = dao.getAllInterfacesForVds(VDS_ID);

        for (VdsNetworkInterface iface : before) {
            assertNotSame(0.0, iface.getStatistics().getTransmitRate());
            assertNotSame(0.0, iface.getStatistics().getReceiveRate());
            assertNotSame(0.0, iface.getStatistics().getReceiveDropRate());
            assertNotSame(0.0, iface.getStatistics().getReceiveDropRate());
        }
        dao.removeStatisticsForVds(VDS_STATISTICS_ID);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);

        for (VdsNetworkInterface iface : after) {
            assertEquals(0.0, iface.getStatistics().getTransmitRate(), 0.0001);
            assertEquals(0.0, iface.getStatistics().getReceiveRate(), 0.0001);
            assertEquals(0.0, iface.getStatistics().getReceiveDropRate(), 0.0001);
            assertEquals(0.0, iface.getStatistics().getReceiveDropRate(), 0.0001);
        }
    }

    /**
     * Ensures updating an interface works for VDS.
     */
    @Test
    public void testUpdateInterfaceForVds() {
        List<VdsNetworkInterface> before = dao.getAllInterfacesForVds(VDS_ID);
        VdsNetworkInterface iface = before.get(0);

        iface.setName(iface.getName().toUpperCase());

        dao.updateInterfaceForVds(iface);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);
        boolean found = false;

        for (VdsNetworkInterface ifaced : after) {
            found |= ifaced.getName().equals(iface.getName());
        }

        assertTrue(found);
    }

    /**
     * Ensures that updating statistics for an interface works as expected.
     */
    @Test
    public void testUpdateStatisticsForVds() {
        List<VdsNetworkInterface> before = dao.getAllInterfacesForVds(VDS_ID);
        VdsNetworkStatistics stats = before.get(0).getStatistics();

        stats.setReceiveDropRate(999.0);

        dao.updateStatisticsForVds(stats);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);
        boolean found = false;

        for (VdsNetworkInterface ifaced : after) {
            if (ifaced.getStatistics().getId().equals(stats.getId())) {
                found = true;
                assertEquals(stats.getReceiveDropRate(), ifaced.getStatistics().getReceiveDropRate());
            }
        }

        if (!found)
            fail("Did not find statistics which is bad.");
    }

    @Test
    public void testMasshUpdateStatisticsForVds() throws Exception {
        List<VdsNetworkInterface> interfaces = dao.getAllInterfacesForVds(VDS_ID);
        List<VdsNetworkStatistics> statistics = new ArrayList<VdsNetworkStatistics>(interfaces.size());
        for (VdsNetworkInterface iface : interfaces) {
            VdsNetworkStatistics stats = iface.getStatistics();
            stats.setReceiveDropRate(RandomUtils.instance().nextInt() * 1.0);
            stats.setStatus(RandomUtils.instance().nextEnum(InterfaceStatus.class));
            statistics.add(stats);
        }

        dao.massUpdateStatisticsForVds(statistics);

        List<VdsNetworkInterface> after = dao.getAllInterfacesForVds(VDS_ID);
        for (VdsNetworkInterface iface : after) {
            boolean found = false;
            for (VdsNetworkStatistics stats : statistics) {
                if (iface.getId().equals(stats.getId())) {
                    found = true;
                    assertEquals(stats.getReceiveDropRate(), iface.getStatistics().getReceiveDropRate());
                    assertEquals(stats.getStatus(), iface.getStatistics().getStatus());
                }
            }
            assertTrue(found);
        }
    }
}
