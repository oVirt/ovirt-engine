package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVdcActionParametersBaseMixIn extends VdcActionParametersBase {
    /**
     * Ignore this method since Jackson will try to recursively dereference it and fail to serialize.
     */
    @JsonIgnore
    @Override
    public abstract VdcActionParametersBase getParentParameters();

    @JsonIgnore
    @Override
    public abstract ArrayList<VdcActionParametersBase> getImagesParameters();

    @JsonDeserialize (using=VdcActionTypeDeserializer.class)
    @Override
    public abstract void setParentCommand(VdcActionType value);

    @JsonDeserialize (using=VdcActionTypeDeserializer.class)
    @Override
    public abstract void setCommandType(VdcActionType commandType);
}
