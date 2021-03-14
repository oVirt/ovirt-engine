package org.ovirt.engine.core.utils.serialization.json;

import java.util.Collection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

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
                                        PolymorphicTypeValidator polymorphicTypeValidator,
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
            idResolver = super.idResolver(config, baseType, polymorphicTypeValidator, subtypes, forSer, forDeser);
        }
        return idResolver;
    }
}
