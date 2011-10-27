package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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

    private void initGroupFromDb(ad_groups group) {
        ad_groups dbGroup = DbFacade.getInstance().getAdGroupDAO().get(group.getid());
    }
}
