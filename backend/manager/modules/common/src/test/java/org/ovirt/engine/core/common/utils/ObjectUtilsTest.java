package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class ObjectUtilsTest {

    @Test
    public void testBigDecimalEqual() {
        assertTrue(ObjectUtils.bigDecimalEqual(new BigDecimal("0"), new BigDecimal("0.0")));
        assertTrue(ObjectUtils.bigDecimalEqual(new BigDecimal("0.0"), new BigDecimal("0.00")));
        assertTrue(ObjectUtils.bigDecimalEqual(new BigDecimal("1"), new BigDecimal("1.0")));
        assertTrue(ObjectUtils.bigDecimalEqual(new BigDecimal("0.1"), new BigDecimal("0.1")));
        assertTrue(ObjectUtils.bigDecimalEqual(null, null));
        assertFalse(ObjectUtils.bigDecimalEqual(null, new BigDecimal("0")));
        assertFalse(ObjectUtils.bigDecimalEqual(new BigDecimal("0"), null));
        assertFalse(ObjectUtils.bigDecimalEqual(new BigDecimal("1"), new BigDecimal("0")));
    }
}
