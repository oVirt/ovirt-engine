package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class LdapSearchGroupsByQueryCommand extends LdapWithConfiguredCredentialsCommandBase {

    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        java.util.ArrayList<ad_groups> groupList = new java.util.ArrayList<ad_groups>();

        List<GroupSearchResult> searchResults = (List<GroupSearchResult>)directorySearcher.FindAll(getLdapQueryData());
        {
            for (GroupSearchResult searchResult : searchResults) {
                String distinguishedName = searchResult.getDistinguishedName();
                List<String> memberOf = searchResult.getMemberOf();
                if (distinguishedName != null) {
                    String groupName = LdapBrokerUtils.generateGroupDisplayValue(searchResult.getDistinguishedName());
                    ad_groups group = new ad_groups(searchResult.getGuid(), groupName, getDomain(),distinguishedName,memberOf);
                    initGroupFromDb(group);
                    groupList.add(group);
                }
            }
        }
        setReturnValue(groupList);
        setSucceeded(true);
    }

    private void initGroupFromDb(ad_groups group) {
        ad_groups dbGroup = DbFacade.getInstance().getAdGroupDao().get(group.getid());
    }
}
