package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.ad_groups;
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
        ad_groups group = InternalBrokerUtils.getGroupByGroupGuid(getGroupId());
        setReturnValue(group);
        if (group != null) {
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }

}
