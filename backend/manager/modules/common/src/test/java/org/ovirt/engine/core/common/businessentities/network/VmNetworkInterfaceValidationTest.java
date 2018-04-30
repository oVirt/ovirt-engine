package org.ovirt.engine.core.common.businessentities.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.validation.Valid;
import javax.validation.groups.Default;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;

@SuppressWarnings("serial")
public class VmNetworkInterfaceValidationTest {

    private static final VmInterfaceType VALID_NIC_TYPE = VmInterfaceType.e1000;

    private static final String VALID_NIC_NAME = "nic";

    private static final String VALID_MAC_ADDRESS = "00:23:45:67:89:ab";

    private static final Random random = new Random();

    @Test
    public void nameIsNullForCreate() {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, null, VALID_NIC_TYPE),
                CreateEntity.class,
                true,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void nameIsNullForUpdate() {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, null, VALID_NIC_TYPE),
                CreateEntity.class,
                true,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void nameIsNullForDefault() {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, null, VALID_NIC_TYPE),
                Default.class,
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void nameIsNotNullForCreate() {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, VALID_NIC_NAME, VALID_NIC_TYPE),
                CreateEntity.class,
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void nameIsNotNullForUpdate() {
        assertNicValidation(createNic(VALID_MAC_ADDRESS, VALID_NIC_NAME, VALID_NIC_TYPE),
                UpdateEntity.class,
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_NAME_NOT_NULL);
    }

    @Test
    public void macAddressIsNullForVmNicUpdate() {
        assertNicValidation(createNic(null, VALID_NIC_NAME, VALID_NIC_TYPE),
                UpdateVmNic.class,
                true,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL);
    }

    @Test
    public void macAddressIsNullForUpdate() {
        assertNicValidation(createNic(null, VALID_NIC_NAME, VALID_NIC_TYPE),
                UpdateEntity.class,
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL);
    }

    @Test
    public void macAddressIsNullForDefault() {
        assertNicValidation(createNic(null, VALID_NIC_NAME, VALID_NIC_TYPE),
                Default.class,
                false,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_NOT_NULL);
    }

    @Test
    public void macAddressEmptyForCreate() {
        assertMacAddressFormatValidation("", CreateEntity.class, false);
    }

    @Test
    public void macAddressFormatValidForCreate() {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS, CreateEntity.class, false);
    }

    @Test
    public void macAddressEmptyForUpdate() {
        assertMacAddressFormatValidation("", UpdateEntity.class, true);
    }

    @Test
    public void macAddressFormatValidForUpdate() {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS, UpdateEntity.class, false);
    }

    @Test
    public void macAddressFormatInvalidForDefault() {
        assertMacAddressFormatValidation(createInvalidMacAddress(), Default.class, false);
    }

    @Test
    public void macAddressFormatInvalidForCreate() {
        assertMacAddressFormatValidation(createInvalidMacAddress(), CreateEntity.class, true);
    }

    @Test
    public void macAddressFormatInvalidForUpdate() {
        assertMacAddressFormatValidation(createInvalidMacAddress(), UpdateEntity.class, true);
    }

    @Test
    public void macAddressTooLongForCreate() {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS + ":00", CreateEntity.class, true);
    }

    @Test
    public void macAddressTooLongForUpdate() {
        assertMacAddressFormatValidation(VALID_MAC_ADDRESS + ":00", UpdateEntity.class, true);
    }

    @Test
    public void macAddressTooShortForCreate() {
        assertMacAddressFormatValidation(createShortMacAddress(), CreateEntity.class, true);
    }

    @Test
    public void macAddressTooShortForUpdate() {
        assertMacAddressFormatValidation(createShortMacAddress(), UpdateEntity.class, true);
    }

    /**
     * Assert that MAC address format is being validated.
     *
     * @param macAddress
     *            The MAC address to validate.
     * @param validationGroup
     *            The validation group to use.
     * @param shouldValidationFail
     *            Should the validation fail due to invalid format, or not.
     */
    private static void assertMacAddressFormatValidation(String macAddress,
                                                         Class<?> validationGroup,
                                                         boolean shouldValidationFail) {
        assertNicValidation(createNic(macAddress, VALID_NIC_NAME, VALID_NIC_TYPE),
                validationGroup,
                shouldValidationFail,
                VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID);
    }

    private static NetworkInterface<?> createNic(String macAddress, String name, VmInterfaceType vmInterfaceType) {
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
     * @param validationGroup
     *            The validation group to use.
     * @param shouldValidationFail
     *            Should the validation fail due to invalid format, or not.
     * @param possibleViolation
     *            Violation that should or shounldn't occur (determined by previous parameter),
     */
    private static void assertNicValidation(NetworkInterface<?> nic,
                                            Class<?> validationGroup,
                                            boolean shouldValidationFail,
                                            String possibleViolation) {
        List<String> violations = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { validationGroup }), new TestParams(nic));

        assertEquals(
                shouldValidationFail,
                violations.contains(possibleViolation), "Validation should" + (shouldValidationFail ? "" : "'nt") + " fail due to violation: ["
                        + possibleViolation + "]; Actual violations were: " + violations);
    }

    private static String createInvalidMacAddress() {
        return VALID_MAC_ADDRESS.replace(VALID_MAC_ADDRESS.charAt(random.nextInt(VALID_MAC_ADDRESS.length())), 'x');
    }

    /**
     * @return A MAC address that is at least 1 char long but shorter than a valid MAC address.
     */
    private static String createShortMacAddress() {
        return VALID_MAC_ADDRESS.substring(random.nextInt(VALID_MAC_ADDRESS.length() - 2) + 1);
    }

    private static class TestParams extends ActionParametersBase {

        @Valid
        private NetworkInterface<?> nic;

        public TestParams(NetworkInterface<?> nic) {
            this.nic = nic;
        }
    }
}
