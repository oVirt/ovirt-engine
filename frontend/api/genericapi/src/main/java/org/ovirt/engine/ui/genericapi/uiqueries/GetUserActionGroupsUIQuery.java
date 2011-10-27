package org.ovirt.engine.ui.genericapi.uiqueries;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;

public class GetUserActionGroupsUIQuery extends UIQueryBase{

    public GetUserActionGroupsUIQuery(UIQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    public void runQuery() {
        returnValue.setReturnValue(ActionGroup.getAllUserActionGroups());
    }

}
