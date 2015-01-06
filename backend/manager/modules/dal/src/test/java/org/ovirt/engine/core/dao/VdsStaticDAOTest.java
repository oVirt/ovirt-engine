package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;


public class VdsStaticDAOTest extends BaseHibernateDaoTestCase<VdsStaticDAO, VdsStatic, Guid> {
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
        newStaticVds.setId(Guid.newGuid());
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setSshPort(22);
        newStaticVds.setSshUsername("root");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newStaticVds.setSshKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setProtocol(VdsProtocol.STOMP);
        newStaticVds.setPort(54321);
        newStaticVds.setName("farkle");
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

    @Override
    protected VdsStaticDAO getDao() {
        return dao;
    }

    @Override
    protected VdsStatic getExistingEntity() {
        return existingVds;
    }

    @Override
    protected VdsStatic getNonExistentEntity() {
        return newStaticVds;
    }

    @Override
    protected int getAllEntitiesCount() {
        return 5;
    }

    @Override
    protected VdsStatic modifyEntity(VdsStatic entity) {
        entity.setName("test");
        return entity;
    }

    @Override
    protected void verifyEntityModification(VdsStatic result) {
        assertEquals("test", result.getName());
    }
}
