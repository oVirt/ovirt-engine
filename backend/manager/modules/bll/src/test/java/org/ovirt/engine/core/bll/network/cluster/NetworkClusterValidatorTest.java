package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.junit.Test;
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

        assertThat(validator.managementNetworkAttachment(NETWORK_NAME), isValid());
    }

    @Test
    public void managementNetworkAttachmentInvalid() throws Exception {
        networkCluster.setRequired(false);

        assertThat(validator.managementNetworkAttachment(NETWORK_NAME),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }
}
