package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MigrationPoliciesValueHelperTest {

    private MigrationPoliciesValueHelper helper = new MigrationPoliciesValueHelper();

    @Test
    public void emptyIsNotValid() {
        assertEquals(helper.validate(null, "").isOk(), false);
    }

    @Test
    public void incorrectJsonNotValid() {
        assertEquals(helper.validate(null, "this is not a valid json").isOk(), false);
    }

    @Test
    public void notAnyValidJsonIsValid() {
        assertEquals(helper.validate(null, "{}").isOk(), false);
    }

    @Test
    public void listOfMigrationPoliciesIsValid() {
        assertEquals(helper.validate(null, helper.getExamplePolicy()).isOk(), true);
    }
}
