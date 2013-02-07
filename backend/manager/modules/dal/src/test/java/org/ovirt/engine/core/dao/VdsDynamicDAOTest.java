package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.NGuid;

public class VdsDynamicDAOTest extends BaseDAOTestCase {
    private VdsDynamicDAO dao;
    private VdsStaticDAO staticDao;
    private VdsStatisticsDAO statisticsDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;
    private VdsDynamic newDynamicVds;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsDynamicDao();
        staticDao =  dbFacade.getVdsStaticDao();
        statisticsDao =  dbFacade.getVdsStatisticsDao();
        existingVds = staticDao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newDynamicVds = new VdsDynamic();
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsDynamic result = dao.get(NGuid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that the right object is returned.
     */
    @Test
    public void testGet() {
        VdsDynamic result = dao.get(existingVds.getId());

        assertNotNull(result);
        assertEquals(existingVds.getId(), result.getId());
    }

    /**
     * Ensures saving a VDS instance works.
     */
    @Test
    public void testSave() {
        staticDao.save(newStaticVds);
        newDynamicVds.setId(newStaticVds.getId());
        dao.save(newDynamicVds);

        VdsStatic staticResult = staticDao.get(newStaticVds.getId());
        VdsDynamic dynamicResult = dao.get(newDynamicVds.getId());

        assertNotNull(staticResult);
        assertEquals(newStaticVds, staticResult);
        assertNotNull(dynamicResult);
        assertEquals(newDynamicVds, dynamicResult);
    }

    /**
     * Ensures removing a VDS instance works.
     */
    @Test
    public void testRemove() {
        dao.remove(existingVds.getId());
        statisticsDao.remove(existingVds.getId());
        staticDao.remove(existingVds.getId());

        VdsStatic resultStatic = staticDao.get(existingVds.getId());
        assertNull(resultStatic);
        VdsDynamic resultDynamic = dao.get(existingVds.getId());
        assertNull(resultDynamic);
    }

    @Test
    public void testUpdateStatus() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setstatus(VDSStatus.Down);
        dao.updateStatus(before.getId(), before.getstatus());
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

}
