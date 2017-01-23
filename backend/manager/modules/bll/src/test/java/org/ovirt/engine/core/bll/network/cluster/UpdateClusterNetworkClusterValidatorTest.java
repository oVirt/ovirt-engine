package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(Strict.class)
public class UpdateClusterNetworkClusterValidatorTest extends
        NetworkClusterValidatorTestBase<UpdateClusterNetworkClusterValidator> {

    @Override
    protected UpdateClusterNetworkClusterValidator createValidator() {
        return new UpdateClusterNetworkClusterValidator(interfaceDao, networkDao, vdsDao, networkCluster);
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
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                Collections.emptyList() :
                Collections.singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }
}
