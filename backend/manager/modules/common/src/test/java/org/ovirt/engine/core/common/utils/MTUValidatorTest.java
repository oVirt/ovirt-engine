package org.ovirt.engine.core.common.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class MTUValidatorTest extends ValidationUtilsTest {

    private static final int TEST_MAX_MTU = 9000;
    private static final String TEST_MANAGEMENT_NETWORK = "ovirtmgmt";
    private Validator validator;

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();

        IConfigUtilsInterface confUtils = mock(IConfigUtilsInterface.class);
        when(confUtils.GetValue(ConfigValues.MaxMTU, Config.DefaultConfigurationVersion)).thenReturn(TEST_MAX_MTU);
        when(confUtils.GetValue(ConfigValues.ManagementNetwork, Config.DefaultConfigurationVersion)).thenReturn(TEST_MANAGEMENT_NETWORK);
        Config.setConfigUtils(confUtils);
    }

    @After
    public void tearDown() {
        Config.setConfigUtils(null);
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

    private <T extends Object> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }
}
