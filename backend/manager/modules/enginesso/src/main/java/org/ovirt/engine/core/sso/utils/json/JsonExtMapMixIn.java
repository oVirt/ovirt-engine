package org.ovirt.engine.core.sso.utils.json;

import org.ovirt.engine.api.extensions.ExtMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSerialize(keyUsing = JsonExtKeySerializer.class)
@JsonDeserialize(keyUsing = JsonExtKeyDeserializer.class)
public abstract class JsonExtMapMixIn extends ExtMap {
}
