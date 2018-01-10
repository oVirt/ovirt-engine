package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;

public class JsonHelper {
    private JsonHelper() {
    }

    public static String objectToJson(Object input, boolean prettyPrint) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator generator = factory.createJsonGenerator(writer);
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
        try {
            return jsonToMap(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Json string \"%s\" cannot be parsed to a Map.", jsonString), e);
        }
    }

    public static Map<String, Object> jsonToMap(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        Map<String, Object> data = mapper.readValue(jsonString, type);
        return data;
    }

    public static List<String> jsonToList(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        List<String> data = mapper.readValue(jsonString, type);
        return data;
    }
}
