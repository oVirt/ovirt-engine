package org.ovirt.engine.core.dao.profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class CpuProfileDaoTest extends BaseGenericDaoTestCase<Guid, CpuProfile, CpuProfileDao> {
    @Override
    protected CpuProfile generateNewEntity() {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName("new_profile");
        cpuProfile.setClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI);
        cpuProfile.setQosId(FixturesTool.QOS_ID_4);
        return cpuProfile;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setQosId(FixturesTool.QOS_ID_5);
        existingEntity.setDescription("desc1");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.CPU_PROFILE_2;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 6;
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForClusterEmpty() {
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
