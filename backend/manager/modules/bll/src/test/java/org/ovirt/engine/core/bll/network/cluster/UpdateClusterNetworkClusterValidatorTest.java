package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class UpdateClusterNetworkClusterValidatorTest extends
        NetworkClusterValidatorTestBase<UpdateClusterNetworkClusterValidator> {

    @Override
    protected UpdateClusterNetworkClusterValidator createValidator() {
        return new UpdateClusterNetworkClusterValidator(interfaceDao, networkDao, networkCluster);
    }

    @Test
    public void managementNetworkChangeValid() {
        testUpdateManagementNetworkChange(true, isValid());
    }

    @Test
    public void managementNetworkChangeInvalid() {
        testUpdateManagementNetworkChange(false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED));
    }

    private void testUpdateManagementNetworkChange(boolean emptyCluster,
                                                   Matcher<ValidationResult> expectedResult) {
        when(networkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                Collections.<VDS>emptyList() :
                Collections.<VDS>singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }
}
