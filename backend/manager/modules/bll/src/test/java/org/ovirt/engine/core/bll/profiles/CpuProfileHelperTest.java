package org.ovirt.engine.core.bll.profiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CpuProfileHelperTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String CLUSTER_NAME = "ClusterName";

    private static final Guid USER_1_ID = Guid.newGuid();

    @Mock
    private CpuProfileDao cpuProfileDao;

    @Mock
    private PermissionDao permissionDao;

    @InjectMocks
    private CpuProfileHelper cpuProfileHelper;

    private CpuProfile cpuProfile1 = new CpuProfile();
    private CpuProfile cpuProfile2 = new CpuProfile();

    @BeforeEach
    public void setUp() {
        cpuProfile1.setId(Guid.newGuid());
        cpuProfile1.setClusterId(CLUSTER_ID);
        cpuProfile1.setName("CpuProfile 1");

        cpuProfile2.setId(Guid.newGuid());
        cpuProfile2.setClusterId(CLUSTER_ID);
        cpuProfile2.setName("CpuProfile 2");

        when(cpuProfileDao.get(cpuProfile1.getId())).thenReturn(cpuProfile1);
        when(cpuProfileDao.get(cpuProfile2.getId())).thenReturn(cpuProfile2);
    }

    @Test
    public void createCpuProfileTest() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(CLUSTER_ID, CLUSTER_NAME);
        assertEquals(CLUSTER_NAME, cpuProfile.getName());
        assertEquals(CLUSTER_ID, cpuProfile.getClusterId());
        assertNotNull(cpuProfile.getId());
        assertEquals(ProfileType.CPU, cpuProfile.getProfileType());
    }

    @Test
    public void testNullClusterId() {
        VmBase vmBase = createVmBase(cpuProfile1.getId());
        vmBase.setClusterId(null);

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_CPU_PROFILE_CLUSTER_NOT_PROVIDED));
    }

    @Test
    public void testNonExistingCpuProfile() {
        VmBase vmBase = createVmBase(Guid.newGuid());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_FAILED_CPU_PROFILE_NOT_FOUND));
    }

    @Test
    public void testDifferentClusters() {
        VmBase vmBase = createVmBase(cpuProfile1.getId());
        vmBase.setClusterId(Guid.newGuid());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_CPU_PROFILE_NOT_MATCH_CLUSTER));
    }

    @Test
    public void testNoPermission() {
        VmBase vmBase = createVmBase(cpuProfile2.getId());

        ValidationResult res =  cpuProfileHelper.setAndValidateCpuProfile(vmBase, USER_1_ID);
        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_NO_PERMISSION_TO_ASSIGN_CPU_PROFILE));
    }

    private VmBase createVmBase(Guid cpuProfileId) {
        VmBase vmBase = new VmBase();
        vmBase.setId(Guid.newGuid());
        vmBase.setClusterId(CLUSTER_ID);
        vmBase.setCpuProfileId(cpuProfileId);
        return vmBase;
    }
}
