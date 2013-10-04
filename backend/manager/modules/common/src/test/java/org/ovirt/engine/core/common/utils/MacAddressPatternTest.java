package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
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

    private final static Class<?>[] ALL_GROUPS = { CreateEntity.class, UpdateEntity.class };
    private final static Class<?>[] CREATE_GROUP = { CreateEntity.class };
    private final static Class<?>[] UPDATE_GROUP = { UpdateEntity.class };

    private Validator validator;
    private String address;
    private boolean expectedResult;
    private Class<?>[] groups;

    public MacAddressPatternTest(String address, boolean expectedResult, Class<?>[] groups) {
        this.address = address;
        this.expectedResult = expectedResult;
        this.groups = groups;
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void checkIPAdress() {
        Set<ConstraintViolation<VmNic>> validate =
                validator.validate(createVmNic(), groups);
        assertEquals(expectedResult, validate.isEmpty());
    }

    private VmNic createVmNic() {
        VmNic nic = new VmNic();
        nic.setName("nic1");
        nic.setMacAddress(address);
        return nic;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> ipAddressParams() {
        return Arrays.asList(new Object[][] {
                { "aa:aa:aa:aa:aa:aa", true, ALL_GROUPS },
                { "AA:AA:AA:AA:AA:AA", true, ALL_GROUPS },
                { "ff:ff:ff:ff:ff:ff", false, ALL_GROUPS },
                { "FF:FF:FF:FF:FF:FF", false, ALL_GROUPS },
                { "02:00:00:00:00:00", true, ALL_GROUPS },
                { "", true, CREATE_GROUP },
                { "", false, UPDATE_GROUP },
                { "00:00:00:00:00:00", false, ALL_GROUPS },
                { "100:00:00:00:00:00", false, ALL_GROUPS },
                { "00:00:00:00:00:001", false, ALL_GROUPS },
                { "01:00:00:00:00:00", false, ALL_GROUPS },
                { "02:00:00:00:XX:XX", false, ALL_GROUPS },
        });
    }
}
