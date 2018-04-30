package org.ovirt.engine.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;

public class EntityHelperTest {

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
