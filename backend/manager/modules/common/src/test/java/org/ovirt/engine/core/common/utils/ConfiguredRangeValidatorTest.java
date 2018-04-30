package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class ConfiguredRangeValidatorTest {

    private static final int TEST_DEFAULT_MIN_RANGE = 0;
    private static final int TEST_MIN_RANGE = 5;
    private static final int TEST_MAX_RANGE = 100;
    private static final int TEST_IN_RANGE = TEST_MAX_RANGE;
    private static final int TEST_OUT_OF_UPPER_RANGE = TEST_MAX_RANGE + 1;
    private static final int TEST_OUT_OF_LOWER_RANGE = TEST_MIN_RANGE - 1;

    private Validator validator;

    @AfterEach
    public void tearDown() {
        Config.setConfigUtils(null);
    }

    @Test
    public void valueOutOfUpperRange() {
        initConfiguredRange();
        validateAndAssertResult(new ConfiguredRangeContainer(TEST_OUT_OF_UPPER_RANGE), false);
    }

    @Test
    public void valueOutOfDefaultLowerRange() {
        initConfiguredWithDefaultMinValue();
        validateAndAssertResult(new ConfiguredRangeContainerDefaultMin(TEST_DEFAULT_MIN_RANGE - 1), false);
    }

    @Test
    public void valueOutOfLowerRange() {
        initConfiguredWithDefaultMinValue();
        validateAndAssertResult(new ConfiguredRangeContainer(TEST_OUT_OF_LOWER_RANGE), false);
    }

    @Test
    public void valueOutOfConfiguredLowerRange() {
        initConfiguredWithDefaultMinValue();
        validateAndAssertResult(new ConfiguredRangeContainerWithConfMin(TEST_MIN_RANGE - 1), false);
    }

    @Test
    public void valueInConfiguredLowerRange() {
        initConfiguredWithDefaultMinValue();
        validateAndAssertResult(new ConfiguredRangeContainerWithConfMin(TEST_MIN_RANGE), true);
    }

    @Test
    public void valueInRange() {
        initConfiguredRange();
        validateAndAssertResult(new ConfiguredRangeContainer(TEST_IN_RANGE), true);
    }

    /**
     * The test assures the precedence of the configuration value over the min attribute of the annotated value.
     *
     * The minimum defined by <code>minConfigValue</code> is <code>TEST_MIN_RANGE</code>.<br/>
     * The minimum defined by <code>min()</code> is <code>TEST_MIN_RANGE + 2</code>.<br/>
     * The test verifies the valid values are greater then the min value obtained from <code>minConfigValue</code><br/>
     */
    @Test
    public void valueLowerLimitSetByConfigValue() {
        initConfiguredWithDefaultMinValue();
        validateAndAssertResult(new ConfiguredRangeContainerWithBothMinAttributes(TEST_MIN_RANGE + 1), true);
        validateAndAssertResult(new ConfiguredRangeContainerWithBothMinAttributes(TEST_MIN_RANGE - 1), false);
    }

    private <T> void validateAndAssertResult(T container, boolean isValid) {
        Set<ConstraintViolation<T>> validate = validator.validate(container);
        assertEquals(isValid, validate.isEmpty());
    }

    public void initConfiguredRange() {
        IConfigUtilsInterface configUtils = initConfigUtils();
        Config.setConfigUtils(configUtils);
    }

    public void initConfiguredWithDefaultMinValue() {
        IConfigUtilsInterface configUtils = initConfigUtils();
        when(configUtils.getValue(ConfigValues.ConnectToServerTimeoutInSeconds, ConfigCommon.defaultConfigurationVersion)).thenReturn(TEST_MIN_RANGE);
        Config.setConfigUtils(configUtils);
    }

    private IConfigUtilsInterface initConfigUtils() {
        validator = ValidationUtils.getValidator();
        IConfigUtilsInterface configUtils = mock(IConfigUtilsInterface.class);
        when(configUtils.getValue(ConfigValues.vdsTimeout, ConfigCommon.defaultConfigurationVersion)).thenReturn(TEST_MAX_RANGE);
        return configUtils;
    }

    private static class ConfiguredRangeContainer {
        @ConfiguredRange(min = TEST_MIN_RANGE, maxConfigValue = ConfigValues.vdsTimeout)
        private int value;

        public ConfiguredRangeContainer(int value) {
            this.value = value;
        }
    }

    private static class ConfiguredRangeContainerDefaultMin {
        @ConfiguredRange(maxConfigValue = ConfigValues.vdsTimeout)
        private int value;

        public ConfiguredRangeContainerDefaultMin(int value) {
            this.value = value;
        }
    }

    private static class ConfiguredRangeContainerWithConfMin {
        @ConfiguredRange(minConfigValue = ConfigValues.ConnectToServerTimeoutInSeconds,
                maxConfigValue = ConfigValues.vdsTimeout)
        private int value;

        public ConfiguredRangeContainerWithConfMin(int value) {
            this.value = value;
        }
    }

    private static class ConfiguredRangeContainerWithBothMinAttributes {
        @ConfiguredRange(min = TEST_MIN_RANGE + 2, minConfigValue = ConfigValues.ConnectToServerTimeoutInSeconds,
                maxConfigValue = ConfigValues.vdsTimeout)
        private int value;

        public ConfiguredRangeContainerWithBothMinAttributes(int value) {
            this.value = value;
        }
    }

}
