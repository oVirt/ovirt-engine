package org.ovirt.engine.core.utils;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class ObjectIdentityCheckerTest extends TestCase {
    public void testIsUpdateable() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse("Should be false by default", oic.IsFieldUpdatable("name"));
        oic.AddPermittedFields("name");
        assertTrue("Should be true now", oic.IsFieldUpdatable("name"));
    }

    public void testNoChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();

        List<String> changes = ObjectIdentityChecker.GetChangedFields(jedi1, jedi2);
        assertTrue("Should be no changes", changes.size() == 0);
    }

    public void testChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side

        List<String> changes = ObjectIdentityChecker.GetChangedFields(jedi1, jedi2);
        assertTrue("Should be 1 changes", changes.size() == 1);
    }

    public void testIsFieldsUpdated() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);

        boolean changed = oic.IsFieldsUpdated(jedi1, jedi2, Arrays.asList("name"));
        assertFalse("No Change", changed);
        changed = oic.IsFieldsUpdated(jedi1, jedi2, Arrays.asList("saberColor"));
        assertTrue("1 Change", changed);
    }
}
