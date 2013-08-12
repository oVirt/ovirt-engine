package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.type.JavaType;

public class JsonObjectDeserializerFactory extends BeanDeserializerFactory {

    public JsonObjectDeserializerFactory() {
        super(null);
    }

    public JsonDeserializer<?> createEnumDeserializer(DeserializationConfig config,
                                                      DeserializerProvider p,
                                                      JavaType type, BeanProperty property)
            throws JsonMappingException {
        EnumResolver<?> resolver = EnumResolver.constructUnsafeUsingToString(type.getRawClass());
        return new JsonEnumDeserializer(resolver);
    }
}
