package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class IdsQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 575294540991590541L;

    private List<Guid> ids;

    public IdsQueryParameters() {
    }

    public IdsQueryParameters(List<Guid> ids) {
        this.ids = ids;
    }

    public List<Guid> getIds() {
        return ids;
    }

    public void setId(List<Guid> ids) {
        this.ids = ids;
    }
}
