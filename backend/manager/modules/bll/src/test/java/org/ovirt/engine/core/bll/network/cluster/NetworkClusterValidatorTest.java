package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.RandomUtils;

public class NetworkClusterValidatorTest {

    private static final String NETWORK_NAME = RandomUtils.instance().nextString(
            RandomUtils.instance().nextInt(1, 10));

    private static final String NETWORK_NAME_REPLACEMENT = String.format(
            NetworkClusterValidator.NETWORK_NAME_REPLACEMENT, NETWORK_NAME);

    private NetworkCluster networkCluster = new NetworkCluster();

    private NetworkClusterValidator validator = new NetworkClusterValidator(networkCluster);

    @Test
    public void managementNetworkAttachmentValid() throws Exception {
        networkCluster.setRequired(true);

        assertEquals(ValidationResult.VALID, validator.managementNetworkAttachment(NETWORK_NAME));
    }

    @Test
    public void managementNetworkAttachmentInvalid() throws Exception {
        networkCluster.setRequired(false);

        assertEquals(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED, NETWORK_NAME_REPLACEMENT),
                validator.managementNetworkAttachment(NETWORK_NAME));
    }
}
