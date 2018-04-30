package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MigrationPoliciesValueHelperTest {

    private MigrationPoliciesValueHelper helper = new MigrationPoliciesValueHelper();

    @Test
    public void emptyIsNotValid() {
        assertFalse(helper.validate(null, "").isOk());
    }

    @Test
    public void incorrectJsonNotValid() {
        assertFalse(helper.validate(null, "this is not a valid json").isOk());
    }

    @Test
    public void notAnyValidJsonIsValid() {
        assertFalse(helper.validate(null, "{}").isOk());
    }

    @Test
    public void listOfMigrationPoliciesIsValid() {
        assertTrue(helper.validate(null, helper.getExamplePolicy()).isOk());
    }
}
