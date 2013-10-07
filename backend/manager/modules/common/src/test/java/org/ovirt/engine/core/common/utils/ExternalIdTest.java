package org.ovirt.engine.core.common.utils;

import static junit.framework.Assert.assertEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ExternalIdTest {

    /**
     * Check that an external id can be serialized and deserialized using JSON.
     */
    @Test
    public void testJsonSerializationAndDeserialization() throws Exception {
        ExternalId original = new ExternalId(0, 1, 2, 3);
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(original);
        ExternalId deserialized = mapper.readValue(value, ExternalId.class);
        assertEquals(original, deserialized);
    }

}
