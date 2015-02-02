package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class IdAndNameQueryParameters extends IdQueryParameters {

    private String name;

    private IdAndNameQueryParameters() {
    }

    public IdAndNameQueryParameters(Guid id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
