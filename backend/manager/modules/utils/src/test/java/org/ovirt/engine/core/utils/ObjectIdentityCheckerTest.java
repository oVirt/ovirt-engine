package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ObjectIdentityCheckerTest {
    @Test
    public void testIsUpdateable() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse("Should be false by default", oic.IsFieldUpdatable("name"));
        oic.AddPermittedFields("name");
        assertTrue("Should be true now", oic.IsFieldUpdatable("name"));
    }

    @Test
    public void testNoChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();

        List<String> changes = ObjectIdentityChecker.GetChangedFields(jedi1, jedi2);
        assertTrue("Should be no changes", changes.size() == 0);
    }

    @Test
    public void testChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side

        List<String> changes = ObjectIdentityChecker.GetChangedFields(jedi1, jedi2);
        assertTrue("Should be 1 changes", changes.size() == 1);
    }

    @Test
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
