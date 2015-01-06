package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatisticsDAOTest extends BaseHibernateDaoTestCase<VdsStatisticsDAO, VdsStatistics, Guid> {
    private VdsStatisticsDAO dao;
    private VdsStaticDAO staticDao;
    private VdsDynamicDAO dynamicDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;
    private VdsStatistics newStatistics;
    private VdsStatistics existingEntity;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsStatisticsDao();
        staticDao = dbFacade.getVdsStaticDao();
        dynamicDao =  dbFacade.getVdsDynamicDao();
        existingVds = staticDao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newStaticVds.setProtocol(VdsProtocol.STOMP);
        newStaticVds.setSshPort(22);
        newStaticVds.setSshUsername("root");
        newStaticVds.setSshKeyFingerprint("b5:ad:16:19:06:9f:b3:41:69:eb:1c:42:1d:12:b5:31");
        newStaticVds.setPort(54321);
        newStaticVds.setName("farkle");
        newStatistics = new VdsStatistics();
        newStatistics.setId(Guid.newGuid());
        existingEntity = dao.get(existingVds.getId());
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

    @Override
    protected VdsStatisticsDAO getDao() {
        return dao;
    }

    @Override
    protected VdsStatistics getExistingEntity() {
        return existingEntity;
    }

    @Override
    protected VdsStatistics getNonExistentEntity() {
        return newStatistics;
    }

    @Override
    protected int getAllEntitiesCount() {
        return 5;
    }

    @Override
    protected VdsStatistics modifyEntity(VdsStatistics entity) {
        entity.setHighlyAvailableIsActive(true);
        return entity;
    }

    @Override
    protected void verifyEntityModification(VdsStatistics result) {
        assertEquals(true, result.getHighlyAvailableIsActive());
    }
}
