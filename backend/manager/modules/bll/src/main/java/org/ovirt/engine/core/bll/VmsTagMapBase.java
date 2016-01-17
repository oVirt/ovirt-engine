package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmsTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected ArrayList<Guid> getVmsList() {
        return getParameters().getEntitiesId();
    }

    public VmsTagMapBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }
}
