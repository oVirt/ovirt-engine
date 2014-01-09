package org.ovirt.engine.core.common.utils;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ExternalIdTest {
    /**
     * This array of bytes will be used as the expected result of multiple tests.
     */
    private static byte[] BYTES = {
        (byte) 0xd8, (byte) 0x99, (byte) 0xf1, (byte) 0xa3, (byte) 0x32, (byte) 0xa7, (byte) 0xf8, (byte) 0xcc,
        (byte) 0x92, (byte) 0xfd, (byte) 0x70, (byte) 0x10, (byte) 0x12, (byte) 0x1d, (byte) 0xa4, (byte) 0xda
    };

    /**
     * Check that creating ids from hexadecimal strings works as expected for all the possible byte values.
     */
    @Test
    public void testAllByteValuesFromHex() throws Exception {
        for (int i = 0; i < 256; i++) {
            String text = String.format("%02x", i);
            byte[] bytes = new byte[] { (byte) i };
            ExternalId id = ExternalId.fromHex(text);
            assertArrayEquals(bytes, id.getBytes());
        }
    }

    /**
     * Check that colons are ignored.
     */
    @Test
    public void testColonsIgnored() throws Exception {
        ExternalId id = ExternalId.fromHex("d8:99:f1:a3:32:a7:f8:cc:92:fd:70:10:12:1d:a4:da");
        assertArrayEquals(BYTES, id.getBytes());
    }

    /**
     * Check that dashes are ignored.
     */
    @Test
    public void testDashesIgnored() throws Exception {
        ExternalId id = ExternalId.fromHex("d899f1a3-32a7-f8cc-92fd-7010121da4da");
        assertArrayEquals(BYTES, id.getBytes());
    }

    /**
     * Check that leading spaces are ignored.
     */
    @Test
    public void testLeadingSpacesIgnored() throws Exception {
        ExternalId id = ExternalId.fromHex(" d899f1a332a7f8cc92fd7010121da4da");
        assertArrayEquals(BYTES, id.getBytes());
    }

    /**
     * Check that trailing spaces are ignored.
     */
    @Test
    public void testTrailingSpacesIgnored() throws Exception {
        ExternalId id = ExternalId.fromHex("d899f1a332a7f8cc92fd7010121da4da ");
        assertArrayEquals(BYTES, id.getBytes());
    }

    /**
     * Check that an external id can be serialized and deserialized using JSON.
     */
    @Test
    public void testJsonSerializationAndDeserialization() throws Exception {
        ExternalId original = new ExternalId(BYTES);
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(original);
        ExternalId deserialized = mapper.readValue(value, ExternalId.class);
        assertEquals(original, deserialized);
    }
}
