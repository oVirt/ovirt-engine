package org.ovirt.engine.core.itests.ldap;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.SearchQuery;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.utils.MockConfigRule;

public class LdapSearchQueryTest {

    @Rule
    public static final MockConfigRule mcr = new MockConfigRule();

    @Test
    public void testSearchQuery() {
        mcr.<String> mockConfigValue(ConfigValues.LDAPSecurityAuthentication,
                Config.DefaultConfigurationVersion,
                "SIMPLE");
        mcr.<Integer> mockConfigValue(ConfigValues.SearchResultsLimit, Config.DefaultConfigurationVersion, 100);

        SearchParameters parameters = new SearchParameters("AdUser: gandalf", SearchType.AdUser);
        SearchQuery<SearchParameters> searchCmd = new SearchQuery<SearchParameters>(parameters);
        searchCmd.setInternalExecution(true);
        searchCmd.Execute();
    }
}
