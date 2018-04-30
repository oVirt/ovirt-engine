package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IntegerMultipleValueHelperTest {

    private IntegerMultipleValueHelper helper = new IntegerMultipleValueHelper();

    @Test
    public void empty() {
        assertFalse(helper.validate(null, "").isOk());
    }

    @Test
    public void oneInteger() {
        assertTrue(helper.validate(null, "1").isOk());
    }

    @Test
    public void oneString() {
        assertFalse(helper.validate(null, "X").isOk());
    }

    @Test
    public void twoInts() {
        assertTrue(helper.validate(null, "1,2").isOk());
    }

    @Test
    public void twoIntsOneLarge() {
        assertTrue(helper.validate(null, "1,12313131").isOk());
    }

    @Test
    public void startsWithComma() {
        assertFalse(helper.validate(null, ",1,2").isOk());
    }

    @Test
    public void endsWithComma() {
        assertFalse(helper.validate(null, "1,2,").isOk());
    }

    @Test
    public void doubleValue() {
        assertFalse(helper.validate(null, "1.1").isOk());
    }
}
