package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.utils.ExternalId;

public class InternalGetAdUserByUserIdCommand extends InternalBrokerCommandBase {
    private ExternalId getUserId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public InternalGetAdUserByUserIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        LdapUser user = InternalBrokerUtils.getUserById(getUserId());
        if (user != null) {
            setSucceeded(true);
            setReturnValue(user);
        } else {
            setSucceeded(false);
        }

    }

}
