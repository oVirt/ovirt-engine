package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class VmNicValidatorTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VmNetworkInterface nic;

    private VmNicValidator validator;

    @Before
    public void setup() {
        validator = new VmNicValidator(nic, null);
    }

    @Test
    public void unlinkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(new ValidationResult(VdcBllMessages.UNLINKING_IS_NOT_SUPPORTED), false, false);
    }

    @Test
    public void linkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(ValidationResult.VALID, false, true);
    }

    @Test
    public void unlinkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(ValidationResult.VALID, true, false);
    }

    @Test
    public void linkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(ValidationResult.VALID, true, true);
    }

    @Test
    public void nullNetworkNameWhenUnlinkingNotSupported() throws Exception {
        networkNameTest(new ValidationResult(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED), false, null);
    }

    @Test
    public void validNetworkNameWhenUnlinkingNotSupported() throws Exception {
        networkNameTest(ValidationResult.VALID, false, "net");
    }

    @Test
    public void nullNetworkNameWhenUnlinkingSupported() throws Exception {
        networkNameTest(ValidationResult.VALID, true, null);
    }

    @Test
    public void validNetworkNameWhenUnlinkingSupported() throws Exception {
        networkNameTest(ValidationResult.VALID, true, "net");
    }

    private void unlinkingTest(ValidationResult expected, boolean networkLinkingSupported, boolean nicLinked) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, null, networkLinkingSupported);
        when(nic.isLinked()).thenReturn(nicLinked);

        assertEquals(expected, validator.linkedCorrectly());
    }

    private void networkNameTest(ValidationResult expected, boolean networkLinkingSupported, String networkName) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, null, networkLinkingSupported);
        when(nic.getNetworkName()).thenReturn(networkName);

        assertEquals(expected, validator.networkNameValid());
    }
}
