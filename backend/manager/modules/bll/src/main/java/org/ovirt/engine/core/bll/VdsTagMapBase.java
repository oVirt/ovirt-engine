package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class VdsTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected ArrayList<Guid> getVdsList() {
        return getParameters().getEntitiesId();
    }

    public VdsTagMapBase(T parameters) {
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
