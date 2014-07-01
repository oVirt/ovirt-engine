package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;


public class VdsStaticDAOTest extends BaseDAOTestCase {
    private static final String IP_ADDRESS = "192.168.122.17";
    private VdsStaticDAO dao;
    private VdsDynamicDAO dynamicDao;
    private VdsStatisticsDAO statisticsDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVdsStaticDao();
        dynamicDao = dbFacade.getVdsDynamicDao();
        statisticsDao = dbFacade.getVdsStatisticsDao();
        existingVds = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setSshPort(22);
        newStaticVds.setSshUsername("root");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newStaticVds.setSshKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setPmSecondaryOptionsMap(new HashMap<String, String>());
        newStaticVds.setProtocol(VdsProtocol.STOMP);
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsStatic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right object is returned.
     */
    @Test
    public void testGet() {
        VdsStatic result = dao.get(existingVds.getId());

        assertNotNull(result);
        assertEquals(existingVds.getId(), result.getId());
    }

    /**
     * Ensures all the right VdsStatic instances are returned.
     */
    @Test
    public void testGetByHostName() {
        VdsStatic vds = dao.getByHostName(existingVds
                .getHostName());

        assertNotNull(vds);
        assertEquals(existingVds.getHostName(), vds.getHostName());
    }

    /**
     * Ensures the right set of VdsStatic instances are returned.
     */
    @Test
    public void testGetAllWithIpAddress() {
        List<VdsStatic> result = dao.getAllWithIpAddress(IP_ADDRESS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures all the right set of VdsStatic instances are returned.
     */
    @Test
    public void testGetAllForVdsGroup() {
        List<VdsStatic> result = dao.getAllForVdsGroup(existingVds
                .getVdsGroupId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsStatic vds : result) {
            assertEquals(existingVds.getVdsGroupId(), vds.getVdsGroupId());
        }
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        dao.save(newStaticVds);

        VdsStatic staticResult = dao.get(newStaticVds.getId());

        assertNotNull(staticResult);
        assertEquals(newStaticVds, staticResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        statisticsDao.remove(existingVds.getId());
        dynamicDao.remove(existingVds.getId());
        dao.remove(existingVds.getId());

        VdsStatic resultStatic = dao.get(existingVds.getId());
        assertNull(resultStatic);
        VdsDynamic resultDynamic = dynamicDao.get(existingVds.getId());
        assertNull(resultDynamic);
        VdsStatistics resultStatistics = statisticsDao.get(existingVds.getId());
        assertNull(resultStatistics);
    }

}
