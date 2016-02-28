package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class AttachNetworkClusterValidatorTest extends NetworkClusterValidatorTestBase<AttachNetworkClusterValidator> {

    @Before
    public void prepareNetworkClusterExpects() {
        when(networkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
    }

    @Override
    protected AttachNetworkClusterValidator createValidator() {
        return new AttachNetworkClusterValidator(interfaceDao, networkDao, networkCluster);
    }

    @Test
    public void managementNetworkChangeValidNoChangeNonEmptyCluster() {
        testManagementNetworkChange(false, false, isValid());
    }

    @Test
    public void managementNetworkChangeValidNoChangeEmptyCluster() {
        testManagementNetworkChange(false, true, isValid());
    }

    @Test
    public void managementNetworkChangeValidEmptyCluster() {
        testManagementNetworkChange(true, true, isValid());
    }

    @Test
    public void managementNetworkChangeInvalidNonEmptyCluster() {
        testManagementNetworkChange(
                true,
                false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED));
    }

    private void testManagementNetworkChange(boolean managementAfter,
                                             boolean emptyCluster,
                                             Matcher<ValidationResult> expectedResult) {
        when(networkCluster.isManagement()).thenReturn(managementAfter);
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                                                                               Collections.<VDS> emptyList() :
                                                                               Collections.<VDS> singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }

}
