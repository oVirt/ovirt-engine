package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VMStatus;

public class ObjectIdentityCheckerTest {
    @Test
    public void testIsUpdateable() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse(oic.isFieldUpdatable("name"), "Should be false by default");
        oic.addPermittedFields("name");
        assertTrue(oic.isFieldUpdatable("name"), "Should be true now");
    }

    @Test
    public void testNoChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();

        Collection<String> changes = ObjectIdentityChecker.getChangedFields(jedi1, jedi2);
        assertEquals(0, changes.size(), "Should be no changes");
    }

    @Test
    public void testChanges() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side

        Collection<String> changes = ObjectIdentityChecker.getChangedFields(jedi1, jedi2);
        assertEquals(1, changes.size(), "Should be 1 changes");
    }

    @Test
    public void testIsFieldsUpdated() {
        Jedi jedi1 = new Jedi();
        Jedi jedi2 = new Jedi();
        jedi2.saberColor = "red"; // Gone to the dark side
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);

        boolean changed = oic.isFieldsUpdated(jedi1, jedi2, Collections.singletonList("name"));
        assertFalse(changed, "No Change");
        changed = oic.isFieldsUpdated(jedi1, jedi2, Collections.singletonList("saberColor"));
        assertTrue(changed, "1 Change");
    }

    @Test
    public void testHotsetUpdateableWhenHotsetRequested() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addHotsetField("name", EnumSet.of(VMStatus.Up));
        assertTrue(oic.isFieldUpdatable(VMStatus.Up, "name", null, true),
                "hot set requested for hot set fields should be true in state Up");
    }

    @Test
    public void testHotsetUpdateableWhenHotsetRequestedAndStatusOtherThanHotSettable() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addHotsetField("name", EnumSet.of(VMStatus.Up));
        assertFalse(oic.isFieldUpdatable(null, "name", null, true),
                "hot set requested for hot set fields should be false in state other than Up");
    }

    @Test
    public void testHotsetNotUpdateableWhenHotsetNotRequested() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        assertFalse(oic.isFieldUpdatable("name"), "Should be false by default");
        oic.addHotsetField("name", EnumSet.of(VMStatus.Up));
        assertFalse(oic.isFieldUpdatable(null, "name", null, false),
                "hot set not requested should return false even if field is hot set");
    }

    @Test
    public void testHotsetUpdateableWhenHotsetRequestedWithStatus() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addField(VMStatus.Down, "name");
        oic.addHotsetField("name", EnumSet.of(VMStatus.Up));
        assertTrue(oic.isFieldUpdatable(VMStatus.Down, "name", null, true),
                "hot set requested for hot set fields should be true");
    }

    @Test
    public void testHotsetUpdateableWhenHotsetNotRequestedWithStatus() {
        ObjectIdentityChecker oic = new ObjectIdentityChecker(Jedi.class);
        oic.addField(VMStatus.Down, "name");
        oic.addHotsetField("name", EnumSet.of(VMStatus.Up));
        assertTrue(oic.isFieldUpdatable(VMStatus.Down, "name", null, false),
                "hot set not requested field should be updateable according to status");
    }
}
