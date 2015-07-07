package org.ovirt.engine.core.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.config.validation.ConfigActionType;

public class EngineConfigValidatorTest {

    /**
     * If an exception is thrown the test has failed, otherwise the test succeeded
     */
    @Test
    public void testSetValidatorWithValidArgs() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn("keyToSet");
        when(engineConfigMap.getValue()).thenReturn("valueToSet");
        ConfigActionType setAction = ConfigActionType.ACTION_SET;
        setAction.validate(engineConfigMap);
    }

    /**
     * If an exception is thrown the test has failed, otherwise the test succeeded
     */
    @Test
    public void testGetValidatorWithValidArgs() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn("keyToGet");
        ConfigActionType setAction = ConfigActionType.ACTION_GET;
        setAction.validate(engineConfigMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValidatorWithOutKey() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn(null);
        when(engineConfigMap.getValue()).thenReturn("valueToSet");
        ConfigActionType setAction = ConfigActionType.ACTION_SET;
        setAction.validate(engineConfigMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValidatorWithOutValue() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn("keyToSet");
        when(engineConfigMap.getValue()).thenReturn(null);
        ConfigActionType setAction = ConfigActionType.ACTION_SET;
        setAction.validate(engineConfigMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValidatorWithOutKey() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn(null);
        ConfigActionType setAction = ConfigActionType.ACTION_GET;
        setAction.validate(engineConfigMap);
    }
}
