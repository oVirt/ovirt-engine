package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ovirt.engine.api.extensions.ExtMap;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(keyUsing = JsonExtDeserializer.class)
public abstract class JsonExtMapMixIn extends ExtMap {
}
