package org.ovirt.engine.core.utils.serialization.json;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonTypeResolver(JsonCustomTypeResolverBuilder.class)
public abstract class JsonCollectionMixIn implements Collection {
}
