package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Use this to test the integer delegate
 *
 *
 */
public class StorageDomainFieldAutoCompleterTest {

    @Test
    public void testValidate() {
        StorageDomainFieldAutoCompleter comp = new StorageDomainFieldAutoCompleter();
        assertTrue("1", comp.validateFieldValue("SIZE", "1"));
        assertTrue("123", comp.validateFieldValue("SIZE", "123"));
        assertFalse("JarJar", comp.validateFieldValue("SIZE", "JarJar"));
    }

}
