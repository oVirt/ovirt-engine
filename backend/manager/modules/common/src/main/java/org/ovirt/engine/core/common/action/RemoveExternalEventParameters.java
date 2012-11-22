package org.ovirt.engine.core.common.action;

public class RemoveExternalEventParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = 1L;
    private long id;

    public RemoveExternalEventParameters(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
