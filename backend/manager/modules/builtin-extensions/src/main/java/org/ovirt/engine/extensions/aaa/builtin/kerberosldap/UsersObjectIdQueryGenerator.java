/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.ovirt.engine.core.compat.Guid;

/**
 */
public class UsersObjectIdQueryGenerator extends LdapQueryDataGeneratorBase<Guid> {

    private Properties configuration;

    public UsersObjectIdQueryGenerator(Properties configuration) {
        this.configuration = configuration;
    }

    public List<LdapQueryData> getLdapQueriesData(String domain) {
        int queryLimit = Integer.parseInt(configuration.getProperty("config.LDAPQueryPartsNumber"));
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
