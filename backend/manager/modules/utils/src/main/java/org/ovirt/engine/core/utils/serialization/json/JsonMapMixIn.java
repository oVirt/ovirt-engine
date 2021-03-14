package org.ovirt.engine.core.utils.serialization.json;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;


@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_ARRAY)
@JsonTypeResolver(JsonCustomTypeResolverBuilder.class)
public abstract class JsonMapMixIn implements Collection {
}
