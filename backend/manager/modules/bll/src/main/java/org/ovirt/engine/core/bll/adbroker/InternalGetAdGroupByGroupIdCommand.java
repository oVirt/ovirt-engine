package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

public class InternalGetAdGroupByGroupIdCommand extends InternalBrokerCommandBase {
    private Guid getGroupId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public InternalGetAdGroupByGroupIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        LdapGroup group = InternalBrokerUtils.getGroupByGroupGuid(getGroupId());
        setReturnValue(group);
        if (group != null) {
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }

}
