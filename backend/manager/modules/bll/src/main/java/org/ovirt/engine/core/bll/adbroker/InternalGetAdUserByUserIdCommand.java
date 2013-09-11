package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;

public class InternalGetAdUserByUserIdCommand extends InternalBrokerCommandBase {
    private Guid getUserId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public InternalGetAdUserByUserIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        LdapUser user = InternalBrokerUtils.getUserByUserGuid(getUserId());

        if (user != null) {
            setSucceeded(true);
            setReturnValue(user);
        } else {
            setSucceeded(false);
        }

    }

}
