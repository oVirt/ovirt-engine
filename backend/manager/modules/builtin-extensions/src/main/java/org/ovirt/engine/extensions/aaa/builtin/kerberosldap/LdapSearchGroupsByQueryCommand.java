package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.LdapGroup;

public class LdapSearchGroupsByQueryCommand extends LdapWithConfiguredCredentialsCommandBase {

    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        ArrayList<LdapGroup> groupList = new ArrayList<LdapGroup>();

        List<GroupSearchResult> searchResults = (List<GroupSearchResult>)directorySearcher.findAll(getLdapQueryData());
        {
            for (GroupSearchResult searchResult : searchResults) {
                String distinguishedName = searchResult.getDistinguishedName();
                List<String> memberOf = searchResult.getMemberOf();
                if (distinguishedName != null) {
                    String groupName = LdapBrokerUtils.generateGroupDisplayValue(searchResult.getDistinguishedName());
                    LdapGroup group = new LdapGroup(searchResult.getId(), groupName, getDomain(), distinguishedName, memberOf);
                    groupList.add(group);
                }
            }
        }
        setReturnValue(groupList);
        setSucceeded(true);
    }
}
