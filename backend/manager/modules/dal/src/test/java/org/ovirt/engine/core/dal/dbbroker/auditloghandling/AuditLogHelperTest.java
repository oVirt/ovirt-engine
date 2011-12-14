package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.List;

import junit.framework.TestCase;

public class AuditLogHelperTest extends TestCase {
    public void testSimple() {
        List<String> array = AuditLogHelper.getCustomLogFields(BaseClass.class, true);
        assertEquals("size should be 1", 1, array.size());
        array = AuditLogHelper.getCustomLogFields(BaseClass.class, false);
        assertEquals("Not Inherited size should be 1", 1, array.size());
    }

    public void testSingleChild() {
        List<String> array = AuditLogHelper.getCustomLogFields(SingleChild.class, true);
        assertEquals("size should be 2", 2, array.size());
        assertEquals("Parent Attribute", "jarjar", array.get(0));
        assertEquals("Child Attribute", "binks", array.get(1));
        array = AuditLogHelper.getCustomLogFields(SingleChild.class, false);
        assertEquals("Not inherited size should be 1", 1, array.size());
        assertEquals("Not Inhertied Child Attribute", "binks", array.get(0));
    }

    public void testMultiChild() {
        List<String> array = AuditLogHelper.getCustomLogFields(MultiChild.class, true);
        assertEquals("size should be 3", 3, array.size());
        assertEquals("Parent Attribute", "jarjar", array.get(0));
        assertEquals("Child Attribute", "luke", array.get(1));
        assertEquals("Child Attribute", "skywalker", array.get(2));
        array = AuditLogHelper.getCustomLogFields(MultiChild.class, false);
        assertEquals("Not Inherited size should be 3", 3, array.size());
        assertEquals("Not Inherited Child Attribute", "luke", array.get(0));
        assertEquals("Not Inherited Child Attribute", "skywalker", array.get(1));
        assertEquals("Not Inherited Parent Attribute", "jarjar", array.get(2));
    }
}
