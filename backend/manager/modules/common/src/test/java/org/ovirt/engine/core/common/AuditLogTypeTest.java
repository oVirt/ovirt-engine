package org.ovirt.engine.core.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

public class AuditLogTypeTest {

    private static final int bitsetSize = 12000;

    @Test
    public void testAuditLogTypeValueUniqueness() {
        BitSet bitset = new BitSet(bitsetSize);
        Set<Integer> nonUniqueValues = new TreeSet<>();

        for (AuditLogType alt : AuditLogType.values()) {
            if (bitset.get(alt.getValue())) {
                nonUniqueValues.add(alt.getValue());
            } else {
                bitset.set(alt.getValue());
            }
        }
        assertTrue(nonUniqueValues.isEmpty(),
                "AuditLogType contains the following non unique values: " + nonUniqueValues);
    }
}
