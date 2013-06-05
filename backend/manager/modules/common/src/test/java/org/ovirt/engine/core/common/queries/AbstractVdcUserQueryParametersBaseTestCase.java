package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for {@link VdcUserQueryParametersBase} */
public abstract class AbstractVdcUserQueryParametersBaseTestCase<P extends VdcUserQueryParametersBase> {

    /** The {@link VdcUserQueryParametersBase} being tested */
    private P param;

    protected P getParamObject() {
        return param;
    }

    @SuppressWarnings("unchecked")
    private Class<P> getTypeParameterClass() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<P>) paramType.getActualTypeArguments()[0];
    }

    @Test
    public void testDefaultConstructor() throws Exception {
        param = getTypeParameterClass().getConstructor().newInstance();
        assertNull("There should not be any user ID with the default constructor", param.getUserId());
    }

    @Test
    public void testParameterizedConstructor() throws Exception {
        Guid expectedUserID = Guid.newGuid();
        param = getTypeParameterClass().getConstructor(Guid.class).newInstance(expectedUserID);
        assertEquals("Wrong user ID", expectedUserID, param.getUserId());
    }
}
