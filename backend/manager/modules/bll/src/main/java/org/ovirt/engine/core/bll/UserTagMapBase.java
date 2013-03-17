package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class UserTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected java.util.ArrayList<Guid> getUserList() {
        return getParameters().getEntitiesId();
    }

    public UserTagMapBase(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getTagId() != null && !getTagId().equals(Guid.Empty)) {
            return true;
        } else {
            addCanDoActionMessage(VdcBllMessages.TAGS_SPECIFY_TAG_IS_NOT_EXISTS);
            return false;
        }
    }
}
