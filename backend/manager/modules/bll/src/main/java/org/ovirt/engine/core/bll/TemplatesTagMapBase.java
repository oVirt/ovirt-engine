package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;

public abstract class TemplatesTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected ArrayList<Guid> getTemplatesList() {
        return getParameters().getEntitiesId();
    }

    public TemplatesTagMapBase(T parameters) {
        super(parameters);
    }

}
