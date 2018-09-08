package org.ovirt.engine.core.utils.ovf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class OvfOvaReaderTest {

    @Test
    public void capacityUnitInBytes() {
        assertEquals(1, OvfOvaReader.parseCapacityUnit("byte"));
    }

    @Test
    public void capacityUnitInTwoBytes() {
        assertEquals(2, OvfOvaReader.parseCapacityUnit("byte * 2"));
    }

    @Test
    public void capacityUnitInKiloBytes() {
        assertEquals(1024, OvfOvaReader.parseCapacityUnit("byte * 2^10"));
    }

    @Test
    public void capacityUnitInFourKiloBytes() {
        assertEquals(4096, OvfOvaReader.parseCapacityUnit("byte * 4 * 2^10"));
    }

    @Test
    public void capacityUnitEmpty() {
        assertThrows(IllegalArgumentException.class, () -> OvfOvaReader.parseCapacityUnit(""));
    }

    @Test
    public void capacityUnitNotInBytes() {
        assertThrows(IllegalArgumentException.class, () -> OvfOvaReader.parseCapacityUnit("block"));
    }

    @Test
    public void capacityUnitInvalidOperator() {
        assertThrows(IllegalArgumentException.class, () -> OvfOvaReader.parseCapacityUnit("byte + 2^10"));
    }
}
