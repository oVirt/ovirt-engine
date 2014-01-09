package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.utils.ExternalId;

public class InternalGetAdGroupByGroupIdCommand extends InternalBrokerCommandBase {
    private ExternalId getGroupId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public InternalGetAdGroupByGroupIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        LdapGroup group = InternalBrokerUtils.getGroupById(getGroupId());
        setReturnValue(group);
        if (group != null) {
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }

}
