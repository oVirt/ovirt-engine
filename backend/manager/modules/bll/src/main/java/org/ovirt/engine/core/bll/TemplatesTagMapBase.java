package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class TemplatesTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected ArrayList<Guid> getTemplatesList() {
        return getParameters().getEntitiesId();
    }

    public TemplatesTagMapBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }
}
