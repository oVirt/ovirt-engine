package org.ovirt.engine.core.bll.validator;

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
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class VmNicValidatorTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final String CLUSTER_VERSION = "7";

    private static final String CLUSTER_VERSION_REPLACEMENT =
            String.format(VmNicValidator.CLUSTER_VERSION_REPLACEMENT_FORMAT, CLUSTER_VERSION);

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VmNic nic;

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
    public void nullVnicProfileWhenUnlinkingNotSupported() throws Exception {
        vnicProfileTest(both(failsWith(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))),
                false,
                null);
    }

    @Test
    public void validVnicProfileWhenUnlinkingNotSupported() throws Exception {
        vnicProfileTest(isValid(), false, VNIC_PROFILE_ID);
    }

    @Test
    public void nullVnicProfileWhenUnlinkingSupported() throws Exception {
        vnicProfileTest(isValid(), true, null);
    }

    @Test
    public void validVnicProfileWhenUnlinkingSupported() throws Exception {
        vnicProfileTest(isValid(), true, VNIC_PROFILE_ID);
    }

    private void unlinkingTest(Matcher<ValidationResult> matcher, boolean networkLinkingSupported, boolean nicLinked) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.isLinked()).thenReturn(nicLinked);

        assertThat(validator.linkedCorrectly(), matcher);
    }

    private void vnicProfileTest(Matcher<ValidationResult> matcher, boolean networkLinkingSupported, Guid vnicProfileId) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.getVnicProfileId()).thenReturn(vnicProfileId);

        assertThat(validator.emptyNetworkValid(), matcher);
    }

}
