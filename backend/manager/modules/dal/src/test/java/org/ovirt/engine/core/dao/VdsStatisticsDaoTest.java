package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatisticsDaoTest extends BaseDaoTestCase {
    private VdsStatisticsDao dao;
    private VdsStatistics newStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsStatisticsDao();
        newStatistics = new VdsStatistics();
        newStatistics.setId(FixturesTool.VDS_JUST_STATIC_ID);

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
        VdsStatistics result = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        assertNotNull(result);
        assertEquals(FixturesTool.VDS_GLUSTER_SERVER2, result.getId());
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        dao.save(newStatistics);
        VdsStatistics statisticsResult = dao.get(newStatistics.getId());

        assertNotNull(statisticsResult);
        assertEquals(newStatistics, statisticsResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        dao.remove(FixturesTool.VDS_GLUSTER_SERVER2);

        VdsStatistics resultStatistics = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertNull(resultStatistics);
    }
}
