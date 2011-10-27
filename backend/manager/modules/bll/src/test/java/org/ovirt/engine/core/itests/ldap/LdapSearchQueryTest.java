package org.ovirt.engine.core.itests.ldap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.ovirt.engine.core.bll.SearchQuery;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;

public class LdapSearchQueryTest {

    @Test
    public void testSearchQuery() {
        IConfigUtilsInterface mockConfigUtils = mock(IConfigUtilsInterface.class);
        Config.setConfigUtils(mockConfigUtils);
        when(mockConfigUtils.<String> GetValue(ConfigValues.LDAPSecurityAuthentication,
                Config.DefaultConfigurationVersion)).thenReturn("SIMPLE");
        when(mockConfigUtils.<Integer> GetValue(ConfigValues.SearchResultsLimit,
                Config.DefaultConfigurationVersion)).thenReturn(100);

        SearchParameters parameters = new SearchParameters("AdUser: gandalf", SearchType.AdUser);
        SearchQuery<SearchParameters> searchCmd = new SearchQuery<SearchParameters>(parameters);
        searchCmd.Execute();
    }
}
