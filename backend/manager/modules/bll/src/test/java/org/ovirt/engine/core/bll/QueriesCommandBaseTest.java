package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/** A test case for the {@link QueriesCommandBase} class. */
public class QueriesCommandBaseTest {
    private static final Log log = LogFactory.getLog(QueriesCommandBaseTest.class);

    /** Test queries are created with the correct type */
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
                assertFalse("could not find type", type.equals(VdcQueryType.Unknown));
            } catch (ClassNotFoundException ignore) {
                log.debug("skipping");
            } catch (ExceptionInInitializerError ignore) {
                log.debug("skipping");
            }
        }
    }

    /** Test that an "oddly" typed query will be considered unknown */
    @Test
    public void testUnknownQuery() throws Exception {
        ThereIsNoSuchQuery query = new ThereIsNoSuchQuery(mock(VdcQueryParametersBase.class));
        Field f = getQueryTypeField();
        assertEquals("Wrong type for 'ThereIsNoSuchQuery' ", VdcQueryType.Unknown, f.get(query));
    }

    /** @return The private type field, via reflection */
    private static Field getQueryTypeField() {
        for (Field f : QueriesCommandBase.class.getDeclaredFields()) {
            if (f.getName().equals("type")) {
                f.setAccessible(true);
                return f;
            }
        }
        fail("Can't find the type field");
        return null;
    }

    /** A stub class that will cause the {@link VdcQueryType#Unknown} to be used */
    private static class ThereIsNoSuchQuery extends QueriesCommandBase<VdcQueryParametersBase> {

        public ThereIsNoSuchQuery(VdcQueryParametersBase parameters) {
            super(parameters);
        }

        @Override
        protected void executeQueryCommand() {
            // Stub method, do nothing
        }

    }
}
