package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.AdUser;

public class InternalGetAdUserByUserNameCommand extends InternalBrokerCommandBase {
    private String getUserName() {
        return ((LdapSearchByUserNameParameters) getParameters()).getUserName();
    }

    public InternalGetAdUserByUserNameCommand(LdapSearchByUserNameParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        AdUser user = InternalBrokerUtils.getUserByUPN(getUserName());

        if (user != null) {
            setSucceeded(true);
            setReturnValue(user);
        } else {
            setSucceeded(false);
        }

    }

}
