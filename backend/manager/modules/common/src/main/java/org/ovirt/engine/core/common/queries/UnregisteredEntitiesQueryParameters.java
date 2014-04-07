package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class UnregisteredEntitiesQueryParameters extends IdQueryParameters {
    private static final long serialVersionUID = 8906662143775124331L;

    private List<Guid> entityGuidList;

    public UnregisteredEntitiesQueryParameters() {
    }

    public UnregisteredEntitiesQueryParameters(Guid id) {
        super(id);
    }

    public UnregisteredEntitiesQueryParameters(Guid id, List<Guid> entityGuidList) {
        super(id);
        this.entityGuidList = entityGuidList;
    }

    public List<Guid> getEntityGuidList() {
        return entityGuidList;
    }

    public void setEntityGuidList(List<Guid> entityGuidList) {
        this.entityGuidList = entityGuidList;
    }
}
