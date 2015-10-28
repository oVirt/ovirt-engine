package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class IdAndNameQueryParameters extends IdQueryParameters {

    private static final long serialVersionUID = -8358667691631277124L;

    private String name;

    @SuppressWarnings("unused")
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
