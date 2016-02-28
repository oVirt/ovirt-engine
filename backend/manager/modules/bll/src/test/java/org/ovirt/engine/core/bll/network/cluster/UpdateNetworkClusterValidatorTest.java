package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

@RunWith(MockitoJUnitRunner.class)
public class UpdateNetworkClusterValidatorTest extends NetworkClusterValidatorTestBase<UpdateNetworkClusterValidator> {
    @Mock
    GlusterBrickDao brickDao;


    @Before
    public void prepareNetworkClusterExpects() {
        when(networkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
    }

    @Override
    protected UpdateNetworkClusterValidator createValidator() {
        return new UpdateNetworkClusterValidator(interfaceDao, networkDao, networkCluster, oldNetworkCluster);
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
        when(oldNetworkCluster.isManagement()).thenReturn(managementBefore);
        when(networkCluster.isManagement()).thenReturn(managementAfter);
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
        when(oldNetworkCluster.isManagement()).thenReturn(managementBefore);
        when(networkCluster.isManagement()).thenReturn(managementAfter);
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                                                                               Collections.<VDS> emptyList() :
                                                                               Collections.<VDS> singletonList(null));
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
        when(oldNetworkCluster.isGluster()).thenReturn(glusterNetworkBefore);
        when(networkCluster.isGluster()).thenReturn(glusterNetworkAfter);
        when(cluster.supportsGlusterService()).thenReturn(glusterService);
        doReturn(brickDao).when(validator).getGlusterBrickDao();
        when(brickDao.getAllByClusterAndNetworkId(any(Guid.class), any(Guid.class))).thenReturn(hasBricks ?
                Collections.<GlusterBrickEntity> singletonList(null) : Collections.<GlusterBrickEntity> emptyList());
        assertThat(validator.glusterNetworkInUseAndUnset(cluster), expectedResult);
    }
}
