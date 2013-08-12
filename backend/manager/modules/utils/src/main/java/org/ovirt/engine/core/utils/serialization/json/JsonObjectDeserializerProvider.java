package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.type.JavaType;

public class JsonObjectDeserializerProvider extends StdDeserializerProvider {

    public JsonObjectDeserializerProvider() {
        super(new JsonObjectDeserializerFactory());
    }

    public JsonDeserializer<Object> findValueDeserializer(DeserializationConfig config,
                                                          JavaType propertyType,
                                                          BeanProperty property)
            throws JsonMappingException {
        return super.findValueDeserializer(config, propertyType, property);
    }
}
