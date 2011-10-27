package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;

//
// JTODO - this needs testing -- Livnat
//

public class LdapGetAdGroupByGroupIdCommand extends LdapWithConfiguredCredentialsCommandBase {
    private Guid getGroupId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public LdapGetAdGroupByGroupIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        Object group = null;

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setFilterParameters(new Object[] { getGroupId() });
        queryData.setLdapQueryType(LdapQueryType.getGroupByGuid);
        queryData.setDomain(getDomain());

        Object searchResult = directorySearcher.FindOne(queryData);

        if (searchResult != null) {
            GroupSearchResult result = (GroupSearchResult) searchResult;
            Guid groupId = result.getGuid();
            if (!getGroupId().equals(groupId)) {
                /**
                 * Cannot find group - group is Inactive
                 */
                group = new ad_groups(getGroupId());
            } else {
                String distinguishedName = result.getDistinguishedName();
                List<String> memberOf = result.getMemberOf();
                String groupName = LdapBrokerUtils.generateGroupDisplayValue(distinguishedName);
                group = new ad_groups(groupId, groupName, getDomain(), distinguishedName, memberOf);
            }
        }
        setReturnValue(group);
        setSucceeded(true);
    }
}
