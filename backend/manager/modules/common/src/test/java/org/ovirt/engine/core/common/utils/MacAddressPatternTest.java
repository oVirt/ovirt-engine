package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

public class MacAddressPatternTest {

    private static final Class<?>[] ALL_GROUPS = { CreateEntity.class, UpdateEntity.class };
    private static final Class<?>[] CREATE_GROUP = { CreateEntity.class };
    private static final Class<?>[] UPDATE_GROUP = { UpdateEntity.class };

    private Validator validator = ValidationUtils.getValidator();

    @ParameterizedTest
    @MethodSource
    public void ipAddress(String address, boolean validMacAddress, Class<?>[] groups, String message) {
        Set<ConstraintViolation<VmNic>> validate =
                validator.validate(createVmNic(address), groups);
        assertEquals(validMacAddress, validate.isEmpty());

        if (!validMacAddress) {
            assertEquals(message, validate.iterator().next().getMessage());
        }
    }

    private VmNic createVmNic(String address) {
        VmNic nic = new VmNic();
        nic.setName("nic1");
        nic.setMacAddress(address);
        return nic;
    }

    public static Stream<Arguments> ipAddress() {
        return Stream.of(
                Arguments.of("aa:aa:aa:aa:aa:aa", true, ALL_GROUPS, null),
                Arguments.of("AA:AA:AA:AA:AA:AA", true, ALL_GROUPS, null),
                Arguments.of("ff:ff:ff:ff:ff:ff", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST),
                Arguments.of("FF:FF:FF:FF:FF:FF", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST),
                Arguments.of("02:00:00:00:00:00", true, ALL_GROUPS, null),
                Arguments.of("", true, CREATE_GROUP, null),
                Arguments.of("", false, UPDATE_GROUP, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                Arguments.of("00:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                Arguments.of("100:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                Arguments.of("00:00:00:00:00:001", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID),
                Arguments.of("01:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST),
                Arguments.of("02:00:00:00:XX:XX", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID)
        );
    }
}
