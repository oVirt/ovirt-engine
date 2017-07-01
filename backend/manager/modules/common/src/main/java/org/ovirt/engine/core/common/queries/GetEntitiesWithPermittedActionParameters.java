package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class GetEntitiesWithPermittedActionParameters extends QueryParametersBase {

    private static final long serialVersionUID = -5471065721016495847L;
    private ActionGroup actionGroup;

    public GetEntitiesWithPermittedActionParameters() {
    }

    public GetEntitiesWithPermittedActionParameters(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

}
