package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;

public class VdcActionTypeTest {
    private static final int bitSetSize = 5000;

    @Test
    public void testAuditLogTypeValueUniqueness() {
        BitSet bitset = new BitSet(bitSetSize);
        Set<Integer> nonUniqueValues = new TreeSet<>();

        for (VdcActionType vat : VdcActionType.values()) {
            if (bitset.get(vat.getValue())) {
                nonUniqueValues.add(vat.getValue());
            }
            else {
                bitset.set(vat.getValue());
            }
        }
        assertTrue("VdcActionType contains the following non unique values: " + nonUniqueValues, nonUniqueValues.isEmpty());
    }

    @Test
    public void testCommandClassExistence() {
        CommandEnumTestUtils.testCommandsExist(VdcActionType.class, vat -> CommandsFactory.getCommandClass(vat.name()));
    }

    @Test
    public void testCommandClassHasEnum() {
        CommandEnumTestUtils.testCommandClassHasEnum(VdcActionType.class, CommandBase.class, "Command");
    }
}
