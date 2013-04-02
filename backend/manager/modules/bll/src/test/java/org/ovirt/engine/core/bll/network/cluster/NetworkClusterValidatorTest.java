package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class NetworkClusterValidatorTest {

    private static final String CLUSTER_VERSION = "7";

    private static final String NETWORK_NAME = RandomUtils.instance().nextString(
            RandomUtils.instance().nextInt(1, 10));

    private static final String NETWORK_NAME_REPLACEMENT = String.format(
            NetworkClusterValidator.NETWORK_NAME_REPLACEMENT, NETWORK_NAME);

    @Mock
    private NetworkCluster networkCluster;

    @Mock
    private Version version;

    private NetworkClusterValidator validator;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Before
    public void setup() {
        when(version.getValue()).thenReturn(CLUSTER_VERSION);
        validator = new NetworkClusterValidator(networkCluster, version);
    }

    @Test
    public void managementNetworkAttachmentValid() throws Exception {
        when(networkCluster.isRequired()).thenReturn(Boolean.TRUE);

        assertThat(validator.managementNetworkAttachment(NETWORK_NAME), isValid());
    }

    @Test
    public void managementNetworkAttachmentInvalid() throws Exception {
        when(networkCluster.isRequired()).thenReturn(Boolean.FALSE);

        assertThat(validator.managementNetworkAttachment(NETWORK_NAME),
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    @Test
    public void migrationNetworkWhenMigrationNetworkNotSupported() throws Exception {
        migrationNetworkSupportTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_NETWORK_IS_NOT_SUPPORTED),
                false,
                true);
    }

    @Test
    public void migrationNetworkWhenMigrationNetworkSupported() throws Exception {
        migrationNetworkSupportTest(isValid(),
                true,
                true);
    }

    @Test
    public void notMigrationNetworkWhenMigrationNetworkNotSupported() throws Exception {
        migrationNetworkSupportTest(isValid(),
                false,
                false);
    }

    @Test
    public void notMigrationNetworkWhenMigrationNetworkSupported() throws Exception {
        migrationNetworkSupportTest(isValid(),
                true,
                false);
    }

    private void migrationNetworkSupportTest(Matcher<ValidationResult> matcher,
            boolean migrationNetworkSupported,
            boolean migration) {
        mockConfigRule.mockConfigValue(ConfigValues.MigrationNetworkEnabled, version, migrationNetworkSupported);
        when(networkCluster.isMigration()).thenReturn(migration);

        assertThat(validator.migrationPropertySupported(NETWORK_NAME), matcher);
    }
}
