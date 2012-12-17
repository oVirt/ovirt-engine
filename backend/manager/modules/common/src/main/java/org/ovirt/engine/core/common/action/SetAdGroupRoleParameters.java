package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class SetAdGroupRoleParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = 3039952946152646137L;
    private boolean _isRestored;

    public SetAdGroupRoleParameters(LdapGroup adGroup, boolean isRestored) {
        super(adGroup);
        _isRestored = isRestored;
    }

    public boolean getIsRestored() {
        return _isRestored;
    }

    public SetAdGroupRoleParameters() {
    }
}
