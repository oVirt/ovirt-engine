package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

@RunWith(MockitoJUnitRunner.class)
public class UpdateNetworkClusterValidatorTest extends NetworkClusterValidatorTestBase<UpdateNetworkClusterValidator> {

    @Before
    public void prepareNetworkClusterExpects() {
        when(networkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
    }

    @Override
    protected UpdateNetworkClusterValidator createValidator() {
        return new UpdateNetworkClusterValidator(networkCluster, oldNetworkCluster, version);
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
        testManagementNetworkUnset(true, false, failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_UNSET));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED));
    }

    private void testUpdateManagementNetworkChange(boolean managementBefore,
                                                   boolean managementAfter,
                                                   boolean emptyCluster,
                                                   Matcher<ValidationResult> expectedResult) {
        when(oldNetworkCluster.isManagement()).thenReturn(managementBefore);
        when(networkCluster.isManagement()).thenReturn(managementAfter);
        when(vdsDao.getAllForVdsGroup(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                                                                               Collections.<VDS> emptyList() :
                                                                               Collections.<VDS> singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }
}
