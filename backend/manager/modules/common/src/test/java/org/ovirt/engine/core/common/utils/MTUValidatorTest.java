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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.validation.annotation.MTU;

public class MTUValidatorTest {

    private static final int TEST_MAX_MTU = 9000;
    private static final String TEST_MANAGEMENT_NETWORK = "ovirtmgmt";
    private Validator validator;

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();

        IConfigUtilsInterface confUtils = mock(IConfigUtilsInterface.class);
        when(confUtils.GetValue(ConfigValues.MaxMTU, ConfigCommon.defaultConfigurationVersion)).thenReturn(TEST_MAX_MTU);
        when(confUtils.GetValue(ConfigValues.ManagementNetwork, ConfigCommon.defaultConfigurationVersion)).thenReturn(TEST_MANAGEMENT_NETWORK);
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
    public void invalidHighMTU() {
        Set<ConstraintViolation<MtuContainer>> validate = validate(new MtuContainer(TEST_MAX_MTU + 1));
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
        @SuppressWarnings("unused")
        private int mtu;

        public MtuContainer(int mtu) {
            super();
            this.mtu = mtu;
        }
    }
}
