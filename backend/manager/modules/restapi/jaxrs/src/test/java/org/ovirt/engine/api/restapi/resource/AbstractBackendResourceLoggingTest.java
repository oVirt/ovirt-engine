package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;

public abstract class AbstractBackendResourceLoggingTest {
    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    protected void setUpLogExpectations(boolean debug) throws Exception {
        Field field = BaseBackendResource.class.getDeclaredField("log");
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(debug);
        setFinalStatic(field, logger);
    }
}
