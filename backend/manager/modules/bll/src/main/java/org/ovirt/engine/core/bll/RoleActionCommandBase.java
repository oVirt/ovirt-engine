package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RolesActionMapParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("ActionName") })
public abstract class RoleActionCommandBase<T extends RolesActionMapParameters> extends RolesCommandBase<T> {
    public RoleActionCommandBase(T parameters) {
        super(parameters);

    }

    public String getActionName() {
        /**
         * TODO: Vitaly change it when roles resx will be complete
         */
        try {
            VdcActionType actionType = getParameters().getRoleActionMap().getaction_id();
            return actionType.toString();
        } catch (java.lang.Exception e) {
            return VdcActionType.Unknown.toString();
        }
    }

    protected static boolean IsActionExists(VdcActionType actionId) {
        String s = actionId.name();
        return (s != null);
    }
}
