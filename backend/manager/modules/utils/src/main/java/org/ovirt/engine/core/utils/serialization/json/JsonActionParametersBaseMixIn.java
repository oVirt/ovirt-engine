package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
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

    @JsonDeserialize(using=ActionTypeDeserializer.class)
    @Override
    public abstract void setParentCommand(ActionType value);

    @JsonDeserialize (using=ActionTypeDeserializer.class)
    @Override
    public abstract void setCommandType(ActionType commandType);
}
