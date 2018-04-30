package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Use this to test the integer delegate
 *
 *
 */
public class StorageDomainFieldAutoCompleterTest {

    @Test
    public void testValidate() {
        StorageDomainFieldAutoCompleter comp = new StorageDomainFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("SIZE", "1"), "1");
        assertTrue(comp.validateFieldValue("SIZE", "123"), "123");
        assertFalse(comp.validateFieldValue("SIZE", "JarJar"), "JarJar");
    }

}
