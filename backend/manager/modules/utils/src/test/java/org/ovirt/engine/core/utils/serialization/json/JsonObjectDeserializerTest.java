package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationException;
import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

/**
 * Tests for {@link JsonObjectDeserializer}.
 */
public class JsonObjectDeserializerTest {

    private static enum Color {
        UNKNOWN,
        BLUE,
        RED
    };

    private static enum Kind {
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

    @Test(expected = SerializationException.class)
    public void testDeserializeVdcActionParameters() {
        VdcActionParametersBase params = new JsonObjectDeserializer().deserialize(getVdcActionParamsJson(), VdcActionParametersBase.class);
        Assert.assertNotNull(params.getLockProperties());
        Assert.assertTrue(params.getLockProperties().isWait());
        Assert.assertEquals(params.getLockProperties().getScope(), Scope.None);
    }

    private String getVdcActionParamsJson() {
        StringBuilder buf = new StringBuilder("");
        buf.append("\"@class\" : \"org.ovirt.engine.core.common.action.VdcActionParametersBase\",");
        buf.append("\"commandId\" : null,");
        buf.append("\"parametersCurrentUser\" : null,");
        buf.append("\"compensationEnabled\" : false,");
        buf.append("\"parentCommand\" : \"Unknown\",");
        buf.append("\"commandType\" : \"Unknown\",");
        buf.append("\"multipleAction\" : false,");
        buf.append("\"entityInfo\" : null,");
        buf.append("\"taskGroupSuccess\" : true,");
        buf.append("\"vdsmTaskIds\" : null,");
        buf.append("\"executionIndex\" : 0,");
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
        final HashMap<String, Boolean> map =
                new JsonObjectDeserializer().deserialize(json, HashMap.class);
        Assert.assertTrue(map.get("success"));
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
        buf.append("\"NEXT_COMMAND_TYPE\" : [ \"org.ovirt.engine.core.common.action.VdcActionType\", \"DestroyImage\" ]");
        buf.append("}");
        Map<String, Serializable> data = new JsonObjectDeserializer().deserialize(buf.toString(), HashMap.class);
        assertNotNull(data);
        assertEquals(data.get("NEXT_COMMAND_TYPE"), VdcActionType.DestroyImage);
    }

    @Test
    public void testEnumDeserializationLiteralDoesNotExist() {
        Kind kind = new JsonObjectDeserializer().deserialize("\"RED\"", Kind.class);
        assertEquals(null, kind);
    }

}
