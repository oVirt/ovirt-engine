package org.ovirt.engine.core.bll.adbroker;

import java.util.List;
import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class InternalSearchGroupsByQueryCommand extends InternalBrokerCommandBase {

    public InternalSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        List<LdapGroup> groupList = InternalBrokerUtils.getAllGroups();
        setReturnValue(groupList);
        setSucceeded(true);
    }

}
