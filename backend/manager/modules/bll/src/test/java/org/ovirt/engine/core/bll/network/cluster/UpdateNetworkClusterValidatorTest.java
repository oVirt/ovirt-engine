package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateNetworkClusterValidatorTest extends NetworkClusterValidatorTestBase<UpdateNetworkClusterValidator> {

    private NetworkCluster oldNetworkCluster;

    @Mock
    private GlusterBrickDao brickDao;

    @Override
    protected UpdateNetworkClusterValidator createValidator() {
        oldNetworkCluster = new NetworkCluster();
        return new UpdateNetworkClusterValidator(
                interfaceDao, networkDao, vdsDao, brickDao, networkCluster, oldNetworkCluster);
    }

    @Test
    public void managementNetworkChangeValidManagementNoChangeNonEmptyCluster() {
        testUpdateManagementNetworkChange(true, true, false, isValid());
    }

    @Test
    public void managementNetworkChangeValidNoChangeNonEmptyCluster() {
        testUpdateManagementNetworkChange(false, false, false, isValid());
    }

    @Test
    public void managementNetworkChangeValidNoChangeEmptyCluster() {
        testUpdateManagementNetworkChange(false, false, true, isValid());
    }

    @Test
    public void managementNetworkChangeValidManagementNoChangeEmptyCluster() {
        testUpdateManagementNetworkChange(true, true, true, isValid());
    }

    @Test
    public void managementNetworkChangeValidEmptyClusterSetManagementNet() {
        testUpdateManagementNetworkChange(false, true, true, isValid());
    }

    @Test
    public void managementNetworkChangeUnsetManagementNet() {
        testUpdateManagementNetworkChange(true, false, false, isValid());
    }

    @Test
    public void managementNetworkUnsetInvalid() {
        testManagementNetworkUnset(true, false, failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_UNSET));
    }

    @Test
    public void managementNetworkUnsetValidNonManagementNoChange() {
        testManagementNetworkUnset(false, false, isValid());
    }

    @Test
    public void managementNetworkUnsetValidManagementNoChange() {
        testManagementNetworkUnset(true, true, isValid());
    }

    @Test
    public void managementNetworkUnsetValidBecomeManagement() {
        testManagementNetworkUnset(false, true, isValid());
    }

    private void testManagementNetworkUnset(boolean managementBefore,
                                            boolean managementAfter,
                                            Matcher<ValidationResult> expectedResult) {
        oldNetworkCluster.setManagement(managementBefore);
        networkCluster.setManagement(managementAfter);
        assertThat(validator.managementNetworkUnset(), expectedResult);
    }

    @Test
    public void managementNetworkChangeInvalidNonEmptyClusterSetManagementNet() {
        testUpdateManagementNetworkChange(
                false,
                true,
                false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED));
    }

    private void testUpdateManagementNetworkChange(boolean managementBefore,
                                                   boolean managementAfter,
                                                   boolean emptyCluster,
                                                   Matcher<ValidationResult> expectedResult) {
        oldNetworkCluster.setManagement(managementBefore);
        networkCluster.setManagement(managementAfter);
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                                                                               Collections.emptyList() :
                                                                               Collections.singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }

    @Test
    public void glusterNetworkWhenNoGlusterService() {
        testGlusterNetworkInUseAndUnset(true, true, false, true, isValid());
    }

    @Test
    public void glusterNetworkWhenGlusterServiceNoChange() {
        testGlusterNetworkInUseAndUnset(true, true, true, true, isValid());
    }

    @Test
    public void glusterNetworkWhenGlusterServiceChangeNoBricks() {
        testGlusterNetworkInUseAndUnset(true, false, true, false, isValid());
    }

    @Test
    public void glusterNetworkWhenGlusterServiceChangeWithBricks() {
        testGlusterNetworkInUseAndUnset(true,
                false,
                true,
                true,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_NETWORK_INUSE));
    }

    private void testGlusterNetworkInUseAndUnset(boolean glusterNetworkBefore,
            boolean glusterNetworkAfter,
            boolean glusterService,
            boolean hasBricks,
            Matcher<ValidationResult> expectedResult) {
        oldNetworkCluster.setGluster(glusterNetworkBefore);
        networkCluster.setGluster(glusterNetworkAfter);
        cluster.setGlusterService(glusterService);
        when(brickDao.getAllByClusterAndNetworkId(any(), any())).thenReturn(hasBricks ?
                Collections.singletonList(null) : Collections.emptyList());
        assertThat(validator.glusterNetworkInUseAndUnset(cluster), expectedResult);
    }
}
