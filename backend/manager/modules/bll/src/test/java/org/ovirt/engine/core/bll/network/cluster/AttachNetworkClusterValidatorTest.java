package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AttachNetworkClusterValidatorTest extends NetworkClusterValidatorTestBase<AttachNetworkClusterValidator> {

    @Override
    protected AttachNetworkClusterValidator createValidator() {
        return new AttachNetworkClusterValidator(interfaceDao, networkDao, vdsDao, networkCluster);
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
        networkCluster.setManagement(managementAfter);
        when(vdsDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(emptyCluster ?
                                                                               Collections.emptyList() :
                                                                               Collections.singletonList(null));
        assertThat(validator.managementNetworkChange(), expectedResult);
    }

}
