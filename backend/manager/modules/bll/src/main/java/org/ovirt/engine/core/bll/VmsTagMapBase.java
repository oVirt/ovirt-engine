package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmsTagMapBase<T extends AttachEntityToTagParameters> extends TagsCommandBase<T> {
    protected java.util.ArrayList<Guid> getVmsList() {
        return getParameters().getEntitiesId();
    }

    public VmsTagMapBase(T parameters) {
        super(parameters);
    }

}
