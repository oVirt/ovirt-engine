package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.utils.RandomUtils;

public class VdsDynamicDAOTest extends BaseHibernateDaoTestCase<VdsDynamicDAO, VdsDynamic, Guid> {
    private static final String TEST_BUILD_NAME = "test1";
    private VdsDynamicDAO dao;
    private VdsStaticDAO staticDao;
    private VdsStatisticsDAO statisticsDao;
    private VdsStatic existingVds;
    private VdsStatic newStaticVds;
    private VdsDynamic newEntity;
    private VdsDynamic existingEntity;

    private static final List<Guid> HOSTS_WITH_UP_STATUS =
            new ArrayList<>(Arrays.asList(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"),
                    new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"),
                    new Guid("afce7a39-8e8c-4819-ba9c-796d316592e8"),
                    new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3"),
                    new Guid("2001751e-549b-4e7a-aff6-32d36856c125")));

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVdsDynamicDao();
        staticDao = dbFacade.getVdsStaticDao();
        statisticsDao = dbFacade.getVdsStatisticsDao();
        existingVds = staticDao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        existingEntity = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);

        newStaticVds = new VdsStatic();
        newStaticVds.setHostName("farkle.redhat.com");
        newStaticVds.setVdsGroupId(existingVds.getVdsGroupId());
        newStaticVds.setProtocol(VdsProtocol.STOMP);

        newEntity = new VdsDynamic();
        newEntity.setId(Guid.newGuid());
    }

    /**
     * Ensures that an invalid id returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VdsDynamic result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    @Test
    public void testUpdateIfNeeded() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setStatus(VDSStatus.Down);
        dao.updateIfNeeded(before);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

    @Test
    public void testUpdateStatus() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setStatus(VDSStatus.Down);
        dao.updateStatus(before.getId(), before.getStatus());
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

    @Test
    public void testUpdateStatusAndReasons() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setStatus(RandomUtils.instance().nextEnum(VDSStatus.class));
        before.setNonOperationalReason(RandomUtils.instance().nextEnum(NonOperationalReason.class));
        before.setMaintenanceReason(RandomUtils.instance().nextString(50));
        dao.updateStatusAndReasons(before);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
        assertEquals(before.getStatus(), after.getStatus());
        assertEquals(before.getNonOperationalReason(), after.getNonOperationalReason());
        assertEquals(before.getMaintenanceReason(), after.getMaintenanceReason());
    }

    @Test
    public void testUpdateHostExternalStatus() {
        VdsDynamic before = dao.get(existingVds.getId());
        before.setExternalStatus(ExternalStatus.Error);
        dao.updateExternalStatus(before.getId(), before.getExternalStatus());
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before.getExternalStatus(), after.getExternalStatus());
    }

    @Test
    public void testUpdateNetConfigDirty() {
        VdsDynamic before = dao.get(existingVds.getId());
        Boolean netConfigDirty = before.getNetConfigDirty();
        netConfigDirty = Boolean.FALSE.equals(netConfigDirty);
        before.setNetConfigDirty(netConfigDirty);
        dao.updateNetConfigDirty(before.getId(), netConfigDirty);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before, after);
    }

    @Test
    public void testGlusterVersion() {
        RpmVersion glusterVersion = new RpmVersion("glusterfs-3.4.0.34.1u2rhs-1.el6rhs");
        VdsDynamic before = dao.get(existingVds.getId());
        before.setGlusterVersion(glusterVersion);
        dao.update(before);
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(glusterVersion, after.getGlusterVersion());
    }

    @Test
    public void testGetIdsOfHostsWithStatus() {
        List<Guid> hostIds = dao.getIdsOfHostsWithStatus(VDSStatus.Up);
        assertEquals(5, hostIds.size());
        assertTrue(hostIds.containsAll(HOSTS_WITH_UP_STATUS));

        hostIds = dao.getIdsOfHostsWithStatus(VDSStatus.Maintenance);
        assertEquals(0, hostIds.size());
    }

    @Test
    public void testUpdateAvailableUpdates() {
        VdsDynamic before = dao.get(existingVds.getId());
        assertFalse(before.isUpdateAvailable());
        before.setUpdateAvailable(true);
        dao.updateUpdateAvailable(before.getId(), before.isUpdateAvailable());
        VdsDynamic after = dao.get(existingVds.getId());
        assertEquals(before.isUpdateAvailable(), after.isUpdateAvailable());
    }

        @Override
    protected VdsDynamicDAO getDao() {
        return dao;
    }

    @Override
    protected VdsDynamic getExistingEntity() {
        return existingEntity;
    }

    @Override
    protected VdsDynamic getNonExistentEntity() {
        return newEntity;
    }

    @Override
    protected int getAllEntitiesCount() {
        return 5;
    }

    @Override
    protected VdsDynamic modifyEntity(VdsDynamic entity) {
        entity.setBuildName(TEST_BUILD_NAME);
        return entity;
    }

    @Override
    protected void verifyEntityModification(VdsDynamic result) {
        assertEquals(TEST_BUILD_NAME, result.getBuildName());
    }
}
