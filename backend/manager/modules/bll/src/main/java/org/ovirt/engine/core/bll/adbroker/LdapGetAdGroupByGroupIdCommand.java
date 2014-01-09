package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.utils.ExternalId;

//
// JTODO - this needs testing -- Livnat
//

public class LdapGetAdGroupByGroupIdCommand extends LdapWithConfiguredCredentialsCommandBase {
    private ExternalId getGroupId() {
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

        Object searchResult = directorySearcher.findOne(queryData);

        if (searchResult != null) {
            GroupSearchResult result = (GroupSearchResult) searchResult;
            ExternalId groupId = result.getId();

            if (ObjectUtils.notEqual(getGroupId(), groupId)) {
                /**
                 * Cannot find group - group is Inactive
                 */
                group = new LdapGroup(getGroupId());
            } else {
                String distinguishedName = result.getDistinguishedName();
                List<String> memberOf = result.getMemberOf();
                String groupName = LdapBrokerUtils.generateGroupDisplayValue(distinguishedName);
                group = new LdapGroup(groupId, groupName, getDomain(), distinguishedName, memberOf);
            }
        }
        setReturnValue(group);
        setSucceeded(true);
    }
}
