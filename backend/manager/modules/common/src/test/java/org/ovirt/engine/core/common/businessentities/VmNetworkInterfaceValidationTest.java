package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.validation.Valid;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;

@SuppressWarnings("serial")
public class VmNetworkInterfaceValidationTest {

    private static final String VALID_MAC_ADDRESS = "01:23:45:67:89:ab";

    private Random random = new Random();

    @Test
    public void macAddressFormatValid() throws Exception {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS, false);
    }

    @Test
    public void macAddressFormatInvalid() throws Exception {
        assertMacAddressFormatValidation(createInvalidMacAddress(), true);
    }

    @Test
    public void macAddressTooLong() throws Exception {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS + ":00", true);
    }

    @Test
    public void macAddressTooShort() throws Exception {
        assertMacAddressFormatValidation(createShortMacAddress(), true);
    }

    /**
     * Assert that MAC address format is being validated.
     *
     * @param macAddress
     *            The MAC address to validate.
     * @param shouldValidationFail
     *            Should the validation fail due to invalid format, or not.
     */
    private void assertMacAddressFormatValidation(String macAddress, boolean shouldValidationFail) {
        NetworkInterface<?> nic = new VmNetworkInterface();
        nic.setMacAddress(macAddress);

        List<String> violations = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { CreateEntity.class }), new TestParams(nic));

        assertEquals(shouldValidationFail,
                violations.contains(VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID));
    }

    private String createInvalidMacAddress() {
        return VALID_MAC_ADDRESS.replace(VALID_MAC_ADDRESS.charAt(random.nextInt(VALID_MAC_ADDRESS.length())), 'x');
    }

    private String createShortMacAddress() {
        return VALID_MAC_ADDRESS.substring(0, random.nextInt(VALID_MAC_ADDRESS.length()));
    }

    private class TestParams extends VdcActionParametersBase {

        @SuppressWarnings("unused")
        @Valid
        private NetworkInterface<?> nic;

        public TestParams(NetworkInterface<?> nic) {
            this.nic = nic;
        }
    }
}
