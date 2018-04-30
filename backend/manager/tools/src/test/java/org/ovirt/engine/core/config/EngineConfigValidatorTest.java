package org.ovirt.engine.core.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
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

    @Test
    public void testSetValidatorWithOutKey() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getValue()).thenReturn("valueToSet");
        ConfigActionType setAction = ConfigActionType.ACTION_SET;
        assertThrows(IllegalArgumentException.class, () -> setAction.validate(engineConfigMap));
    }

    @Test
    public void testSetValidatorWithOutValue() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        when(engineConfigMap.getKey()).thenReturn("keyToSet");
        ConfigActionType setAction = ConfigActionType.ACTION_SET;
        assertThrows(IllegalArgumentException.class, () -> setAction.validate(engineConfigMap));
    }

    @Test
    public void testGetValidatorWithOutKey() {
        EngineConfigMap engineConfigMap = mock(EngineConfigMap.class);
        ConfigActionType setAction = ConfigActionType.ACTION_GET;
        assertThrows(IllegalArgumentException.class, () -> setAction.validate(engineConfigMap));
    }
}
