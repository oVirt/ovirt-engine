package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.utils.RandomUtils;

public class VdsDynamicDaoTest extends BaseGenericDaoTestCase<Guid, VdsDynamic, VdsDynamicDao> {
    private static final List<Guid> HOSTS_WITH_UP_STATUS =
            Arrays.asList(FixturesTool.VDS_RHEL6_NFS_SPM,
                    FixturesTool.HOST_ID,
                    FixturesTool.HOST_WITH_NO_VFS_CONFIGS_ID,
                    FixturesTool.GLUSTER_BRICK_SERVER1,
                    FixturesTool.VDS_GLUSTER_SERVER2);

    @Override
    protected VdsDynamic generateNewEntity() {
        VdsDynamic newDynamicVds = new VdsDynamic();
        newDynamicVds.setId(FixturesTool.VDS_JUST_STATIC_ID);
        newDynamicVds.setUpdateAvailable(true);
        return newDynamicVds;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setGlusterVersion(new RpmVersion("glusterfs-3.4.0.34.1u2rhs-1.el6rhs"));
        existingEntity.setLibrbdVersion(new RpmVersion("librbd1-0.80.9-1.fc21.x86_64_updated"));
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VDS_GLUSTER_SERVER2;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 5;
    }

    @Disabled
    @Override
    public void testGetAll() {
        // Not Supported
    }

    @Test
    public void testUpdateStatus() {
        VdsDynamic before = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        before.setStatus(VDSStatus.Down);
        dao.updateStatus(before.getId(), before.getStatus());
        VdsDynamic after = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertEquals(before, after);
    }

    @Test
    public void testUpdateStatusAndReasons() {
        VdsDynamic before = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        before.setStatus(RandomUtils.instance().nextEnum(VDSStatus.class));
        before.setNonOperationalReason(RandomUtils.instance().nextEnum(NonOperationalReason.class));
        before.setMaintenanceReason(RandomUtils.instance().nextString(50));
        dao.updateStatusAndReasons(before);
        VdsDynamic after = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertEquals(before, after);
        assertEquals(before.getStatus(), after.getStatus());
        assertEquals(before.getNonOperationalReason(), after.getNonOperationalReason());
        assertEquals(before.getMaintenanceReason(), after.getMaintenanceReason());
    }

    @Test
    public void testUpdateHostExternalStatus() {
        VdsDynamic before = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        before.setExternalStatus(ExternalStatus.Error);
        dao.updateExternalStatus(before.getId(), before.getExternalStatus());
        VdsDynamic after = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertEquals(before.getExternalStatus(), after.getExternalStatus());
    }

    @Test
    public void testUpdateNetConfigDirty() {
        VdsDynamic before = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        Boolean netConfigDirty = before.getNetConfigDirty();
        netConfigDirty = Boolean.FALSE.equals(netConfigDirty);
        before.setNetConfigDirty(netConfigDirty);
        dao.updateNetConfigDirty(before.getId(), netConfigDirty);
        VdsDynamic after = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertEquals(before, after);
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
        VdsDynamic before = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertFalse(before.isUpdateAvailable());
        before.setUpdateAvailable(true);
        dao.updateUpdateAvailable(before.getId(), before.isUpdateAvailable());
        VdsDynamic after = dao.get(FixturesTool.VDS_GLUSTER_SERVER2);
        assertEquals(before.isUpdateAvailable(), after.isUpdateAvailable());
    }

    @Test
    public void testCheckIfExistsHostWithStatusInCluster() {
        boolean resultBeforeUpdateStatus =
                dao.checkIfExistsHostWithStatusInCluster(FixturesTool.GLUSTER_CLUSTER_ID, VDSStatus.Up);
        assertTrue(resultBeforeUpdateStatus);

        boolean resultAfterUpdateStatus =
                dao.checkIfExistsHostWithStatusInCluster(FixturesTool.GLUSTER_CLUSTER_ID, VDSStatus.Connecting);
        assertFalse(resultAfterUpdateStatus);
    }
}
