package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

public class JsonHelper {
    private JsonHelper() {
    }

    public static String objectToJson(Object input, boolean prettyPrint) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator generator = factory.createGenerator(writer);
        if (prettyPrint) {
            generator.useDefaultPrettyPrinter();
        }
        mapper.writeValue(generator, input);
        return writer.toString();
    }

    public static String mapToJson(Map<String, Object> input, boolean prettyPrint)
            throws IOException {
        return objectToJson(input, prettyPrint);
    }

    public static String mapToJson(Map<String, Object> input)
            throws IOException {
        return mapToJson(input, true);
    }

    public static String mapToJsonUnchecked(Map<String, Object> input) {
        try {
            return mapToJson(input);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Object \"%s\" cannot be serialized to JSON.", input), e);
        }
    }

    public static Map<String, Object> jsonToMapUnchecked(String jsonString) {
        return jsonToMapUnchecked(jsonString, Object.class);
    }

    public static <T> Map<String, T> jsonToMapUnchecked(String jsonString, Class<T> clazz) {
        try {
            return jsonToMap(jsonString, clazz);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Json string \"%s\" cannot be parsed to a Map.", jsonString), e);
        }
    }

    public static Map<String, Object> jsonToMap(String jsonString) throws IOException {
        return jsonToMap(jsonString, Object.class);
    }

    public static <T> Map<String, T> jsonToMap(String jsonString, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, clazz);
        Map<String, T> data = mapper.readValue(jsonString, type);
        return data;
    }

    public static List<String> jsonToList(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        List<String> data = mapper.readValue(jsonString, type);
        return data;
    }

    public static void invokeIfExistsInt(JsonNode node, String fieldName, Consumer<Integer> consumer) {
        if (node.has(fieldName)) {
            consumer.accept(node.get(fieldName).asInt());
        }
    }

    public static void invokeIfExistsLong(JsonNode node, String fieldName, Consumer<Long> consumer) {
        if (node.has(fieldName)) {
            consumer.accept(node.get(fieldName).asLong());
        }
    }

    public static void invokeIfExistsBoolean(JsonNode node, String fieldName, Consumer<Boolean> consumer) {
        if (node.has(fieldName)) {
            consumer.accept(node.get(fieldName).asBoolean());
        }
    }

    public static void invokeIfExistsString(JsonNode node, String fieldName, Consumer<String> consumer) {
        if (node.has(fieldName)) {
            consumer.accept(node.get(fieldName).asText());
        }
    }

    public static <T> void invokeIfExistsStringTransformed(JsonNode node, String fieldName, Consumer<T> consumer, Function<String, T> transformer) {
        if (node.has(fieldName)) {
            consumer.accept(transformer.apply(node.get(fieldName).asText()));
        }
    }
}
