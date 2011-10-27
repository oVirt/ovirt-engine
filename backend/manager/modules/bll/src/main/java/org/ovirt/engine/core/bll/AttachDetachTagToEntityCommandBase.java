package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class AttachDetachTagToEntityCommandBase<T extends AttachEntityToTagParameters> extends
        TagsCommandBase<T> {
    public AttachDetachTagToEntityCommandBase(T parameters) {
        super(parameters);
    }

    protected java.util.ArrayList<Guid> getEntitiesList() {
        return getParameters().getEntitiesId();
    }

}
