package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVdcActionParametersBaseMixIn extends VdcActionParametersBase {
    /**
     * Ignore this method since Jackson will try to recursively dereference it and fail to serialize.
     */
    @JsonIgnore
    @Override
    public abstract VdcActionParametersBase getParentParameters();

}
