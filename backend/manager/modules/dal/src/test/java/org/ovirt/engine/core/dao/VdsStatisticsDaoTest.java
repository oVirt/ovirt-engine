package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatisticsDaoTest extends BaseDaoTestCase {
    private VdsStatisticsDao dao;
    private VdsStaticDao staticDao;
    private VdsDynamicDao dynamicDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;
    private VdsStatistics newStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsStatisticsDao();
        staticDao = dbFacade.getVdsStaticDao();
        dynamicDao =  dbFacade.getVdsDynamicDao();
        existingVds = staticDao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setClusterId(existingVds.getClusterId());
        newStaticVds.setProtocol(VdsProtocol.STOMP);
        newStatistics = new VdsStatistics();

    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsStatistics result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right object is returned.
     */
    @Test
    public void testGet() {
        VdsStatistics result = dao.get(existingVds.getId());

        assertNotNull(result);
        assertEquals(existingVds.getId(), result.getId());
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        staticDao.save(newStaticVds);
        newStatistics.setId(newStaticVds.getId());
        dao.save(newStatistics);

        VdsStatic staticResult = staticDao.get(newStaticVds.getId());
        VdsStatistics statisticsResult = dao.get(newStatistics.getId());

        assertNotNull(staticResult);
        assertEquals(newStaticVds, staticResult);
        assertNotNull(statisticsResult);
        assertEquals(newStatistics, statisticsResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        dao.remove(existingVds.getId());
        dynamicDao.remove(existingVds.getId());
        staticDao.remove(existingVds.getId());

        VdsStatic resultStatic = staticDao.get(existingVds.getId());
        assertNull(resultStatic);
        VdsStatistics resultStatistics = dao.get(existingVds.getId());
        assertNull(resultStatistics);
    }
}
