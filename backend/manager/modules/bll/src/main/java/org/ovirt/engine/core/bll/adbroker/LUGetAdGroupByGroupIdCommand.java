package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.common.businessentities.ad_groups;

public class LUGetAdGroupByGroupIdCommand extends LUBrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(LUGetAdUserByUserIdCommand.class);

    private Guid getGroupId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public LUGetAdGroupByGroupIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        log.debug("ExecuteQuery Entry, group ID=" + getGroupId().toString());
        for (ad_groups group : getAdGroups()) {
            if (group.getid().equals(getGroupId())) {
                setReturnValue(group);
                setSucceeded(true);
                break;
            }
        }
    }
}
