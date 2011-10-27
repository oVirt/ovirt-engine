package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

/**
 * Use this to test the integer delegate
 *
 *
 */
public class StorageDomainFieldAutoCompleterTest extends TestCase {

    public void testValidate() {
        StorageDomainFieldAutoCompleter comp = new StorageDomainFieldAutoCompleter();
        assertTrue("1", comp.validateFieldValue("SIZE", "1"));
        assertTrue("123", comp.validateFieldValue("SIZE", "123"));
        assertFalse("JarJar", comp.validateFieldValue("SIZE", "JarJar"));
    }

}
