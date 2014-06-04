package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.action.RunVmParams;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonRunVmParamsMixIn extends RunVmParams {

    @JsonIgnore
    public abstract Boolean getBootMenuEnabled();

}
