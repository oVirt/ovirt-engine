package org.ovirt.engine.core.common.utils;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({ Config.class })
public class MTUValidatorTest extends ValidationUtilsTest {

    private Validator validator;

    @Before
    public void setup() throws SecurityException, NoSuchMethodException, Exception {
        super.setup();
        validator = ValidationUtils.getValidator();
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.ManagementNetwork)).thenReturn("ovirtmgmt");
        mockMaxMTU(9000);
    }

    @Test
    public void invalidLowMTU() {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setMtu(30);
        Set<ConstraintViolation<VdsNetworkInterface>> validate = validate(nic);
        Assert.assertTrue(validate.size() > 0);

    }

    @Test
    public void invalidHighMTU() {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setMtu(15000);
        Set<ConstraintViolation<VdsNetworkInterface>> validate = validate(nic);
        Assert.assertTrue(validate.size() > 0);

    }

    @Test
    public void useDefaultMTU() {
        network net = new network();
        net.setMtu(0);
        Set<ConstraintViolation<network>> validate = validate(net);
        Assert.assertTrue(validate.size() == 0);
    }

    private void mockMaxMTU(int maxMTU) {
        when(Config.GetValue(ConfigValues.MaxMTU)).thenReturn(maxMTU);
    }

    private <T extends Object> Set<ConstraintViolation<T>> validate(T object) {
        Set<ConstraintViolation<T>> validate = validator.validate(object);
        for (ConstraintViolation<T> violation : validate) {
            System.out.println(violation.getMessage());
        }
        return validate;
    }
}
