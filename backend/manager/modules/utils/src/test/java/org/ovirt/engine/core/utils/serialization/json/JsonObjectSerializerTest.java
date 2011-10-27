package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Tests for {@link JsonObjectSerializer}.
 */
public class JsonObjectSerializerTest {

    @Test
    public void testSerialize() {
        JsonSerializablePojo serializablePojo = new JsonSerializablePojo();

        assertEquals(serializablePojo.toJsonForm(true),
                new JsonObjectSerializer().serialize(serializablePojo).replaceAll("\\s", ""));
    }
}
