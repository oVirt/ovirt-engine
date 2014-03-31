/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

/**
 * @author yzaslavs
 */
public class UsersObjectIdQueryGenerator extends LdapQueryDataGeneratorBase<Guid> {

    public List<LdapQueryData> getLdapQueriesData(String domain) {
        int queryLimit = Config.<Integer> getValue(ConfigValues.MaxLDAPQueryPartsNumber);
        List<LdapQueryData> results = new ArrayList<LdapQueryData>();
        LdapQueryData subQueryData = new LdapQueryDataImpl();

        ArrayList<Object> filterParameters = new ArrayList<Object>();

        int counter = 0;
        for (Guid identifier : ldapIdentifiers) {

            filterParameters.add(identifier);
            // Checking if more than queryLimit query clauses were added to the query
            if (counter >= queryLimit) {
                // More than queryLimit query clauses were added to the query -
                // close the query, add it to the results, and start a new query
                subQueryData.setFilterParameters(filterParameters.toArray());
                subQueryData.setLdapQueryType(LdapQueryType.getUsersByUserGuids);
                subQueryData.setBaseDNParameters(null);
                subQueryData.setDomain(domain);
                results.add(subQueryData);
                subQueryData = new LdapQueryDataImpl();
                filterParameters = new ArrayList<Object>();
                counter = 0;
            }
            counter++;
        }

        if (!filterParameters.isEmpty()) {
            subQueryData.setFilterParameters(filterParameters.toArray());
            subQueryData.setLdapQueryType(LdapQueryType.getUsersByUserGuids);
            subQueryData.setBaseDNParameters(null);
            subQueryData.setDomain(domain);
            results.add(subQueryData);
        }

        return results;
    }

}
