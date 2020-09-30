package org.ovirt.engine.core.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.TextNode;
import org.junit.jupiter.api.Test;

public class DbUserJsonSerializationTest {

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, JsonNode> allTypesAsJsonNodes() {
        Map<String, JsonNode> result = new HashMap<>();
        result.put("null", NullNode.getInstance());
        result.put("string", new TextNode("string"));
        result.put("number", new IntNode(1));
        result.put("boolean", BooleanNode.getTrue());
        result.put("object", mapper.createObjectNode().putAll(Map.of("foo", new TextNode("bar"))));
        result.put("array", mapper.createArrayNode().addAll(List.of(new TextNode("foo"))));
        return result;
    }

    private Map<String, String> allTypesAsJsonStrings = Map.of(
            "null", "null",
            "string", "\"string\"",
            "number", "1",
            "boolean", "true",
            "object", "{\"foo\":\"bar\"}",
            "array", "[\"foo\"]"
    );

    private String jsonObjectWithAllTypes = "{"
            + "\"null\":null,"
            + "\"string\":\"string\","
            + "\"number\":1,"
            + "\"boolean\":true,"
            + "\"object\":{\"foo\":\"bar\"},"
            + "\"array\":[\"foo\"]"
            + "}";

    @Test
    void deserializeTopLevelKeys() {
        assertThat(DbUserDaoImpl.toNestedMap(allTypesAsJsonStrings))
                .isEqualTo(allTypesAsJsonNodes());
    }

    @Test
    void serializeAndDeserializeTopLevelJsonKeys() {
        Map<String, String> serialized = DbUserDaoImpl.toStringMap(jsonObjectWithAllTypes);
        Map<String, Object> deserialized = DbUserDaoImpl.toNestedMap(serialized);
        assertThat(deserialized).isEqualTo(allTypesAsJsonNodes());
    }

    @Test
    void deserializeNestedJson() {
        assertThat(DbUserDaoImpl.toNestedMap(Map.of("nested", jsonObjectWithAllTypes)))
                .isEqualTo(Map.of("nested", mapper.createObjectNode().putAll(allTypesAsJsonNodes())));
    }

    @Test
    void serializeAndDeserializeNestedJson() {
        Map<String, String> serialized = DbUserDaoImpl.toStringMap("{\"nested\":" + jsonObjectWithAllTypes + "}");
        Map<String, Object> deserialized = DbUserDaoImpl.toNestedMap(serialized);
        assertThat(deserialized).isEqualTo(Map.of("nested", mapper.createObjectNode().putAll(allTypesAsJsonNodes())));
    }
}
