package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class InternalSearchGroupsByQueryCommand extends InternalBrokerCommandBase {

    public InternalSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        java.util.List<LdapGroup> groupList = InternalBrokerUtils.getAllGroups();
        setReturnValue(groupList);
        setSucceeded(true);
    }

}
