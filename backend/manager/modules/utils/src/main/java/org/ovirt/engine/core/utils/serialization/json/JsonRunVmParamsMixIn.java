package org.ovirt.engine.core.utils.serialization.json;

import org.ovirt.engine.core.common.action.RunVmParams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonRunVmParamsMixIn extends RunVmParams {

    @JsonIgnore
    public abstract Boolean getBootMenuEnabled();

}
