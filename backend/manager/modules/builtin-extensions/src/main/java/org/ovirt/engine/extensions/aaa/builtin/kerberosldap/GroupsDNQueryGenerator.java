package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Group query generated based on distinguished names
 */
public class GroupsDNQueryGenerator extends LdapQueryDataGeneratorBase<String> {

    public GroupsDNQueryGenerator(Set<String> groupIdentifiers) {
        super(groupIdentifiers);
    }

    public GroupsDNQueryGenerator() {
    }

    public List<LdapQueryData> getLdapQueriesData() {
        List<LdapQueryData> results = new ArrayList<LdapQueryData>();
        for (String groupIdentifier : ldapIdentifiers) {
            LdapQueryData queryData = new LdapQueryDataImpl();
            groupIdentifier = LdapBrokerUtils.hadleNameEscaping(groupIdentifier);
            queryData.setBaseDNParameters(new Object[] { groupIdentifier });
            queryData.setDomain(LdapBrokerUtils.getGroupDomain(groupIdentifier));
            String groupName = groupIdentifier.split(",", 2)[0].split("=")[1];
            queryData.setFilterParameters(new Object[] { groupName });
            queryData.setLdapQueryType(LdapQueryType.getGroupByDN);
            results.add(queryData);
        }
        return results;
    }
}
