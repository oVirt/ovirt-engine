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

    private static final VmInterfaceType VALID_NIC_TYPE = VmInterfaceType.e1000;

    private static final String VALID_NIC_NAME = "nic";

    private static final String VALID_MAC_ADDRESS = "01:23:45:67:89:ab";

    private Random random = new Random();

    @Test
    public void nameIsNull() throws Exception {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, null, VALID_NIC_TYPE),
                true,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void nameIsNotNull() throws Exception {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, VALID_NIC_NAME, VALID_NIC_TYPE),
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void macAddressIsNull() throws Exception {
        assertNicValidation(createNic(null, VALID_NIC_NAME, VALID_NIC_TYPE),
                true,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL);
    }

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
        assertNicValidation(createNic(macAddress, VALID_NIC_NAME, VALID_NIC_TYPE),
                shouldValidationFail,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID);
    }

    private NetworkInterface<?> createNic(String macAddress, String name, VmInterfaceType vmInterfaceType) {
        NetworkInterface<?> nic = new VmNetworkInterface();
        nic.setName(name);
        nic.setMacAddress(macAddress);
        nic.setType(vmInterfaceType == null ? null : vmInterfaceType.getValue());
        return nic;
    }

    /**
     * Assert that the given NIC is being validated.
     *
     * @param nic
     *            The NIC to validate.
     * @param shouldValidationFail
     *            Should the validation fail due to invalid format, or not.
     * @param possibleViolation
     *            Violation that should or shounldn't occur (determined by previous parameter),
     */
    private void assertNicValidation(NetworkInterface<?> nic, boolean shouldValidationFail, String possibleViolation) {
        List<String> violations = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { CreateEntity.class }), new TestParams(nic));

        assertEquals("Validation should" + (shouldValidationFail ? "" : "'nt") + " fail due to violation: ["
                + possibleViolation + "]; Actual violations were: " + violations,
                shouldValidationFail,
                violations.contains(possibleViolation));
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
