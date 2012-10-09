package org.ovirt.engine.core.common.utils;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

public class ObjectUtilsTest {

    @Test
    public void testObjectsEqual() {
        Integer ten = new Integer(10);
        assertFalse(ObjectUtils.objectsEqual(ten, new Integer(20)));
        assertTrue(ObjectUtils.objectsEqual(ten, new Integer(10)));
        assertTrue(ObjectUtils.objectsEqual(null, null));
        assertFalse(ObjectUtils.objectsEqual(ten, null));
        assertFalse(ObjectUtils.objectsEqual(null, ten));
    }
}
