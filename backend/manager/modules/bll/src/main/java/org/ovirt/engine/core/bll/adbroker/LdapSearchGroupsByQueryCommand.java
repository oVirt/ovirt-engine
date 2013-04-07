package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class LdapSearchGroupsByQueryCommand extends LdapWithConfiguredCredentialsCommandBase {

    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        java.util.ArrayList<LdapGroup> groupList = new java.util.ArrayList<LdapGroup>();

        List<GroupSearchResult> searchResults = (List<GroupSearchResult>)directorySearcher.FindAll(getLdapQueryData());
        {
            for (GroupSearchResult searchResult : searchResults) {
                String distinguishedName = searchResult.getDistinguishedName();
                List<String> memberOf = searchResult.getMemberOf();
                if (distinguishedName != null) {
                    String groupName = LdapBrokerUtils.generateGroupDisplayValue(searchResult.getDistinguishedName());
                    LdapGroup group = new LdapGroup(searchResult.getGuid(), groupName, getDomain(),distinguishedName,memberOf);
                    groupList.add(group);
                }
            }
        }
        setReturnValue(groupList);
        setSucceeded(true);
    }
}
