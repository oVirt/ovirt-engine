package org.ovirt.engine.core.bll.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileType;
import org.ovirt.engine.core.compat.Guid;

public class CpuProfileHelperTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String CLUSTER_NAME = "ClusterName";

    @Test
    public void createCpuProfileTest() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(CLUSTER_ID, CLUSTER_NAME);
        assertEquals(cpuProfile.getName(), CLUSTER_NAME);
        assertEquals(cpuProfile.getClusterId(), CLUSTER_ID);
        assertNotNull(cpuProfile.getId());
        assertEquals(cpuProfile.getProfileType(), ProfileType.CPU);
    }
}
