package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.common.businessentities.ad_groups;

public class LUGetAdGroupByGroupIdCommand extends LUBrokerCommandBase {
    private static Log log = LogFactory.getLog(LUGetAdUserByUserIdCommand.class);

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
