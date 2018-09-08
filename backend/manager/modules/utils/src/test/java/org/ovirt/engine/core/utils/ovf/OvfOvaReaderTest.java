package org.ovirt.engine.core.utils.ovf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
}
