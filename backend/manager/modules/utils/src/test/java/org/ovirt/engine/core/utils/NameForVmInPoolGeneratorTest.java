package org.ovirt.engine.core.utils;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class NameForVmInPoolGeneratorTest {

    @Test
    public void validateGenerateVmNameStartsWithMask() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("???mypool");
        for (int i=1; i<17; ++i) {
            generator.generateVmName();
        }
        assertEquals("017mypool", generator.generateVmName());
    }

    @Test
    public void validateGenerateVmNameMaskInside() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("my???pool");
        assertEquals("my001pool", generator.generateVmName());
    }

    @Test
    public void validateGenerateVmNameEndsWithMask() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("mypool???");
        for (int i=1; i<101; ++i) {
            generator.generateVmName();
        }
        assertEquals("mypool101", generator.generateVmName());
    }

    @Test
    public void validateGenerateVmNameWithoutMask1() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("mypool");
        assertEquals("mypool-1", generator.generateVmName());
    }

    @Test
    public void validateGenerateVmNameWithoutMask2() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("mypool");
        for (int i=1; i<17; ++i) {
            generator.generateVmName();
        }
        assertEquals("mypool-17", generator.generateVmName());
    }

    @Test
    public void validateGenerateVmNameWithoutMask3() {
        NameForVmInPoolGenerator generator = createNameForVmInPoolGenerator("mypool");
        for (int i=1; i<101; ++i) {
            generator.generateVmName();
        }
        assertEquals("mypool-101", generator.generateVmName());
    }

    private NameForVmInPoolGenerator createNameForVmInPoolGenerator(String poolName) {
        return new NameForVmInPoolGenerator(poolName.replace('?', VmPool.MASK_CHARACTER));
    }
}
