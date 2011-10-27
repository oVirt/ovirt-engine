package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.*;

import java.util.ArrayList;
import org.ovirt.engine.core.utils.jwin32.*;

public class LUGetAdUserByUserIdListCommand extends LUBrokerCommandBase {
    public LUGetAdUserByUserIdListCommand(LdapSearchByIdListParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        ArrayList<AdUser> users = getAdUsers();
        setReturnValue(users);
    }
}
