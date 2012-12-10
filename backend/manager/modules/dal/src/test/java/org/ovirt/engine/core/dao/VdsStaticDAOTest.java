package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.NGuid;


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
        dao = prepareDAO(dbFacade.getVdsStaticDao());
        dynamicDao = prepareDAO(dbFacade.getVdsDynamicDao());
        statisticsDao = prepareDAO(dbFacade.getVdsStatisticsDao());
        existingVds = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        newStaticVds = new VdsStatic();
        newStaticVds.sethost_name("farkle.redhat.com");
        newStaticVds.setvds_group_id(existingVds.getvds_group_id());
        newStaticVds.setSSHKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setPmSecondaryOptionsMap(new ValueObjectMap());
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsStatic result = dao.get(NGuid.NewGuid());

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
     * Ensures null is returned when the name is invalid.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        VdsStatic result = dao.get("farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving by name works.
     */
    @Test
    public void testGetByName() {
        VdsStatic result = dao.get(existingVds.getvds_name());

        assertNotNull(result);
        assertEquals(existingVds.getvds_name(), result.getvds_name());
    }


    /**
     * Ensures all the right VdsStatic instances are returned.
     */
    @Test
    public void testGetAllForHost() {
        List<VdsStatic> result = dao.getAllForHost(existingVds
                .gethost_name());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsStatic vds : result) {
            assertEquals(existingVds.gethost_name(), vds.gethost_name());
        }
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
                .getvds_group_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VdsStatic vds : result) {
            assertEquals(existingVds.getvds_group_id(), vds.getvds_group_id());
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
