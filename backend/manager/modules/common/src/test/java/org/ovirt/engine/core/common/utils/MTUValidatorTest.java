package org.ovirt.engine.core.common.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.validation.annotation.MTU;

public class MTUValidatorTest {

    private static final String TEST_MANAGEMENT_NETWORK = "ovirtmgmt";
    private Validator validator;

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();

        IConfigUtilsInterface confUtils = mock(IConfigUtilsInterface.class);
        when(confUtils.getValue(ConfigValues.DefaultManagementNetwork, ConfigCommon.defaultConfigurationVersion)).thenReturn(TEST_MANAGEMENT_NETWORK);
        Config.setConfigUtils(confUtils);
    }

    @After
    public void tearDown() {
        Config.setConfigUtils(null);
    }

    @Test
    public void invalidLowMTU() {
        Set<ConstraintViolation<MtuContainer>> validate = validate(new MtuContainer(30));
        Assert.assertTrue(validate.size() > 0);

    }

    @Test
    public void useDefaultMTU() {
        Set<ConstraintViolation<MtuContainer>> validate = validate(new MtuContainer(0));
        Assert.assertTrue(validate.size() == 0);
    }

    private <T extends Object> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    private class MtuContainer {
        @MTU
        private int mtu;

        public MtuContainer(int mtu) {
            super();
            this.mtu = mtu;
        }
    }
}
