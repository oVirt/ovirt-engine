package org.ovirt.engine.core.utils.kubevirt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UnitsTest {

    @Test
    public void readMemoryMB() {
        assertEquals(64, (int) Units.parse("64M"));
    }

    @Test
    public void readMemoryMiB() {
        assertEquals(419, (int) Units.parse("400Mi"));
    }

    @Test
    public void readMemoryGB() {
        assertEquals(8000, (int) Units.parse("8G"));
    }

    @Test
    public void readMemoryGiB() {
        assertEquals(2147, (int) Units.parse("2Gi"));
    }

    @Test
    public void readMemoryKB() {
        assertEquals(8, (int) Units.parse("8000K"));
    }

    @Test
    public void readMemoryKiB() {
        assertEquals(2, (int) Units.parse("2048Ki"));
    }
}
