package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Set;

import junit.framework.TestCase;

public class AuditLogHelperTest extends TestCase {
    public void testSimple() {
        Set<String> array = AuditLogHelper.getCustomLogFields(BaseClass.class, true);
        assertEquals("size should be 1", 1, array.size());
        array = AuditLogHelper.getCustomLogFields(BaseClass.class, false);
        assertEquals("Not Inherited size should be 1", 1, array.size());
    }

    public void testSingleChild() {
        Set<String> array = AuditLogHelper.getCustomLogFields(SingleChild.class, true);
        assertEquals("size should be 2", 2, array.size());
        assertTrue("Parent Attribute", array.contains("jarjar"));
        assertTrue("Child Attribute", array.contains("binks"));
        array = AuditLogHelper.getCustomLogFields(SingleChild.class, false);
        assertEquals("Not inherited size should be 1", 1, array.size());
        assertTrue("Not Inhertied Child Attribute", array.contains("binks"));
    }

    public void testMultiChild() {
        Set<String> array = AuditLogHelper.getCustomLogFields(MultiChild.class, true);
        assertEquals("size should be 3", 3, array.size());
        assertTrue("Parent Attribute", array.contains("jarjar"));
        assertTrue("Child Attribute", array.contains("luke"));
        assertTrue("Child Attribute", array.contains("skywalker"));
        array = AuditLogHelper.getCustomLogFields(MultiChild.class, false);
        assertEquals("Not Inherited size should be 3", 3, array.size());
        assertTrue("Not Inherited Child Attribute", array.contains("luke"));
        assertTrue("Not Inherited Child Attribute", array.contains("skywalker"));
        assertTrue("Not Inherited Parent Attribute", array.contains("jarjar"));
    }
}
