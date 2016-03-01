package org.ovirt.engine.core.sso.utils;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ovirt.engine.api.extensions.ExtMap;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSerialize(keyUsing = JsonExtKeySerializer.class)
@JsonDeserialize(keyUsing = JsonExtKeyDeserializer.class)
public abstract class JsonExtMapMixIn extends ExtMap {
}
