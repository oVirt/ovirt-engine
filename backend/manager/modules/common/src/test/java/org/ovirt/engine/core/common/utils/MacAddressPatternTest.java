package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

@RunWith(Parameterized.class)
public class MacAddressPatternTest {

    private static final Class<?>[] ALL_GROUPS = { CreateEntity.class, UpdateEntity.class };
    private static final Class<?>[] CREATE_GROUP = { CreateEntity.class };
    private static final Class<?>[] UPDATE_GROUP = { UpdateEntity.class };

    private Validator validator = ValidationUtils.getValidator();
    @Parameterized.Parameter(0)
    public String address;
    @Parameterized.Parameter(1)
    public boolean validMacAddress;
    @Parameterized.Parameter(2)
    public Class<?>[] groups;
    @Parameterized.Parameter(3)
    public String message;

    @Test
    public void checkIPAdress() {
        Set<ConstraintViolation<VmNic>> validate =
                validator.validate(createVmNic(), groups);
        assertEquals(validMacAddress, validate.isEmpty());

        if (validMacAddress) {
            assertTrue(validate.isEmpty());
        } else {
            assertEquals(1, validate.size());
            assertEquals(message, validate.iterator().next().getMessage());
        }
    }

    private VmNic createVmNic() {
        VmNic nic = new VmNic();
        nic.setName("nic1");
        nic.setMacAddress(address);
        return nic;
    }

    @Parameterized.Parameters
    public static Object[][] ipAddressParams() {
        return new Object[][] {
                         { "aa:aa:aa:aa:aa:aa", true, ALL_GROUPS, null },
                         { "AA:AA:AA:AA:AA:AA", true, ALL_GROUPS, null },
                         { "ff:ff:ff:ff:ff:ff", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST },
                         { "FF:FF:FF:FF:FF:FF", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST },
                         { "02:00:00:00:00:00", true, ALL_GROUPS, null },
                         { "", true, CREATE_GROUP, null },
                         { "", false, UPDATE_GROUP, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID },
                         { "00:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID },
                         { "100:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID },
                         { "00:00:00:00:00:001", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID },
                         { "01:00:00:00:00:00", false, ALL_GROUPS, VmNic.VALIDATION_VM_NETWORK_MAC_ADDRESS_MULTICAST },
                         { "02:00:00:00:XX:XX", false, ALL_GROUPS, VmNic.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID },
        };
    }
}
