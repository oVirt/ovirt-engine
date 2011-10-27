package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class UserGroupTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected java.util.ArrayList<Guid> getGroupList() {
        return getParameters().getEntitiesId();
    }

    public UserGroupTagMapBase(T parameters) {
        super(parameters);
    }

}
