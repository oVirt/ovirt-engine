package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.compat.*;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

import org.ovirt.engine.core.utils.jwin32.*;

public class LUGetAdUserByUserIdCommand extends LUBrokerCommandBase {
    private static Log log = LogFactory.getLog(LUGetAdUserByUserIdCommand.class);

    private Guid getUserId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public LUGetAdUserByUserIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        log.debug("ExecuteQuery Entry, user ID=" + getUserId().toString());
        for (AdUser user : getAdUsers()) {
            if (user.getUserId().equals(getUserId())) {
                setReturnValue(user);
                setSucceeded(true);
                break;
            }
        }
    }
}
