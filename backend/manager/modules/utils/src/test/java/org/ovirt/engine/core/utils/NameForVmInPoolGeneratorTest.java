package org.ovirt.engine.core.utils;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class NameForVmInPoolGeneratorTest {

    @Test
    public void validateGenerateVmNameStartsWithMask() {
        assertEquals("017mypool", NameForVmInPoolGenerator.generateVmName("???mypool".replace('?', VmPool.MASK_CHARACTER), 17));
    }

    @Test
    public void validateGenerateVmNameMaskInside() {
        assertEquals("my001pool", NameForVmInPoolGenerator.generateVmName("my???pool".replace('?', VmPool.MASK_CHARACTER), 1));
    }

    @Test
    public void validateGenerateVmNameEndsWithMask() {
        assertEquals("mypool170", NameForVmInPoolGenerator.generateVmName("mypool???".replace('?', VmPool.MASK_CHARACTER), 170));
    }

    @Test
    public void validateGenerateVmNameWithoutMask1() {
        assertEquals("mypool-1", NameForVmInPoolGenerator.generateVmName("mypool", 1));
    }

    @Test
    public void validateGenerateVmNameWithoutMask2() {
        assertEquals("mypool-17", NameForVmInPoolGenerator.generateVmName("mypool", 17));
    }

    @Test
    public void validateGenerateVmNameWithoutMask3() {
        assertEquals("mypool-170", NameForVmInPoolGenerator.generateVmName("mypool", 170));
    }
}
