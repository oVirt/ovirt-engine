package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateAllTemplateDisksParameters extends VmTemplateParameters {

    private static final long serialVersionUID = -4591947977307945497L;

    private Boolean legal;
    private Boolean shared;

    public UpdateAllTemplateDisksParameters() {
    }

    public UpdateAllTemplateDisksParameters(Guid vmTemplateId, Boolean legal, Boolean shared) {
        super(vmTemplateId);
        this.legal = legal;
        this.shared = shared;
    }

    public Boolean getLegal() {
        return legal;
    }

    public void setLegal(Boolean legal) {
        this.legal = legal;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

}
