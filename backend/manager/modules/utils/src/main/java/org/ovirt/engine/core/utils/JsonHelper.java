package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonHelper {
    private JsonHelper() {
    }

    public static String mapToJson(Map<String, Object> input)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator generator = factory.createJsonGenerator(writer);
        generator.useDefaultPrettyPrinter();
        mapper.writeValue(generator, input);
        return writer.toString();
    }
}
