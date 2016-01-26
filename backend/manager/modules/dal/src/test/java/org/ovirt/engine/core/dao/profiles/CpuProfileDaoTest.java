package org.ovirt.engine.core.dao.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class CpuProfileDaoTest extends BaseDaoTestCase {

    private CpuProfile cpuProfile;
    private CpuProfileDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getCpuProfileDao();
        cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName("new_profile");
        cpuProfile.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        cpuProfile.setQosId(FixturesTool.QOS_ID_4);
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        CpuProfile result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the interface profile is returned.
     */
    @Test
    public void testGet() {
        CpuProfile result = dao.get(FixturesTool.CPU_PROFILE_1);

        assertNotNull(result);
        assertEquals(FixturesTool.CPU_PROFILE_1, result.getId());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForStorageEmpty() {
        List<CpuProfile> result = dao.getAllForCluster(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that profiles are returned.
     */
    @Test
    public void testGetAllForClusterFull() {
        checkResults(dao.getAllForCluster(FixturesTool.CLUSTER_RHEL6_ISCSI));
    }

    private void checkResults(List<CpuProfile> result) {
        assertNotNull(result);
        assertEquals(2, result.size());
        for (CpuProfile cpuProfile : result) {
            assertEquals(FixturesTool.CLUSTER_RHEL6_ISCSI, cpuProfile.getClusterId());
        }
    }

    @Test
    public void testGetAll() {
        List<CpuProfile> result = dao.getAll();

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    /**
     * Ensures that the save is working correctly
     */
    @Test
    public void testSave() {
        assertNull(dao.get(cpuProfile.getId()));
        dao.save(cpuProfile);
        CpuProfile result = dao.get(cpuProfile.getId());
        assertNotNull(result);
        assertEquals(cpuProfile, result);
    }

    /**
     * Ensures that the update is working correctly
     */
    @Test
    public void testUpdate() {
        CpuProfile profile = dao.get(FixturesTool.CPU_PROFILE_1);
        assertNotNull(profile);
        assertTrue(FixturesTool.QOS_ID_4.equals(profile.getQosId()));
        profile.setQosId(FixturesTool.QOS_ID_5);
        profile.setDescription("desc1");
        dao.update(profile);
        CpuProfile result = dao.get(profile.getId());
        assertNotNull(result);
        assertEquals(profile, result);
    }

    /**
     * Ensures that the remove is working correctly
     */
    @Test
    public void testRemove() {
        dao.save(cpuProfile);
        CpuProfile result = dao.get(cpuProfile.getId());
        assertNotNull(result);
        dao.remove(cpuProfile.getId());
        assertNull(dao.get(cpuProfile.getId()));
    }

    @Test
    public void testGetByQos() {
        List<CpuProfile> allForQos = dao.getAllForQos(FixturesTool.QOS_ID_4);
        assertNotNull(allForQos);
        assertEquals(2, allForQos.size());
        for (CpuProfile cpuProfile : allForQos) {
            assertEquals(FixturesTool.QOS_ID_4, cpuProfile.getQosId());
        }
    }

    @Test
    public void testGetFilteredByPermissions() {
        checkResults(dao.getAllForCluster(FixturesTool.CLUSTER_RHEL6_ISCSI, PRIVILEGED_USER_ID, true, ActionGroup.ASSIGN_CPU_PROFILE));
    }

    @Test
    public void testGetFilteredByPermissionsForUnprivilegedUser() {
        List<CpuProfile> result =
                dao.getAllForCluster(FixturesTool.CLUSTER_RHEL6_ISCSI, UNPRIVILEGED_USER_ID, true, ActionGroup.ASSIGN_CPU_PROFILE);
        assertTrue(result.isEmpty());
    }
}
