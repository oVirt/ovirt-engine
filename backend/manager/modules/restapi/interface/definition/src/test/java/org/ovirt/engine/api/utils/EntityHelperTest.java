package org.ovirt.engine.api.utils;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;

public class EntityHelperTest extends Assert {

    @Test
    public void testIsCollection() {
        assertTrue(EntityHelper.isCollection(new Vms()));
        assertFalse(EntityHelper.isCollection(new Vm()));
    }

    @Test
    public void testGetCollectionGettter() {
        Method method = EntityHelper.getCollectionGetter(new Vms());
        assertEquals("getVms", method.getName());
    }
}
