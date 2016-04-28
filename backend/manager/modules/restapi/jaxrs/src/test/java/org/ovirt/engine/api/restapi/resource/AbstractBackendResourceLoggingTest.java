package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.mockito.Mockito;
import org.slf4j.Logger;

public abstract class AbstractBackendResourceLoggingTest extends Assert {
    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    protected void setUpLogExpectations(boolean debug) {
        try {
            Field field = BaseBackendResource.class.getDeclaredField("log");
            Logger logger = Mockito.mock(Logger.class);
            Mockito.when(logger.isDebugEnabled()).thenReturn(debug);
            setFinalStatic(field, logger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
