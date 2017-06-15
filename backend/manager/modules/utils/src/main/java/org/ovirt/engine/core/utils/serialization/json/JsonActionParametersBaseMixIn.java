package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonActionParametersBaseMixIn extends ActionParametersBase {
    /**
     * Ignore this method since Jackson will try to recursively dereference it and fail to serialize.
     */
    @JsonIgnore
    @Override
    public abstract ActionParametersBase getParentParameters();

    @JsonIgnore
    @Override
    public abstract ArrayList<ActionParametersBase> getImagesParameters();

    @JsonDeserialize (using=ActionTypeDeserializer.class)
    @Override
    public abstract void setParentCommand(ActionType value);

    @JsonDeserialize (using=ActionTypeDeserializer.class)
    @Override
    public abstract void setCommandType(ActionType commandType);
}
