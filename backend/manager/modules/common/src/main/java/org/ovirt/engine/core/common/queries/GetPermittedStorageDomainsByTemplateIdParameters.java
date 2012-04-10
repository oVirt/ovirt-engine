package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class GetPermittedStorageDomainsByTemplateIdParameters extends GetStorageDomainsByVmTemplateIdQueryParameters {

    private static final long serialVersionUID = -6978898086667409681L;
    private ActionGroup actionGroup;

    public GetPermittedStorageDomainsByTemplateIdParameters() {
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

}
