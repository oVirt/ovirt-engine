package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class VdsTagMapBase<T extends AttachVdsToTagParameters> extends TagsCommandBase<T> {
    protected java.util.ArrayList<Guid> getVdsList() {
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
