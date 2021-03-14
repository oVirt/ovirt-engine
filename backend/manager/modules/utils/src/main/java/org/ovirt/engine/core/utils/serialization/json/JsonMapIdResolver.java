package org.ovirt.engine.core.utils.serialization.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonMapIdResolver extends ClassNameIdResolver {

    private static Map<String, String> relacementIds = new HashMap<>();

    static {
        relacementIds.put("java.util.Collections$SingletonMap", "java.util.HashMap");
        relacementIds.put("java.util.Collections$UnmodifiableMap", "java.util.HashMap");
    }

    public JsonMapIdResolver() {
        this(null, null);
    }

    protected JsonMapIdResolver(JavaType baseType, TypeFactory typeFactory) {
        super(baseType, typeFactory);
    }

    @Override
    public String idFromValue(Object o) {
        String id = super.idFromValue(o);
        // return a replacement id if it exists
        return relacementIds.getOrDefault(id, id);
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        String id = super.idFromValueAndType(o, aClass);
        // return a replacement id if it exists
        return relacementIds.getOrDefault(id, id);
    }
}
