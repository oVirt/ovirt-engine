package org.ovirt.engine.core.bll.profiles;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class CpuProfileHelperTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String CLUSTER_NAME = "ClusterName";

    private static final Guid USER_1_ID = Guid.newGuid();
    private static final Guid USER_2_ID = Guid.newGuid();

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.CpuQosSupported, Version.getLast().getValue(), true));

    @Mock
    private CpuProfileDao cpuProfileDao;

    @Mock
    private PermissionDao permissionDao;

    @InjectMocks
    private CpuProfileHelper cpuProfileHelper;

    private CpuProfile cpuProfile1 = new CpuProfile();
    private CpuProfile cpuProfile2 = new CpuProfile();

    @Before
    public void setUp() {
        cpuProfile1.setId(Guid.newGuid());
        cpuProfile1.setClusterId(CLUSTER_ID);
        cpuProfile1.setName("CpuProfile 1");

        cpuProfile2.setId(Guid.newGuid());
        cpuProfile2.setClusterId(CLUSTER_ID);
        cpuProfile2.setName("CpuProfile 2");

        when(cpuProfileDao.get(cpuProfile1.getId())).thenReturn(cpuProfile1);
        when(cpuProfileDao.get(cpuProfile2.getId())).thenReturn(cpuProfile2);
        when(cpuProfileDao.getAllForCluster(CLUSTER_ID)).thenReturn(Arrays.asList(cpuProfile1, cpuProfile2));

        when(permissionDao.getEntityPermissions(
                USER_1_ID, ActionGroup.ASSIGN_CPU_PROFILE, cpuProfile1.getId(), VdcObjectType.CpuProfile))
                .thenReturn(Guid.newGuid());

        when(permissionDao.getEntityPermissions(
                USER_2_ID, ActionGroup.ASSIGN_CPU_PROFILE, cpuProfile2.getId(), VdcObjectType.CpuProfile))
                .thenReturn(Guid.newGuid());
    }

    @Test
    public void createCpuProfileTest() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(CLUSTER_ID, CLUSTER_NAME);
        assertEquals(cpuProfile.getName(), CLUSTER_NAME);
        assertEquals(cpuProfile.getClusterId(), CLUSTER_ID);
        assertNotNull(cpuProfile.getId());
        assertEquals(cpuProfile.getProfileType(), ProfileType.CPU);
    }

    @Test
    public void testNullClusterId() {
        VmBase vmBase = createVmBase(cpuProfile1.getId());
        vmBase.setVdsGroupId(null);

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, Version.getLast(), USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_CPU_PROFILE_CLUSTER_NOT_PROVIDED));
    }

    @Test
    public void testNonExistingCpuProfile() {
        VmBase vmBase = createVmBase(Guid.newGuid());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, Version.getLast(), USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_FAILED_CPU_PROFILE_NOT_FOUND));
    }

    @Test
    public void testDifferentClusters() {
        VmBase vmBase = createVmBase(cpuProfile1.getId());
        vmBase.setVdsGroupId(Guid.newGuid());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, Version.getLast(), USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_CPU_PROFILE_NOT_MATCH_CLUSTER));
    }

    @Test
    public void testNoPermission() {
        VmBase vmBase = createVmBase(cpuProfile2.getId());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, Version.getLast(), USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_NO_PERMISSION_TO_ASSIGN_CPU_PROFILE));
    }

    private VmBase createVmBase(Guid cpuProfileId) {
        VmBase vmBase = new VmBase();
        vmBase.setId(Guid.newGuid());
        vmBase.setVdsGroupId(CLUSTER_ID);
        vmBase.setCpuProfileId(cpuProfileId);
        return vmBase;
    }
}
