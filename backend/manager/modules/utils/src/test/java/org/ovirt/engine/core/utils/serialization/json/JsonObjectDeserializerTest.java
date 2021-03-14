package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.utils.SerializationException;

/**
 * Tests for {@link JsonObjectDeserializer}.
 */
public class JsonObjectDeserializerTest {

    private enum Color {
        UNKNOWN,
        BLUE,
        RED
    };

    private enum Kind {
        UNKNOWN,
        PERSON,
        ANIMAL
    };

    @Test
    public void testSerialize() {
        JsonSerializablePojo serializablePojo = new JsonSerializablePojo();

        assertEquals(serializablePojo,
                new JsonObjectDeserializer().deserialize(
                        serializablePojo.toJsonForm(false), JsonSerializablePojo.class));
    }

    @Test
    public void testNullSerialize() {
        assertNull(new JsonObjectDeserializer().deserialize(null, JsonSerializablePojo.class));
    }

    @Test
    public void testDeserializeMap() {
        checkJson("{\"success\":true}");
        checkJson("{\"success\":true, \"problem\": \"none\"}");
    }

    @Test
    public void testDeserializeMapFailWithSingleQuote() {
        assertThrows(SerializationException.class, () -> checkJson("{'success':true}"));
    }

    @Test
    public void testDeserializeMapFailWithNoQuote() {
        assertThrows(SerializationException.class, () -> checkJson("{success:true}"));
    }

    @Test
    public void testDeserializeMapFailWithBadTrue() {
        assertThrows(SerializationException.class, () -> checkJson("{\"success\":treue}"));
    }

    @Test
    public void testDeserializeVdcActionParameters() {
        assertThrows(SerializationException.class,
                () -> new JsonObjectDeserializer().deserialize(getVdcActionParamsJson(), ActionParametersBase.class));
    }

    private String getVdcActionParamsJson() {
        StringBuilder buf = new StringBuilder("");
        buf.append("\"@class\" : \"org.ovirt.engine.core.common.action.ActionParametersBase\",");
        buf.append("\"commandId\" : null,");
        buf.append("\"parametersCurrentUser\" : null,");
        buf.append("\"compensationEnabled\" : false,");
        buf.append("\"parentCommand\" : \"Unknown\",");
        buf.append("\"commandType\" : \"Unknown\",");
        buf.append("\"multipleAction\" : false,");
        buf.append("\"entityInfo\" : null,");
        buf.append("\"taskGroupSuccess\" : true,");
        buf.append("\"vdsmTaskIds\" : null,");
        buf.append("\"correlationId\" : null,");
        buf.append("\"jobId\" : null,");
        buf.append("\"stepId\" : null,");
        buf.append("\"lockProperties\" : {");
        buf.append("\"scope\" : None,");
        buf.append("\"wait\" : true");
        buf.append("},");
        buf.append("\"shouldBeLogged\" : true,");
        buf.append("\"executionReason\" : \"REGULAR_FLOW\",");
        buf.append("\"transactionScopeOption\" : \"Required\",");
        buf.append("\"sessionId\" : \"\"");
        return buf.toString();
    }
    private void checkJson(String json) {
        @SuppressWarnings("unchecked")
        final Map<String, Boolean> map = new JsonObjectDeserializer().deserialize(json, HashMap.class);
        assertTrue(map.get("success"));
    }

    @Test
    public void testEnumDeserialization() {
        Color color = new JsonObjectDeserializer().deserialize("\"RED\"", Color.class);
        assertEquals(Color.RED, color);
    }

    @Test
    public void testParameterMapDeserialization() {
        StringBuilder buf = new StringBuilder("");
        buf.append("{");
        buf.append("\"NEXT_COMMAND_TYPE\" : [ \"org.ovirt.engine.core.common.action.ActionType\", \"DestroyImage\" ]");
        buf.append("}");
        Map<String, Serializable> data = new JsonObjectDeserializer().deserialize(buf.toString(), HashMap.class);
        assertNotNull(data);
        assertEquals(ActionType.DestroyImage, data.get("NEXT_COMMAND_TYPE"));
    }

    @Test
    public void testEnumDeserializationLiteralDoesNotExist() {
        Kind kind = new JsonObjectDeserializer().deserialize("\"RED\"", Kind.class);
        assertNull(kind);
    }

}
