package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class UserTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected ArrayList<Guid> getUserList() {
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
            addCanDoActionMessage(EngineMessage.TAGS_SPECIFY_TAG_IS_NOT_EXISTS);
            return false;
        }
    }
}
