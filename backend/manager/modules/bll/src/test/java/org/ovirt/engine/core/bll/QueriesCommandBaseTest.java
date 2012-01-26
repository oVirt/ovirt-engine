package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/** A test case for the {@link QueriesCommandBase} class. */
public class QueriesCommandBaseTest {
    private static final Log log = LogFactory.getLog(QueriesCommandBaseTest.class);

    /** */
    @SuppressWarnings("unchecked")
    @Test
    public void testQueryCreation() throws Exception {
        for (VdcQueryType queryType : VdcQueryType.values()) {
            try {
                log.debug("evaluating " + queryType);

                // Get the query's class
                Class<? extends QueriesCommandBase<?>> clazz =
                        (Class<? extends QueriesCommandBase<?>>) Class.forName(queryType.getPackageName() + "."
                                + queryType.name() + "Query");

                // Create a new instance, parameters don't matter
                Constructor<? extends QueriesCommandBase<?>> cons =
                        (Constructor<? extends QueriesCommandBase<?>>) clazz.getConstructors()[0];

                // Construct the parameter array
                Class<?>[] paramTypes = cons.getParameterTypes();
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; ++i) {
                    params[i] = mock(paramTypes[i]);
                }
                QueriesCommandBase<?> query = cons.newInstance(params);

                // find the getQueryType method - note that it's private.
                Field typeField = getQueryTypeField();

                // Invoke it and get the enum
                VdcQueryType type = (VdcQueryType) typeField.get(query);
                assertNotNull("could not find type", type);
            } catch (ClassNotFoundException ignore) {
                log.debug("skipping");
            } catch (ExceptionInInitializerError ignore) {
                log.debug("skipping");
            }
        }
    }

    private static Field getQueryTypeField() {
        for (Field f : QueriesCommandBase.class.getDeclaredFields()) {
            if (f.getName().equals("_type")) {
                f.setAccessible(true);
                return f;
            }
        }
        fail("Can't find the _type field");
        return null;
    }
}
