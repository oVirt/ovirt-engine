package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.ad_groups;

public class InternalSearchGroupsByQueryCommand extends InternalBrokerCommandBase {

    public InternalSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        java.util.List<ad_groups> groupList = InternalBrokerUtils.getAllGroups();
        setReturnValue(groupList);
        setSucceeded(true);
    }

}
