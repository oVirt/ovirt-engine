package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VMStatus;

public class ObjectIdentityCheckerTest {
    @Test
    public void testIsUpdateable() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse("Should be false by default", oic.isFieldUpdatable("name"));
        oic.addPermittedFields("name");
        assertTrue("Should be true now", oic.isFieldUpdatable("name"));
    }

    @Test
    public void testNoChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();

        List<String> changes = ObjectIdentityChecker.getChangedFields(jedi1, jedi2);
        assertTrue("Should be no changes", changes.size() == 0);
    }

    @Test
    public void testChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side

        List<String> changes = ObjectIdentityChecker.getChangedFields(jedi1, jedi2);
        assertTrue("Should be 1 changes", changes.size() == 1);
    }

    @Test
    public void testIsFieldsUpdated() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);

        boolean changed = oic.isFieldsUpdated(jedi1, jedi2, Arrays.asList("name"));
        assertFalse("No Change", changed);
        changed = oic.isFieldsUpdated(jedi1, jedi2, Arrays.asList("saberColor"));
        assertTrue("1 Change", changed);
    }

    @Test
    public void testHotsetUpdateableWhenHotsetRequested() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addHotsetFields("name");
        assertTrue("hot set requested for hot set fields should be true", oic.isFieldUpdatable(null, "name", null, true));
    }

    @Test
    public void testHotsetNotUpdateableWhenHotsetNotRequested() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse("Should be false by default", oic.isFieldUpdatable("name"));
        oic.addHotsetFields("name");
        assertFalse("hot set not requested should return false even if field is hot set", oic.isFieldUpdatable(null, "name", null, false));
    }

    @Test
    public void testHotsetUpdateableWhenHotsetRequestedWithStatus() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addField(VMStatus.Down, "name");
        oic.addHotsetFields("name");
        assertTrue("hot set requested for hot set fields should be true", oic.isFieldUpdatable(VMStatus.Down, "name", null, true));
    }

    @Test
    public void testHotsetUpdateableWhenHotsetNotRequestedWithStatus() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addField(VMStatus.Down, "name");
        oic.addHotsetFields("name");
        assertTrue("hot set not requested field should be updateable according to status", oic.isFieldUpdatable(VMStatus.Down, "name", null, false));
    }
}
