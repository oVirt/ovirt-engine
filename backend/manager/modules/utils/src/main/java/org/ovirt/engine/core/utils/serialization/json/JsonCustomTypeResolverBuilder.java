package org.ovirt.engine.core.utils.serialization.json;

import java.util.Collection;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

public class JsonCustomTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

    public JsonCustomTypeResolverBuilder() {
        this(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
    }

    public JsonCustomTypeResolverBuilder(ObjectMapper.DefaultTyping typing) {
        super(typing);
    }

    /**
     * Custom type resolver is used only for non concrete collections and maps.
     * @param t the JavaType of the object
     */
    @Override
    public boolean useForType(JavaType t) {
        if ((t.isCollectionLikeType() || t.isMapLikeType()) && !t.isConcrete()) {
            return true;
        }

        return false;
    }

    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config,
                                        JavaType baseType,
                                        Collection<NamedType> subtypes,
                                        boolean forSer,
                                        boolean forDeser) {
        TypeIdResolver idResolver;
        if (baseType.isCollectionLikeType()) {
            // provide a custom id resolver for collections
            idResolver = new JsonCollectionIdResolver(baseType, config.getTypeFactory());
        } else if (baseType.isMapLikeType()) {
            // provide a custom id resolver for maps
            idResolver = new JsonMapIdResolver(baseType, config.getTypeFactory());
        } else {
            // use the default resolver
            idResolver = super.idResolver(config, baseType, subtypes, forSer, forDeser);
        }
        return idResolver;
    }
}
