package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;

public class InternalSearchUserByQueryCommand extends InternalBrokerCommandBase {

    public InternalSearchUserByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        List<LdapUser> userList = InternalBrokerUtils.getAllUsers();
        setReturnValue(userList);
        setSucceeded(true);
    }

}
