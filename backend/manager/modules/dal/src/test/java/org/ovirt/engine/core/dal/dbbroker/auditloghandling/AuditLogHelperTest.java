package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import junit.framework.TestCase;

public class AuditLogHelperTest extends TestCase {
    public void testSimple() {
        CustomLogField[] array = AuditLogHelper.getCustomLogFields(BaseClass.class, true);
        assertEquals("size should be 1", 1, array.length);
        array = AuditLogHelper.getCustomLogFields(BaseClass.class, false);
        assertEquals("Not Inherited size should be 1", 1, array.length);
    }

    public void testSingleChild() {
        CustomLogField[] array = AuditLogHelper.getCustomLogFields(SingleChild.class, true);
        assertEquals("size should be 2", 2, array.length);
        assertEquals("Parent Attribute", "JarJar", array[0].value());
        assertEquals("Child Attribute", "Binks", array[1].value());
        array = AuditLogHelper.getCustomLogFields(SingleChild.class, false);
        assertEquals("Not inherited size should be 1", 1, array.length);
        assertEquals("Not Inhertied Child Attribute", "Binks", array[0].value());
    }

    public void testMultiChild() {
        CustomLogField[] array = AuditLogHelper.getCustomLogFields(MultiChild.class, true);
        assertEquals("size should be 3", 3, array.length);
        assertEquals("Parent Attribute", "JarJar", array[0].value());
        assertEquals("Child Attribute", "Luke", array[1].value());
        assertEquals("Child Attribute", "Skywalker", array[2].value());
        array = AuditLogHelper.getCustomLogFields(MultiChild.class, false);
        assertEquals("Not Inherited size should be 3", 3, array.length);
        assertEquals("Not Inherited Child Attribute", "Luke", array[0].value());
        assertEquals("Not Inherited Child Attribute", "Skywalker", array[1].value());
        assertEquals("Not Inherited Parent Attribute", "JarJar", array[2].value());
    }
}
