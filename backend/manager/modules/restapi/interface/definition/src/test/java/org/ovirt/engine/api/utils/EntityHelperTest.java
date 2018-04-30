package org.ovirt.engine.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
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
