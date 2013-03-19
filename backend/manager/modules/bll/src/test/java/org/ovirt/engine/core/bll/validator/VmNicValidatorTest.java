package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
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
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class VmNicValidatorTest {

    private static final String CLUSTER_VERSION = "7";

    private static final String CLUSTER_VERSION_REPLACEMENT =
            String.format(VmNicValidator.CLUSTER_VERSION_REPLACEMENT_FORMAT, CLUSTER_VERSION);

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VmNetworkInterface nic;

    @Mock
    private Version version;

    private VmNicValidator validator;

    @Before
    public void setup() {
        when(version.getValue()).thenReturn(CLUSTER_VERSION);

        validator = new VmNicValidator(nic, version);
    }

    @Test
    public void unlinkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(both(failsWith(VdcBllMessages.UNLINKING_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))),
                false,
                false);
    }

    @Test
    public void linkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(isValid(), false, true);
    }

    @Test
    public void unlinkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(isValid(), true, false);
    }

    @Test
    public void linkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(isValid(), true, true);
    }

    @Test
    public void nullNetworkNameWhenUnlinkingNotSupported() throws Exception {
        networkNameTest(both(failsWith(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))),
                false,
                null);
    }

    @Test
    public void validNetworkNameWhenUnlinkingNotSupported() throws Exception {
        networkNameTest(isValid(), false, "net");
    }

    @Test
    public void nullNetworkNameWhenUnlinkingSupported() throws Exception {
        networkNameTest(isValid(), true, null);
    }

    @Test
    public void validNetworkNameWhenUnlinkingSupported() throws Exception {
        networkNameTest(isValid(), true, "net");
    }

    @Test
    public void validNetworkWhenPortMirroring() throws Exception {
        portMirroringTest(isValid(), "net", true);
    }

    @Test
    public void nullNetworkWhenPortMirroring() throws Exception {
        portMirroringTest(failsWith(VdcBllMessages.PORT_MIRRORING_REQUIRES_NETWORK), null, true);
    }

    @Test
    public void validNetworkWhenNoPortMirroring() throws Exception {
        portMirroringTest(isValid(), "net", false);
    }

    @Test
    public void nullNetworkWhenNoPortMirroring() throws Exception {
        portMirroringTest(isValid(), null, false);
    }

    @Test
    public void externalNetworkPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(true,
                true,
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED));
    }

    @Test
    public void externalNetworkNotPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(true, false, isValid());
    }

    @Test
    public void internalNetworkPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(false, true, isValid());
    }

    @Test
    public void internalNetworkNotPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(false, false, isValid());
    }

    private void externalNetworkPortMirroringTest(boolean externalNetwork,
            boolean portMirroring,
            Matcher<ValidationResult> matcher) {
        Network network = mock(Network.class);
        if (externalNetwork) {
            when(network.getProvidedBy()).thenReturn(mock(ProviderNetwork.class));
        }

        when(nic.isPortMirroring()).thenReturn(portMirroring);

        assertThat(validator.portMirroringNotSetIfExternalNetwork(network), matcher);
    }

    private void unlinkingTest(Matcher<ValidationResult> matcher, boolean networkLinkingSupported, boolean nicLinked) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.isLinked()).thenReturn(nicLinked);

        assertThat(validator.linkedCorrectly(), matcher);
    }

    private void networkNameTest(Matcher<ValidationResult> matcher, boolean networkLinkingSupported, String networkName) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.getNetworkName()).thenReturn(networkName);

        assertThat(validator.networkNameValid(), matcher);
    }

    private void portMirroringTest(Matcher<ValidationResult> matcher, String networkName, boolean portMirroring) {
        when(nic.getNetworkName()).thenReturn(networkName);
        when(nic.isPortMirroring()).thenReturn(portMirroring);

        assertThat(validator.networkProvidedForPortMirroring(), matcher);
    }
}
