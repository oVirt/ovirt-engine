package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import junit.framework.Assert;

import org.apache.commons.lang.SerializationException;
import org.junit.Test;

/**
 * Tests for {@link JsonObjectDeserializer}.
 */
public class JsonObjectDeserializerTest {

    @Test
    public void testSerialize() {
        JsonSerializablePojo serializablePojo = new JsonSerializablePojo();

        assertEquals(serializablePojo,
                new JsonObjectDeserializer().deserialize(
                        serializablePojo.toJsonForm(false), JsonSerializablePojo.class));
    }

    @Test
    public void testNullSerialize() {
        assertEquals(null, new JsonObjectDeserializer().deserialize(null, JsonSerializablePojo.class));
    }

    @Test
    public void testDeserializeMap() {
        checkJson("{\"success\":true}");
        checkJson("{\"success\":true, \"problem\": \"none\"}");
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeMapFailWithSingleQuote() {
        checkJson("{'success':true}");
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeMapFailWithNoQuote() {
        checkJson("{success:true}");
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeMapFailWithBadTrue() {
        checkJson("{\"success\":treue}");
    }

    private void checkJson(String json) {
        @SuppressWarnings("unchecked")
        final HashMap<String, Boolean> map =
                new JsonObjectDeserializer().deserialize(json, HashMap.class);
        Assert.assertTrue(map.get("success"));
    }

}
